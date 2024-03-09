pluginManagement {
    includeBuild("gradle/plugins")
    plugins {
        // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
        id("org.jetbrains.intellij") version "1.17.2"
        // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
        id("org.jetbrains.changelog") version "2.2.0"
        // grammarkit - read more: https://github.com/JetBrains/gradle-grammar-kit-plugin
        id("org.jetbrains.grammarkit") version "2022.3.2.2"
    }
}

rootProject.name = "NixIDEA"
