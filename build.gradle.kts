import org.jetbrains.changelog.closure
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.grammarkit.tasks.GenerateLexer
import org.jetbrains.grammarkit.tasks.GenerateParser

plugins {
    // Java support
    id("java")
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "0.6.5"
    // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "0.6.2"
    // grammarkit - read more: https://github.com/JetBrains/gradle-grammar-kit-plugin
    id("org.jetbrains.grammarkit") version "2020.3.2"
}

// Import variables from gradle.properties file
val pluginGroup: String by project
val pluginName: String by project
val pluginVersion: String by project
val pluginSinceBuild: String by project
val pluginUntilBuild: String by project
val pluginVerifierIdeVersions: String by project

val platformType: String by project
val platformVersion: String by project
val platformDownloadSources: String by project

group = pluginGroup
version = pluginVersion

// Configure project's dependencies
repositories {
    mavenCentral()
}

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName = pluginName
    version = platformVersion
    type = platformType
    downloadSources = platformDownloadSources.toBoolean()
    updateSinceUntilBuild = true
}

changelog {
    headerParserRegex = "^[-._+0-9a-zA-Z]+\$"
}

grammarKit {
    // version of IntelliJ patched JFlex (see bintray link below), Default is 1.7.0-1
    jflexRelease = "1.7.0-1"

    // tag or short commit hash of Grammar-Kit to use (see link below). Default is 2020.1
    grammarKitRelease = "2020.1"
}

tasks {

    task("writeMetadataFiles") {
        outputs.upToDateWhen { false }
        doLast {
            project.buildDir.resolve("version.txt").writeText(pluginVersion)
            project.buildDir.resolve("zipfile.txt").writeText(buildPlugin.get().archiveFile.get().toString())
            project.buildDir.resolve("latest_changelog.md").writeText(changelog.getLatest().toText())
        }
    }

    task<GenerateLexer>("generateNixLexer") {
        source = "src/main/lang/Nix.flex"
        targetDir = "src/gen/java/org/nixos/idea/lang"
        targetClass = "NixLexer"
        purgeOldFiles = true
    }

    task<GenerateParser>("generateNixParser") {
        source = "src/main/lang/Nix.bnf"
        targetRoot = "src/gen/java"
        pathToParser = "/org/nixos/idea/lang/NixParser"
        pathToPsiRoot = "/org/nixos/idea/psi"
        purgeOldFiles = true
    }

    // Set the compatibility versions to 1.8
    withType<JavaCompile> {
        dependsOn(
                "generateNixLexer",
                "generateNixParser"
        )

        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"

        sourceSets {
            // srcDir "path" appends to the default src/main/java (etc), as opposed to main.java.srcDir = xyz which would override the defaults
            main {
                java {
                    srcDir("src/gen/java")
                    srcDir("src/main/java")
                }
            }
        }

    }

    patchPluginXml {
        version(pluginVersion)
        sinceBuild(pluginSinceBuild)
        untilBuild(pluginUntilBuild)

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription(
                closure {
                    File("./README.md").readText().lines().run {
                        val start = "<!-- Plugin description -->"
                        val end = "<!-- Plugin description end -->"

                        if (!containsAll(listOf(start, end))) {
                            throw GradleException("Plugin description section not found in README.md file:\n$start ... $end")
                        }
                        subList(indexOf(start) + 1, indexOf(end))
                    }.joinToString("\n").run { markdownToHTML(this) }
                }
        )

        // Get the latest available change notes from the changelog file
        changeNotes(
                closure {
                    changelog.getLatest().toHTML()
                }
        )
    }

    runPluginVerifier {
        ideVersions(pluginVerifierIdeVersions)
    }

    publishPlugin {
        token(System.getenv("JETBRAINS_TOKEN"))
        channels(pluginVersion.split('-').getOrElse(1) { "default" }.split('.').first())
    }

}
