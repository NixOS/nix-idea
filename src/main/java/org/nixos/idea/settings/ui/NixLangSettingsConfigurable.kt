package org.nixos.idea.settings.ui

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.Configurable
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.MAX_LINE_LENGTH_WORD_WRAP
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import org.nixos.idea.settings.NixExternalFormatterSettings
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
            lateinit var enabledCheckBox: Cell<JBCheckBox>
            row {
                enabledCheckBox = checkBox("Enable external formatter")
                    .bindSelected(settings::isFormatEnabled)
                    .comment(
                        """
                        Format Nix files via an external formatter.
                        Source of focused file will be passed as standard input.
                        """.trimIndent(),
                        MAX_LINE_LENGTH_WORD_WRAP
                    )
            }
            indent {
                row("Command:") {
                    cell(RawCommandLineEditor())
                        .bindText(settings::formatCommand)
                        .placeholderText("Command to execute for formatting")
                        .suggestionsPopup(settings.commandHistory, BUILTIN_SUGGESTIONS)
                        .align(AlignX.FILL)
                        .validateOnReset()
                        .validateWhenTextChanged()
                        .warnOnInput("You have to specify the command of the formatter") {
                            it.text.isNullOrBlank()
                        }
                }
            }.enabledIf(enabledCheckBox.selected)
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
    )
)
