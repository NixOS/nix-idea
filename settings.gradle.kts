pluginManagement {
    includeBuild("gradle/plugins")
    repositories {
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        gradlePluginPortal()
    }
}

rootProject.name = "NixIDEA"

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
