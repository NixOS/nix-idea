package org.nixos.idea.lsp

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import org.nixos.idea.settings.ui.CommandSuggestionsPopup
import org.nixos.idea.settings.ui.UiDslExtensions.bindText
import org.nixos.idea.settings.ui.UiDslExtensions.placeholderText
import org.nixos.idea.settings.ui.UiDslExtensions.suggestionsPopup
import org.nixos.idea.settings.ui.UiDslExtensions.validateOnReset
import org.nixos.idea.settings.ui.UiDslExtensions.validateWhenTextChanged
import org.nixos.idea.settings.ui.UiDslExtensions.warnOnInput

class NixLspSettingsConfigurable :
    BoundSearchableConfigurable("Language Server (LSP)", "org.nixos.idea.lsp.NixLspSettingsConfigurable"),
    Configurable.Beta {

    override fun createPanel() = panel {
        val settings = NixLspSettings.getInstance()
        lateinit var enabledCheckBox: Cell<JBCheckBox>
        row {
            enabledCheckBox = checkBox("Enable language server")
                .bindSelected(settings::isEnabled)
        }
        group("Language Server Configuration") {
            row("Command:") {
                cell(RawCommandLineEditor())
                    .bindText(settings::command)
                    .placeholderText("Command to start Language Server")
                    .suggestionsPopup(settings.commandHistory, BUILTIN_SUGGESTIONS)
                    .align(AlignX.FILL)
                    .validateOnReset()
                    .validateWhenTextChanged()
                    .warnOnInput("You have to specify the command to start the Language Server") {
                        it.text.isNullOrBlank()
                    }
            }
        }.enabledIf(enabledCheckBox.selected)
    }

    @Suppress("UnstableApiUsage")
    override fun apply() {
        super.apply()
        reset() // Update UI components to use normalized property values
        for (project in ProjectManager.getInstance().openProjects) {
            LspServerManager.getInstance(project).stopAndRestartIfNeeded(NixLspServerSupportProvider::class.java)
        }
    }
}

private val BUILTIN_SUGGESTIONS: List<CommandSuggestionsPopup.Suggestion> = listOf(
    CommandSuggestionsPopup.Suggestion.builtin(
        "<html>Use <b>nil</b> from nixpkgs</html>",
        "nix --extra-experimental-features \"nix-command flakes\" run nixpkgs#nil"
    ),
    CommandSuggestionsPopup.Suggestion.builtin(
        "<html>Use <b>nixd</b> from nixpkgs</html>",
        "nix --extra-experimental-features \"nix-command flakes\" run nixpkgs#nixd"
    )
)
