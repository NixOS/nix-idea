pluginManagement {
    includeBuild("gradle/plugins")
    // TODO remove the following block when we don't need the SNAPSHOT anymore
    repositories {
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        gradlePluginPortal()
    }
}

rootProject.name = "NixIDEA"

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
