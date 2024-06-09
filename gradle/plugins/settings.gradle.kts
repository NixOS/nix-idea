dependencyResolutionManagement {
    // Reuse version catalog from the main build.
    versionCatalogs {
        create("libs", { from(files("../libs.versions.toml")) })
    }
}

// Avoid warning about missing project name after enabling project accessors
rootProject.name = "plugins"
