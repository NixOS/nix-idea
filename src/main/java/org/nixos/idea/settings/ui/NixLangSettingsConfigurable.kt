package org.nixos.idea.settings.ui

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.MAX_LINE_LENGTH_WORD_WRAP
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import com.intellij.ui.dsl.builder.toMutableProperty
import org.nixos.idea.lsp.ui.CommandSuggestionsPopup
import org.nixos.idea.settings.NixExternalFormatterSettings

class NixLangSettingsConfigurable :
    BoundSearchableConfigurable("Nix", "org.nixos.idea.settings.ui.NixLangSettingsConfigurable"),
    Configurable.Beta {

    override fun createPanel(): DialogPanel {
        return panel {
            group("External Formatter", indent = false) {
                val settings = NixExternalFormatterSettings.getInstance()
                lateinit var enabledCheckBox: Cell<JBCheckBox>
                row {
                    enabledCheckBox = checkBox("Enable external formatter").bindSelected(
                        settings::isFormatEnabled
                    ).comment(
                        """
                        Format Nix files via an external formatter.
                        Source of focused file will be passed as standard input.
                        """.trimIndent(),
                        MAX_LINE_LENGTH_WORD_WRAP
                    )
                }
                indent {
                    row("Command:") {
                        cell(RawCommandLineEditor()).applyToComponent {
                            editorField.emptyText.setText("Command to execute for formatting")
                            editorField.accessibleContext.accessibleName = "Command to execute for formatting"
                            CommandSuggestionsPopup(
                                this,
                                settings.commandHistory,
                                BUILTIN_SUGGESTIONS
                            ).install()
                        }.bind(
                            RawCommandLineEditor::getText,
                            RawCommandLineEditor::setText,
                            settings::formatCommand.toMutableProperty()
                        ).align(AlignX.FILL)
                    }
                }.enabledIf(enabledCheckBox.selected)
            }
        }
    }
}

private val BUILTIN_SUGGESTIONS: List<CommandSuggestionsPopup.Suggestion> = listOf(
    CommandSuggestionsPopup.Suggestion.builtin(
        "<html>Use <b>nixpkgs-fmt</b> from nixpkgs</html>",
        "nix --extra-experimental-features \"nix-command flakes\" run nixpkgs#nixpkgs-fmt"
    )
)
