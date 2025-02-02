package org.nixos.idea.settings.ui

import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.bind
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.selected
import org.nixos.idea.settings.NixSymbolSettings

class NixSymbolConfigurable :
    BoundSearchableConfigurable("Nix Symbols", "org.nixos.idea.settings.ui.NixSymbolConfigurable"),
    Configurable.Beta {

    override fun createPanel(): DialogPanel {
        val settings = NixSymbolSettings.getInstance()
        lateinit var enabledCheckBox: Cell<JBCheckBox>
        return panel {
            row {
                enabledCheckBox = checkBox("Use Symbol API to resolve references and find usages")
                    .bindSelected(settings::enabled)
            }
            rowsRange {
                groupRowsRange("Go To Declaration") {
                    buttonsGroup {
                        row {
                            radioButton("Go to first declaration", true)
                            radioButton("Ask when symbol has multiple declarations", false)
                        }.rowComment(
                            """
                            Attribute sets and <code>let</code>-expressions may contain
                            multiple indirect declarations of the same symbol.
                            """.trimIndent()
                        ).contextHelp(
                            """
                            The following code block contains three
                            declarations of “<code>common</code>”:
                            <pre>
                            let
                              zero = 0;
                              common.a = 1;
                              common.b = 2;
                              common.c = 3;
                            in
                              common
                            </pre>
                            If you run <em>Go To Declaration</em> on the last line,
                            this setting defines whether
                            the action jumps directly to <code>common.a</code>
                            (the first declaration),
                            or opens a popup asking which declaration you want to see.
                            """.trimIndent()
                        )
                    }.bind(settings::jumpToFirstDeclaration)
                }
                groupRowsRange("Find Usages") {
                    row {
                        checkBox("Show declarations as part of the results")
                            .bindSelected(settings::showDeclarationsAsUsages)
                    }
                }
            }.enabledIf(enabledCheckBox.selected)
        }
    }
}
