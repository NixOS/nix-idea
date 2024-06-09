import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.tasks.CustomRunIdeTask
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask.FailureLevel
import java.util.EnumSet

plugins {
    alias(libs.plugins.jetbrains.intellij.platform)
    alias(libs.plugins.jetbrains.changelog)
    id("local.plugin-module")
    id("local.bump-version")
}

pluginModule {
    platform = IntelliJPlatformType.IntellijIdeaCommunity
}

// Import variables from gradle.properties file
val pluginVersion: String by project
val platformVersion: String by project

dependencies {
    implementation(projects.core)
    implementation(projects.optional.clion)
    implementation(projects.optional.java)
    implementation(projects.optional.python)
    implementation(projects.optional.ultimate)

    intellijPlatform {
        // Version 1.364 seems to be broken and always complains about supposedly missing 'plugin.xml':
        // https://youtrack.jetbrains.com/issue/MP-6388
        pluginVerifier("1.307")
    }
}

// Configure intellij-platform-gradle-plugin plugin.
// Read more: https://github.com/JetBrains/intellij-platform-gradle-plugin
intellijPlatform {
    verifyPlugin {
        ides {
            ides(
                providers.gradleProperty("verifierIdeVersionOverride")
                    // Verify only against the IDE specified by the property
                    .map { listOf(it) }
                    // If property is not set, verify against the recommended list of IDEs
                    .orElse(ProductReleasesValueSource())
            )
        }
        failureLevel = EnumSet.complementOf(
            EnumSet.of(
                FailureLevel.DEPRECATED_API_USAGES,
                FailureLevel.SCHEDULED_FOR_REMOVAL_API_USAGES,
                FailureLevel.EXPERIMENTAL_API_USAGES,
            )
        )
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

tasks {

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

    val runIntellijUltimate by registering(CustomRunIdeTask::class) {
        type = IntelliJPlatformType.IntellijIdeaUltimate
        version = platformVersion
    }

    val runCLion by registering(CustomRunIdeTask::class) {
        type = IntelliJPlatformType.CLion
        version = platformVersion
    }

    val runPyCharm by registering(CustomRunIdeTask::class) {
        type = IntelliJPlatformType.PyCharmCommunity
        version = platformVersion
    }

    val runPyCharmProfessional by registering(CustomRunIdeTask::class) {
        type = IntelliJPlatformType.PyCharmProfessional
        version = platformVersion
    }

}
