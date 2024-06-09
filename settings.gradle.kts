pluginManagement {
    includeBuild("gradle/plugins")
    // TODO remove the following block when we don't need the SNAPSHOT anymore
    repositories {
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        gradlePluginPortal()
    }
}

rootProject.name = "NixIDEA"

include("core")
include("optional:clion")
include("optional:java")
include("optional:python")
include("optional:ultimate")

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
