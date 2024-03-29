import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

/**
 * Replaces the value of [propertyName] in the given [propertiesFile].
 * The old value given by [oldValue] is replaced by the value given by [newValue].
 */
@UntrackedTask(because = "Changes project files in-place")
abstract class ChangePropertyTask @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout,
) : DefaultTask() {

    @get:Input
    val oldValue = objects.property<String>()

    @get:Input
    val newValue = objects.property<String>()

    @get:Input
    val propertyName = objects.property<String>()

    @get:OutputFile
    val propertiesFile = objects.fileProperty()
        .convention(layout.projectDirectory.file("gradle.properties"))

    @TaskAction
    fun run() {
        val escapedName = Regex.escape(propertyName.get())
        val escapedOldValue = Regex.escape(oldValue.get())
        val escapedReplacement = Regex.escapeReplacement(newValue.get())

        val file = propertiesFile.get().asFile
        file.writeText(
            file.readText().replace(
                Regex(
                    "^(\\s*$escapedName\\s*=\\s*)$escapedOldValue(\\s*)$",
                    RegexOption.MULTILINE,
                ),
                "$1${escapedReplacement}$2",
            )
        )
    }
}
