package org.nixos.idea.lsp

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import com.intellij.ui.dsl.builder.toMutableProperty
import org.nixos.idea.lsp.ui.CommandSuggestionsPopup

class NixLspSettingsConfigurable :
    BoundSearchableConfigurable("Language Server (LSP)", "org.nixos.idea.lsp.NixLspSettingsConfigurable"),
    Configurable.Beta {

    override fun createPanel(): DialogPanel {
        val settings = NixLspSettings.getInstance()
        lateinit var enabledCheckBox: Cell<JBCheckBox>
        return panel {
            row {
                enabledCheckBox = checkBox("Enable language server")
                    .bindSelected(settings::isEnabled)
            }
            groupRowsRange("Language Server Configuration") {
                row("Command:") {
                    cell(RawCommandLineEditor()).applyToComponent {
                        editorField.emptyText.setText("Command to start Language Server")
                        editorField.accessibleContext.accessibleName = "Command to start Language Server"
                        // TODO What about the following line?
                        //editorField.margin = myEnabled!!.margin
                        CommandSuggestionsPopup(
                            this,
                            settings.commandHistory,
                            BUILTIN_SUGGESTIONS
                        ).install()
                    }.bind(
                        RawCommandLineEditor::getText,
                        RawCommandLineEditor::setText,
                        settings::command.toMutableProperty()
                    ).align(AlignX.FILL)
                }
            }.enabledIf(enabledCheckBox.selected)
        }
    }

    @Suppress("UnstableApiUsage")
    override fun apply() {
        super.apply()
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
