import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.ChangelogPluginExtension
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.tasks.PatchPluginXmlTask
import org.jetbrains.intellij.platform.gradle.tasks.compaion.ProcessResourcesCompanion

plugins {
    alias(libs.plugins.jetbrains.changelog) apply false
    alias(libs.plugins.jetbrains.grammarkit)
    id("local.plugin-module")
}

// Import variables from gradle.properties file
val pluginVersion: String by project
val pluginSinceBuild: String by project
val pluginUntilBuild: String by project

pluginModule {
    platform = IntelliJPlatformType.IntellijIdeaCommunity
}

// Enable patching of plugin.xml
PatchPluginXmlTask.register(project)
ProcessResourcesCompanion.register(project)

// Configure project's dependencies
dependencies {
    compileOnly(libs.jetbrains.annotations)

    testImplementation(platform(libs.junit5.bom))
    testImplementation(libs.junit5.jupiter)
    testImplementation(libs.junit5.platform.testkit)
    testRuntimeOnly(libs.junit5.vintage.engine)
}

// Configure intellij-platform-gradle-plugin plugin.
// Read more: https://github.com/JetBrains/intellij-platform-gradle-plugin
intellijPlatform {
    pluginConfiguration {
        id = "nix-idea"
        name = "NixIDEA"
        version = pluginVersion
        vendor {
            name = "NixOS"
        }
        ideaVersion {
            sinceBuild = pluginSinceBuild
            untilBuild = pluginUntilBuild
        }
        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        description = providers.provider {
            rootDir.resolve("README.md").readText().lines()
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
            with(rootProject.the<ChangelogPluginExtension>()) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML
                )
            }
        }
    }
}

grammarKit {
    // version of IntelliJ patched JFlex
    // -> https://github.com/JetBrains/intellij-deps-jflex
    jflexRelease = "1.9.2"

    // tag or short commit hash of Grammar-Kit to use
    // -> https://github.com/JetBrains/Grammar-Kit
    grammarKitRelease = "2022.3.2"
}

val lexerSource = layout.buildDirectory.dir("generated/sources/lexer/java/main")
sourceSets {
    main {
        java {
            srcDir(lexerSource)
            srcDir(tasks.generateParser)
        }
    }
}

tasks {

    generateLexer {
        sourceFile = layout.projectDirectory.file("src/main/lang/Nix.flex")
        targetOutputDir = lexerSource.map { it.dir("org/nixos/idea/lang") }
        purgeOldFiles = true
    }

    generateParser {
        sourceFile = layout.projectDirectory.file("src/main/lang/Nix.bnf")
        targetRootOutputDir = layout.buildDirectory.dir("generated/sources/parser/java/main")
        // Maybe we can remove the following properties in the future
        // https://github.com/JetBrains/gradle-grammar-kit-plugin/issues/178
        pathToParser = "org/nixos/idea/lang/NixParser.java"
        pathToPsiRoot = "org/nixos/idea/psi"
        purgeOldFiles = true
    }

    compileJava {
        dependsOn(generateLexer)
        // dependency to generateParser is implicitly detected by Gradle
    }

    compileKotlin {
        dependsOn(generateLexer)
        // dependency to generateParser is implicitly detected by Gradle
    }

    test {
        useJUnitPlatform {
            excludeTags("mock")
        }
    }

}
