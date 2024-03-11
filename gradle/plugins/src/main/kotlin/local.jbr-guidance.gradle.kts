import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.tooling.Failure
import org.gradle.tooling.events.FailureResult
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener
import org.jetbrains.intellij.tasks.RunIdeBase
import java.util.function.Predicate

val jbrHome = rootProject.file("jbr")

task<Exec>("jbr") {
    description = "Create a symlink to package jetbrains.jdk"
    group = "build setup"
    commandLine("nix-build", "<nixpkgs>", "-A", "jetbrains.jdk", "-o", jbrHome)
}

jbrHome.resolve("bin/java").takeIf { it.exists() }
    ?.also { jbrExecutable ->
        // Override JVM of gradle-intellij-plugin with JVM at `jbr/bin/java`
        // https://github.com/JetBrains/gradle-intellij-plugin/issues/1437#issuecomment-1987310948
        tasks.withType<RunIdeBase> {
            projectExecutable = jbrExecutable.toString()
        }
        pluginManager.withPlugin("org.jetbrains.intellij") {
            // Uses `withPlugin` because the following code must run after the gradle-intellij-plugin got applied.
            // We must also use `afterEvaluate`, as the gradle-intellij-plugin uses `afterEvaluate` as well.
            // Otherwise, gradle-intellij-plugin would just overwrite our configuration.
            afterEvaluate {
                tasks.withType<Test> {
                    executable = jbrExecutable.toString()
                }
            }
        }
    }
    ?: run {
        // There is no JVM at `jbr/bin/java`. Monitor the build and provide some helpful message when it fails.
        val service = gradle.sharedServices.registerIfAbsent("jbr-guidance", JbrGuidance::class) {}
        serviceOf<BuildEventsListenerRegistry>().onTaskCompletion(service)
    }

abstract class JbrGuidance : BuildService<BuildServiceParameters.None>, OperationCompletionListener, AutoCloseable {
    private val regex = Regex("""\.gradle/.*/(jbr|jbre)/.*/java\b""")
    private val logger = Logging.getLogger("jbr-guidance")
    private var observedJbrError = false

    override fun onFinish(event: FinishEvent?) {
        val result = event?.result as? FailureResult
        result?.failures?.forEach { failure ->
            if (anyCauseMatches(failure) { it.message?.contains(regex) == true }) {
                observedJbrError = true
            }
        }
    }

    override fun close() {
        if (observedJbrError) {
            logger.error(
                """
                |
                |! Info for users on NixOS:
                |!
                |! The JetBrains Runtime (JBR) downloaded by Gradle is not compatible with NixOS.
                |! You may run the ‘:jbr’ task to configure the runtime of <nixpkgs> instead.
                |! Alternatively, you may run the following command within the project directory.
                |!
                |!   nix-build '<nixpkgs>' -A jetbrains.jdk -o jbr
                |!
                |! This will create a symlink to the package jetbrains.jdk of nixpkgs at
                |! ${'$'}projectDir/jbr, which is automatically detected by future builds.
                """.trimMargin()
            )
        }
    }

    private fun anyCauseMatches(failure: Failure?, condition: Predicate<Failure>): Boolean {
        return if (failure == null) {
            false
        } else if (condition.test(failure)) {
            true
        } else {
            failure.causes.any { anyCauseMatches(it, condition) }
        }
    }
}
