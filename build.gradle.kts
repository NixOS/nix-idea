import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.tasks.RunPluginVerifierTask

plugins {
    id("java")
    alias(libs.plugins.jetbrains.intellij)
    alias(libs.plugins.jetbrains.changelog)
    alias(libs.plugins.jetbrains.grammarkit)
    id("local.bump-version")
    id("local.jbr-guidance")
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
    testImplementation(platform(libs.junit5.bom))
    testImplementation(libs.junit5.jupiter)
    testImplementation(libs.junit5.platform.testkit)
    testRuntimeOnly(libs.junit5.vintage.engine)
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

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    withType<Javadoc> {
        options.encoding = "UTF-8"
    }

    withType<Test> {
        systemProperty("idea.test.execution.policy", "org.nixos.idea.NixTestExecutionPolicy")
        systemProperty("plugin.testDataPath", rootProject.rootDir.resolve("src/test/testData").path)
    }

    task<MetadataTask>("metadata") {
        outputDir = layout.buildDirectory.dir("metadata")
        file("version.txt", pluginVersion)
        file("zipfile.txt") { buildPlugin.get().archiveFile.get().toString() }
        file("latest_changelog.md") {
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.MARKDOWN
                )
            }
        }
    }

    generateLexer {
        sourceFile = file("src/main/lang/Nix.flex")
        targetOutputDir = file("src/gen/java/org/nixos/idea/lang")
        purgeOldFiles = true
    }

    generateParser {
        sourceFile = file("src/main/lang/Nix.bnf")
        targetRootOutputDir = file("src/gen/java")
        pathToParser = "/org/nixos/idea/lang/NixParser"
        pathToPsiRoot = "/org/nixos/idea/psi"
        purgeOldFiles = true
        // Task :generateLexer deletes files generated by this task when executed afterward.
        mustRunAfter(generateLexer)
    }

    compileJava {
        dependsOn(generateLexer, generateParser)
    }

    test {
        useJUnitPlatform {
            excludeTags("mock")
        }
    }

    patchPluginXml {
        version = pluginVersion
        sinceBuild = pluginSinceBuild
        untilBuild = pluginUntilBuild

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        pluginDescription = providers.provider {
            projectDir.resolve("README.md").readText().lines()
                .run {
                    val start = "<!-- Plugin description -->"
                    val end = "<!-- Plugin description end -->"
                    if (!containsAll(listOf(start, end))) {
                        throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                    }
                    subList(indexOf(start) + 1, indexOf(end))
                }
                .joinToString("\n")
                .run { markdownToHTML(this) }
        }

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
        // Version 1.364 seems to be broken and always complains about supposedly missing 'plugin.xml':
        // https://youtrack.jetbrains.com/issue/MP-6388
        verifierVersion = "1.307"
    }

    publishPlugin {
        token = providers.environmentVariable("JETBRAINS_TOKEN")
        // Note: `listOf("foo").first()` does not what you think on Java 21 and Gradle 8.6. (The return type is TaskProvider<Task>)
        // See https://github.com/gradle/gradle/issues/27699 and https://youtrack.jetbrains.com/issue/KT-65235.
        channels = listOf(pluginVersion.split('-').getOrElse(1) { "default" }.split('.')[0])
    }

}
