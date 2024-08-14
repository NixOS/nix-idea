import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.tooling.Failure
import org.gradle.tooling.events.FailureResult
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask
import org.jetbrains.intellij.platform.gradle.tasks.aware.RuntimeAware
import java.util.function.Predicate

val jbrHome = rootProject.file("jbr")

task<Exec>("jbr") {
    description = "Create a symlink to package jetbrains.jdk"
    group = "build setup"
    commandLine("nix-build", "<nixpkgs>", "-A", "jetbrains.jdk", "-o", jbrHome)
}

jbrHome.resolve("bin/java").takeIf { it.exists() }
    ?.also { _ ->
        // Use JVM at `jbr/bin/java`. The JVM otherwise downloaded by intellij-platform-gradle-plugin wouldn't work on NixOS.
        // https://github.com/JetBrains/intellij-platform-gradle-plugin/issues/1437#issuecomment-1987310948
        // VerifyPluginTask doesn't execute the JVM, so we can ignore this task for the sake of consistency with other platforms.
        tasks.configureEach {
            if (this is RuntimeAware && this !is VerifyPluginTask) {
                runtimeDirectory = jbrHome
            }
        }
    }
    ?: run {
        // There is no JVM at `jbr/bin/java`. Monitor the build and provide some helpful message when it fails.
        val service = gradle.sharedServices.registerIfAbsent("jbr-guidance", JbrGuidance::class) {}
        serviceOf<BuildEventsListenerRegistry>().onTaskCompletion(service)
        // If the configuration cache is enabled, the JBR failure may be triggered during serialization
        // before any task gets executed. (As of intellij-platform-gradle-plugin 2.0.0 and Gradle 8.6.)
        // Unfortunately, only failures in tasks are reported to the JbrGuidance build service.
        // If there are tasks scheduled, but none of them is executed, we therefore also print the message.
        project.gradle.taskGraph.whenReady {
            if (allTasks.isNotEmpty()) {
                service.get().expectAtLeastOneTask()
            }
        }
    }

abstract class JbrGuidance : BuildService<BuildServiceParameters.None>, OperationCompletionListener, AutoCloseable {
    private val regex = Regex("""\.gradle/.*/(jbr|jbre)/.*/java\b""")
    private val logger = Logging.getLogger("jbr-guidance")
    private var taskMissing = false
    private var observedJbrError = false

    fun expectAtLeastOneTask() {
        taskMissing = true
    }

    override fun onFinish(event: FinishEvent?) {
        taskMissing = false
        val result = event?.result as? FailureResult
        result?.failures?.forEach { failure ->
            if (anyCauseMatches(failure) { it.message?.contains(regex) == true }) {
                observedJbrError = true
            }
        }
    }

    override fun close() {
        if (observedJbrError || taskMissing) {
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
