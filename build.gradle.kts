import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java")
    kotlin("jvm") version embeddedKotlinVersion
    alias(libs.plugins.jetbrains.intellij.platform)
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
    // Don't use Gradle's toolchain feature as it prevents building the project with more recent JDKs. Related issues:
    // https://github.com/gradle/gradle/issues/16256 - Ability to set a min language version for a toolchain
    // https://github.com/gradle/gradle/issues/17444 - Toolchains feature does not appear to treat Java as backwards compatible
    // https://github.com/gradle/gradle/issues/18894 - More flexibility in querying Java toolchains
    sourceCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// Configure project's dependencies
repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    compileOnly(libs.jetbrains.annotations)

    testImplementation(platform(libs.junit5.bom))
    testImplementation(libs.junit5.jupiter)
    testImplementation(libs.junit5.platform.testkit)
    testImplementation(libs.junit4)
    testRuntimeOnly(libs.junit5.vintage.engine)

    intellijPlatform {
        create(platformType, platformVersion)
        testFramework(TestFrameworkType.Platform)
        //testFramework(TestFrameworkType.JUnit5)
    }
}

// Configure intellij-platform-gradle-plugin plugin.
// Read more: https://github.com/JetBrains/intellij-platform-gradle-plugin
intellijPlatform {
    projectName = pluginName
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
    pluginVerification {
        freeArgs = listOf("-mute", "TemplateWordInPluginName")
        ides {
            ides(
                providers.gradleProperty("verifierIdeVersionOverride")
                    // Verify only against the IDE specified by the property
                    .map { listOf(it) }
                    // If property is not set, verify against the IDEs in gradle/productsReleases.txt
                    .orElse(
                        layout.projectDirectory.file("gradle/productsReleases.txt")
                            .let { providers.fileContents(it).asText }
                            .map { it.lines().map(String::trim).filter(String::isNotEmpty) }
                    )
            )
        }
    }
    publishing {
        token = providers.environmentVariable("JETBRAINS_TOKEN")
        // Note: `listOf("foo").first()` does not what you think on Java 21 and Gradle 8.6. (The return type is TaskProvider<Task>)
        // See https://github.com/gradle/gradle/issues/27699 and https://youtrack.jetbrains.com/issue/KT-65235.
        channels = listOf(pluginVersion.split('-').getOrElse(1) { "default" }.split('.')[0])
    }
}

changelog {
    repositoryUrl = "https://github.com/NixOS/nix-idea"
    lineSeparator = "\n"
    // Workarounds because our version numbers do not match the format of semantic versioning:
    headerParserRegex = "^[-._+0-9a-zA-Z]+\$"
    combinePreReleases = false
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

    val runIntellij by intellijPlatformTesting.runIde.registering {
        type = IntelliJPlatformType.IntellijIdeaCommunity
    }

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

    register<MetadataTask>("metadata") {
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

    register<MetadataTask>("updateProductsReleases") {
        doNotTrackState("Updates files outside of build directory")
        outputDir = layout.projectDirectory.dir("gradle")
        file(
            "productsReleases.txt",
            intellijPlatform.pluginVerification.ides.ProductReleasesValueSource().map {
                it.joinToString("\n", "", "\n")
            })
    }

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
