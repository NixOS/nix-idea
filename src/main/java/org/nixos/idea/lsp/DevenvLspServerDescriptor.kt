package org.nixos.idea.lsp

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.execution.ParametersListUtil
import org.eclipse.lsp4j.ConfigurationItem
import org.nixos.idea.file.NixFileType

internal class DevenvLspServerDescriptor(project: Project, settings: NixLspSettings) :
    NixLspServerDescriptor(project, settings, "devenv") {

    override val commandString: String
        get() = settings.devenvCommand

    @Throws(ExecutionException::class)
    override fun createCommandLine(): GeneralCommandLine {
        // `devenv lsp` must run in the directory containing the devenv configuration
        return super.createCommandLine().apply {
            project.basePath?.let { withWorkDirectory(it) }
        }
    }

    override fun isSupportedFile(file: VirtualFile): Boolean {
        return file.fileType === NixFileType.INSTANCE && file.name == DEVENV_FILE_NAME
    }

    // nixd ignores the --config option passed by `devenv lsp` and instead requests its
    // configuration from the client via workspace/configuration. Without a proper answer it
    // falls back to completing NixOS options instead of devenv options.
    override fun getWorkspaceConfiguration(item: ConfigurationItem): Any? {
        if (item.section != null && item.section != NIXD_CONFIG_SECTION) return null
        return nixdConfiguration
    }

    private val nixdConfiguration: JsonElement? by lazy {
        try {
            val argv = ParametersListUtil.parse(commandString, false, true) + PRINT_CONFIG_OPTION
            val commandLine = GeneralCommandLine(argv)
            project.basePath?.let { commandLine.withWorkDirectory(it) }
            val output = ExecUtil.execAndGetOutput(commandLine, PRINT_CONFIG_TIMEOUT_MILLIS)
            if (output.exitCode != 0 || output.isTimeout) {
                thisLogger().warn("$commandLine failed (exit code ${output.exitCode}): ${output.stderr}")
                null
            } else {
                JsonParser.parseString(output.stdout).asJsonObject.get(NIXD_CONFIG_SECTION)
            }
        } catch (e: Exception) {
            thisLogger().warn("Failed to obtain nixd configuration from $commandString", e)
            null
        }
    }

    companion object {
        const val DEVENV_FILE_NAME = "devenv.nix"
        private const val NIXD_CONFIG_SECTION = "nixd"
        private const val PRINT_CONFIG_OPTION = "--print-config"
        private const val PRINT_CONFIG_TIMEOUT_MILLIS = 30_000
    }
}
