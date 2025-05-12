package org.nixos.idea.lang.formatter

import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.FormattingModelBuilder
import com.intellij.formatting.FormattingModelProvider
import com.intellij.util.lazyPub
import org.nixos.idea.lang.formatter.dsl.FormatterDefinition
import org.nixos.idea.lang.formatter.rules.AttributeAlignmentFormatter
import org.nixos.idea.lang.formatter.rules.CollectionFormatter
import org.nixos.idea.lang.formatter.rules.DefaultFormatter
import org.nixos.idea.lang.formatter.rules.IfFormatter

/**
 * Entry point for formatting of the Nix Language.
 *
 * @see [Code Formatter Documentation](https://plugins.jetbrains.com/docs/intellij/code-formatting.html)
 */
class NixFormattingModelBuilder : FormattingModelBuilder {
    private val formatterDefinition by lazyPub {
        FormatterDefinition(
            rules = arrayOf(
                // Apply DefaultFormatter first. Other rules may overwrite it
                DefaultFormatter,
                AttributeAlignmentFormatter,
                CollectionFormatter,
                IfFormatter,
            )
        )
    }

    override fun createModel(context: FormattingContext): FormattingModel {
        val node = context.getNode()
        val globalSettings = context.getCodeStyleSettings()
        val result = formatterDefinition.process(node, globalSettings)
        return FormattingModelProvider.createFormattingModelForPsiFile(
            context.getContainingFile(),
            NixBlock(node, NixSpacingUtil.spacingBuilder(globalSettings), result),
            globalSettings
        )
    }
}
