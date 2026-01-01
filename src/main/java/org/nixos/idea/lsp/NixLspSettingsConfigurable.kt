package org.nixos.idea.lsp

import com.intellij.json.JsonFileType
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.platform.lsp.api.LspServerManager
import com.intellij.ui.EditorTextField
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.LabelPosition
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import com.intellij.ui.dsl.builder.toMutableProperty
import org.nixos.idea.settings.ui.CommandSuggestionsPopup
import org.nixos.idea.settings.ui.UiDslExtensions.bindText
import org.nixos.idea.settings.ui.UiDslExtensions.placeholderText
import org.nixos.idea.settings.ui.UiDslExtensions.suggestionsPopup
import org.nixos.idea.settings.ui.UiDslExtensions.validateOnReset
import org.nixos.idea.settings.ui.UiDslExtensions.validateWhenTextChanged
import org.nixos.idea.settings.ui.UiDslExtensions.warnOnInput
import java.awt.Dimension

class NixLspSettingsConfigurable(val project: Project) :
    BoundSearchableConfigurable("Language Server (LSP)", "org.nixos.idea.lsp.NixLspSettingsConfigurable"),
    Configurable.Beta {

    override fun createPanel() = panel {
        val settings = NixLspSettings.getInstance(project)
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
            row {
                cell(createJsonEditorTextField())
                    .align(Align.FILL)
                    .label("Workspace configuration:", LabelPosition.TOP)
                    .bind(
                        EditorTextField::getText, EditorTextField::setText,
                        settings::configuration.toMutableProperty()
                    )
            }.resizableRow()
        }.enabledIf(enabledCheckBox.selected)
    }

    @Suppress("UnstableApiUsage")
    override fun apply() {
        super.apply()
        reset() // Update UI components to use normalized property values
        LspServerManager.getInstance(project).stopAndRestartIfNeeded(NixLspServerSupportProvider::class.java)
    }

    private fun NixLspSettingsConfigurable.createJsonEditorTextField(): EditorTextField {
        val editorTextField = object : EditorTextField(null, project, JsonFileType.INSTANCE, false, false) {
            override fun createEditor(): EditorEx {
                val editor = super.createEditor()
                editor.settings.isUseSoftWraps = true
                return editor
            }
        }
        return editorTextField
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
