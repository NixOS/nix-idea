import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    id("local.plugin-module")
}

pluginModule {
    platform = IntelliJPlatformType.IntellijIdeaUltimate
}

dependencies {
    implementation(projects.core)
}
