import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class MetadataTask : DefaultTask() {

    @get:Input
    abstract val files: MapProperty<String, String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Inject
    protected abstract val providers: ProviderFactory

    fun file(fileName: String, content: String) {
        files.put(fileName, content)
    }

    fun file(fileName: String, content: () -> String) {
        files.put(fileName, providers.provider(content))
    }

    @TaskAction
    protected fun writeFiles() {
        val dir = outputDir.get().asFile
        for ((fileName, content) in files.get()) {
            dir.resolve(fileName).writeText(content)
        }
    }
}
