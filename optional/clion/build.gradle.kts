import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    id("local.plugin-module")
}

pluginModule {
    platform = IntelliJPlatformType.CLion
}

dependencies {
    implementation(projects.core)
}
