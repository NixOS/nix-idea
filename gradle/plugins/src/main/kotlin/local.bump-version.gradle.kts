val VERSION_PROPERTY = "pluginVersion"

tasks.register<ChangePropertyTask>("bumpVersion") {
    description = "Bumps the version of the project"
    oldValue = providers.gradleProperty(VERSION_PROPERTY)
    newValue = oldValue.map { PluginVersions.increment(it) }
    propertyName = VERSION_PROPERTY
    dependsOn("patchChangelog")
}

tasks.named("patchChangelog") {
    // GitHub seems to use CRLF as line feeds.
    // We have to replace them to avoid files with mixed line endings.
    doFirst {
        val releaseNote = property("releaseNote")
        if (releaseNote is String) {
            setProperty("releaseNote", releaseNote.replace("\r\n", "\n"))
        }
    }
}
