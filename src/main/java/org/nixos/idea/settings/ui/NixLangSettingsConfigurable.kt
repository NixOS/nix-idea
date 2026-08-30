package org.nixos.idea.settings.ui

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.Configurable
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.MAX_LINE_LENGTH_WORD_WRAP
import com.intellij.ui.dsl.builder.bind
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import com.intellij.ui.layout.and
import org.nixos.idea.settings.NixExternalFormatterSettings
import org.nixos.idea.settings.NixExternalFormatterSettings.InputMode
import org.nixos.idea.settings.NixExternalFormatterSettings.Strategy
import org.nixos.idea.settings.ui.UiDslExtensions.bindText
import org.nixos.idea.settings.ui.UiDslExtensions.placeholderText
import org.nixos.idea.settings.ui.UiDslExtensions.suggestionsPopup
import org.nixos.idea.settings.ui.UiDslExtensions.validateOnReset
import org.nixos.idea.settings.ui.UiDslExtensions.validateWhenTextChanged
import org.nixos.idea.settings.ui.UiDslExtensions.warnOnInput

class NixLangSettingsConfigurable :
    BoundSearchableConfigurable("Nix", "org.nixos.idea.settings.ui.NixLangSettingsConfigurable"),
    Configurable.Beta {

    override fun createPanel() = panel {
        group("External Formatter", indent = false) {
            val settings = NixExternalFormatterSettings.getInstance()
            lateinit var formatterEnabled: Cell<JBCheckBox>
            lateinit var customCommandEnabled: Cell<JBRadioButton>
            row {
                formatterEnabled = checkBox("Enable external formatter")
                    .bindSelected(settings::isEnabled)
                    .comment(
                        """
                        Format Nix files via an external formatter.
                        """.trimIndent(),
                        MAX_LINE_LENGTH_WORD_WRAP
                    )
            }
            indent {
                buttonsGroup {
                    row {
                        radioButton("<html>Use <b>nix fmt</b></html>", Strategy.NIX_FMT)
                        customCommandEnabled = radioButton("Use custom command", Strategy.CUSTOM_COMMAND)
                    }
                }.bind(settings::strategy)
                groupRowsRange(indent = false) {
                    row("Command:") {
                        cell(RawCommandLineEditor())
                            .bindText(settings::formatCommand)
                            .placeholderText("Command to execute for formatting")
                            .suggestionsPopup(settings.formatCommandHistory, BUILTIN_SUGGESTIONS)
                            .align(AlignX.FILL)
                            .validateOnReset()
                            .validateWhenTextChanged()
                            .warnOnInput("You have to specify the command of the formatter") {
                                it.text.isNullOrBlank()
                            }
                    }
                    buttonsGroup {
                        row {
                            radioButton("<html>Use <code>stdin</code> and <code>stdout</code></html>", InputMode.STDIO)
                                .comment("Source of the focused file will be passed as standard input.")
                        }
                        row {
                            radioButton("Use CLI argument", InputMode.CLI_ARGUMENT)
                                .comment("Path of the focused file will be specified as an additional argument.")
                        }
                    }.bind(settings::formatCommandInputMode)
                    row {
                        checkBox("Run formatter in the file's parent directory")
                            .bindSelected(settings::isFormatCommandRunInFileDirectory)
                            .comment("Required for <b>nix fmt</b> to work.")
                    }
                }.visibleIf(formatterEnabled.selected and customCommandEnabled.selected)
            }.enabledIf(formatterEnabled.selected)
        }
    }

    override fun apply() {
        super.apply()
        reset() // Update UI components to use normalized property values
    }
}

private val BUILTIN_SUGGESTIONS: List<CommandSuggestionsPopup.Suggestion> = listOf(
    CommandSuggestionsPopup.Suggestion.builtin(
        "<html>Use <b>nixpkgs-fmt</b> from nixpkgs</html>",
        "nix --extra-experimental-features \"nix-command flakes\" run nixpkgs#nixpkgs-fmt"
    ),
    CommandSuggestionsPopup.Suggestion.builtin(
        "<html>Use <b>nix fmt</b> from flake.nix</html>",
        "nix --extra-experimental-features \"nix-command flakes\" fmt"
    )
)
