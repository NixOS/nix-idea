import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask
import org.jetbrains.intellij.tasks.RunPluginVerifierTask

plugins {
    // Java support
    id("java")
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "1.17.2"
    // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "2.2.0"
    // grammarkit - read more: https://github.com/JetBrains/gradle-grammar-kit-plugin
    id("org.jetbrains.grammarkit") version "2022.3.2.2"
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
    sourceCompatibility = JavaVersion.VERSION_17
}

// Configure project's dependencies
repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
}

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij.pluginName = pluginName
intellij {
    version = platformVersion
    type = platformType
    updateSinceUntilBuild = true
}

changelog {
    repositoryUrl = "https://github.com/NixOS/nix-idea"
    lineSeparator = "\n"
    // Workarounds because our version numbers do not match the format of semantic versioning:
    headerParserRegex = "^[-._+0-9a-zA-Z]+\$"
    combinePreReleases = false
}

grammarKit {
    // version of IntelliJ patched JFlex (see bintray link below), Default is 1.7.0-1
    jflexRelease = "1.7.0-1"

    // tag or short commit hash of Grammar-Kit to use (see link below). Default is 2020.1
    grammarKitRelease = "2021.1.2"
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
// Check https://github.com/gradle/gradle/issues/20151 if an alternative for deprecated buildFinished became available.
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
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    withType<Javadoc> {
        options.encoding = "UTF-8"
    }

    task<Exec>("jbr") {
        description = "Create a symlink to package jetbrains.jdk"
        group = "build setup"
        commandLine("nix-build", "<nixpkgs>", "-A", "jetbrains.jdk", "-o", "jbr")
    }

    withType<org.jetbrains.intellij.tasks.RunIdeBase> {
        project.file("jbr/bin/java")
            .takeIf { it.exists() }
            ?.let { projectExecutable = it.toString() }
            ?: tasksUsingDownloadedJbr.add(this)
    }

    withType<Test> {
        systemProperty("idea.test.execution.policy", "org.nixos.idea.NixTestExecutionPolicy")
        systemProperty("plugin.testDataPath", rootProject.rootDir.resolve("src/test/testData").path)
    }

    task("metadata") {
        outputs.upToDateWhen { false }
        doLast {
            val dir = project.layout.buildDirectory.dir("metadata").get().asFile
            dir.mkdirs()
            dir.resolve("version.txt").writeText(pluginVersion)
            dir.resolve("zipfile.txt").writeText(buildPlugin.get().archiveFile.get().toString())
            dir.resolve("latest_changelog.md").writeText(with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.MARKDOWN
                )
            })
        }
    }

    val generateNixLexer by registering(GenerateLexerTask::class) {
        sourceFile = file("src/main/lang/Nix.flex")
        targetOutputDir = file("src/gen/java/org/nixos/idea/lang")
        purgeOldFiles = true
    }

    val generateNixParser by registering(GenerateParserTask::class) {
        sourceFile = file("src/main/lang/Nix.bnf")
        targetRootOutputDir = file("src/gen/java")
        pathToParser = "/org/nixos/idea/lang/NixParser"
        pathToPsiRoot = "/org/nixos/idea/psi"
        purgeOldFiles = true
        // Task :generateLexer deletes files generated by this task when executed afterward.
        mustRunAfter(generateLexer)
    }

    compileJava {
        dependsOn(generateNixLexer, generateNixParser)
    }

    test {
        useJUnitPlatform()
    }

    patchPluginXml {
        version = pluginVersion
        sinceBuild = pluginSinceBuild
        untilBuild = pluginUntilBuild

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription = projectDir.resolve("README.md").readText().lines().run {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            if (!containsAll(listOf(start, end))) {
                throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
            }
            subList(indexOf(start) + 1, indexOf(end))
        }.joinToString("\n").run { markdownToHTML(this) }

        // Get the latest available change notes from the changelog file
        changeNotes = provider {
            with(changelog) {
                renderItem(
                        (getOrNull(pluginVersion) ?: getUnreleased())
                                .withHeader(false)
                                .withEmptySections(false),
                        Changelog.OutputType.HTML
                )
            }
        }
    }

    runPluginVerifier {
        failureLevel = RunPluginVerifierTask.FailureLevel.ALL
    }

    publishPlugin {
        token = System.getenv("JETBRAINS_TOKEN")
        channels = listOf(pluginVersion.split('-').getOrElse(1) { "default" }.split('.').first())
    }

}

apply(from = "gradle/bumpVersion.gradle.kts")
