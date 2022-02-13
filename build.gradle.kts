import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask

plugins {
    // Java support
    id("java")
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "1.3.1"
    // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "1.3.1"
    // grammarkit - read more: https://github.com/JetBrains/gradle-grammar-kit-plugin
    id("org.jetbrains.grammarkit") version "2021.2.1"
}

// Import variables from gradle.properties file
val pluginGroup: String by project
val pluginName: String by project
val pluginVersion: String by project
val pluginSinceBuild: String by project
val pluginUntilBuild: String by project

val platformType: String by project
val platformVersion: String by project

group = pluginGroup
version = pluginVersion

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

// Configure project's dependencies
repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
}

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij.pluginName.set(pluginName)
intellij {
    version.set(platformVersion)
    type.set(platformType)
    updateSinceUntilBuild.set(true)
}

changelog {
    headerParserRegex.set("^[-._+0-9a-zA-Z]+\$")
}

grammarKit {
    // version of IntelliJ patched JFlex (see bintray link below), Default is 1.7.0-1
    jflexRelease.set("1.7.0-1")

    // tag or short commit hash of Grammar-Kit to use (see link below). Default is 2020.1
    grammarKitRelease.set("2021.1.2")
}

sourceSets {
    main {
        java {
            srcDir("src/gen/java")
            srcDir("src/main/java")
        }
    }
}

val tasksUsingDownloadedJbr = mutableListOf<Task>()
gradle.buildFinished {
    val regex = Regex("""\.gradle/.*/jbr/.*/java\b""")
    for (task in tasksUsingDownloadedJbr) {
        if (task.state.failure?.cause?.message?.contains(regex) == true) {
            logger.error("""
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
                """.trimMargin())
            break
        }
    }
}

tasks {

    task<Exec>("jbr") {
        description = "Create a symlink to package jetbrains.jdk"
        group = "build setup"
        commandLine("nix-build", "<nixpkgs>", "-A", "jetbrains.jdk", "-o", "jbr")
    }

    withType<org.jetbrains.intellij.tasks.RunIdeBase> {
        project.file("jbr/bin/java")
            .takeIf { it.exists() }
            ?.let { projectExecutable.set(it.toString()) }
            ?: tasksUsingDownloadedJbr.add(this)
    }

    task("metadata") {
        outputs.upToDateWhen { false }
        doLast {
            val dir = project.buildDir.resolve("metadata")
            dir.mkdirs()
            dir.resolve("version.txt").writeText(pluginVersion)
            dir.resolve("zipfile.txt").writeText(buildPlugin.get().archiveFile.get().toString())
            dir.resolve("latest_changelog.md").writeText(changelog.getLatest().toText())
        }
    }

    val generateNixLexer by registering(GenerateLexerTask::class) {
        source.set("src/main/lang/Nix.flex")
        targetDir.set("src/gen/java/org/nixos/idea/lang")
        targetClass.set("_NixLexer")
        purgeOldFiles.set(true)
    }

    val generateNixParser by registering(GenerateParserTask::class) {
        source.set("src/main/lang/Nix.bnf")
        targetRoot.set("src/gen/java")
        pathToParser.set("/org/nixos/idea/lang/NixParser")
        pathToPsiRoot.set("/org/nixos/idea/psi")
        purgeOldFiles.set(true)
    }

    compileJava {
        dependsOn(generateNixLexer, generateNixParser)
    }

    test {
        useJUnitPlatform()
    }

    patchPluginXml {
        version.set(pluginVersion)
        sinceBuild.set(pluginSinceBuild)
        untilBuild.set(pluginUntilBuild)

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription.set(
            projectDir.resolve("README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        )

        // Get the latest available change notes from the changelog file
        changeNotes.set(provider { changelog.getLatest().toHTML() })
    }

    runPluginVerifier {
        failureLevel.set(org.jetbrains.intellij.tasks.RunPluginVerifierTask.FailureLevel.ALL)
    }

    publishPlugin {
        token.set(System.getenv("JETBRAINS_TOKEN"))
        channels.set(listOf(pluginVersion.split('-').getOrElse(1) { "default" }.split('.').first()))
    }

}
