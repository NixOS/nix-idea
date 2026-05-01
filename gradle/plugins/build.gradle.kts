plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(plugin(libs.plugins.jetbrains.intellij.platform))
}

/**
 * Maps plugin dependencies to its maven coordinates.
 *
 * Hopefully, there will be native support for adding plugins as a dependency in the future.
 * See [Gradle Issue #17963 – Accept plugin declarations from version catalog also as libraries](https://github.com/gradle/gradle/issues/17963).
 */
fun plugin(pluginProvider: Provider<PluginDependency>): Provider<Map<String, String>> {
    return pluginProvider.map {
        val id = it.pluginId
        mapOf(
            "group" to id,
            "name" to "$id.gradle.plugin", // PLUGIN_MARKER_SUFFIX
            "version" to it.version.requiredVersion,
        )
    }
}
