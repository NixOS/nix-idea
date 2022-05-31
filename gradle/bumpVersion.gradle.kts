import java.util.regex.Pattern

val GRADLE_PROPERTIES = "gradle.properties"
val VERSION_PROPERTY = "pluginVersion"

task("bumpVersion") {
    description = "Bumps the version of the project"
    dependsOn("patchChangelog")
    outputs.upToDateWhen { false }

    doLast {
        val prevVersion = project.property(VERSION_PROPERTY) as String
        val nextVersion = incrementVersion(prevVersion)
        replaceInProperties(prevVersion, nextVersion)
    }
}

tasks.named("patchChangelog") {
    // GitHub seems to use CRLF as line feeds.
    // We have to replace them to avoid files with mixed line endings.
    doFirst {
        val releaseNote = property("releaseNote") as String
        if (releaseNote != null) {
            setProperty("releaseNote", releaseNote.replace("\r\n", "\n"))
        }
    }
    // The task (as of org.jetbrains.changelog 1.3.1) removes trailing newlines from the changelog.
    // Add a trailing newline afterwards as a workaround.
    doLast {
        val file = file(property("outputFile"))
        if (!file.readText().endsWith("\n")) {
            file.appendText("\n")
        }
    }
}

/**
 * Replaces the [prevVersion] with [nextVersion] for the `pluginVersion` property in `gradle.properties`.
 */
fun replaceInProperties(prevVersion: String, nextVersion: String): Unit {
    val pProperty = Regex.escape(VERSION_PROPERTY)
    val pVersion = Regex.escape(prevVersion)
    val rVersion = Regex.escapeReplacement(nextVersion)

    val file = file(GRADLE_PROPERTIES)
    val previousContent = file.readText()
    file.writeText(previousContent.replace(
            Regex("^(\\s*$pProperty\\s*=\\s*)$pVersion(\\s*)$", RegexOption.MULTILINE),
            "$1${rVersion}$2"))
}

/**
 * Increments the last integer within the given string.
 */
fun incrementVersion(previous: String): String {
    val matcher = Pattern.compile("(.*[^\\d])(\\d+)([^\\d]*)").matcher(previous)
    if (matcher.matches()) {
        val incrementedNumber = Integer.parseInt(matcher.group(2)) + 1
        return matcher.group(1) + incrementedNumber + matcher.group(3)
    }
    else {
        throw GradleException("Unsupported version: " + previous)
    }
}
