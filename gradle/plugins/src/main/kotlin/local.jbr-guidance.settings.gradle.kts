@file:Suppress("UnstableApiUsage")

import org.gradle.kotlin.dsl.support.serviceOf
import org.jetbrains.intellij.tasks.RunIdeBase
import java.util.function.Predicate

val jbrHome = file("jbr")

gradle.rootProject {
    task<Exec>("jbr") {
        description = "Create a symlink to package jetbrains.jdk"
        group = "build setup"
        commandLine("nix-build", "<nixpkgs>", "-A", "jetbrains.jdk", "-o", jbrHome)
    }
}

jbrHome.resolve("bin/java").takeIf { it.exists() }
    ?.also { jbrExecutable -> gradle.allprojects {
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
    }}
    ?: run {
        // There is no JVM at `jbr/bin/java`. Monitor the build and provide some helpful message when it fails.
        val flowScope = serviceOf<FlowScope>()
        val flowProviders = serviceOf<FlowProviders>()
        flowScope.always(JbrGuidance::class) {
            parameters.buildResult = flowProviders.buildWorkResult
        }
    }

abstract class JbrGuidance : FlowAction<JbrGuidance.Parameters> {
    private val regex = Regex("""\.gradle/.*/(jbr|jbre)/.*/java\b""")
    private val logger = Logging.getLogger("jbr-guidance")

    interface Parameters : FlowParameters {
        @get:Input
        val buildResult: Property<BuildWorkResult>
    }

    override fun execute(parameters: Parameters) {
        val result = parameters.buildResult.get()
        if (anyCauseMatches(result.failure.orElse(null)) { it.message?.contains(regex) == true }) {
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

    private fun anyCauseMatches(e: Throwable?, condition: Predicate<Throwable>): Boolean {
        var current = e
        while (current != null) {
            if (condition.test(current)) {
                return true
            }
            current = current.cause
        }
        return false
    }
}
