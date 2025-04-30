package org.nixos.idea.lang.formatter;

import com.intellij.formatting.FormattingContext;
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.formatting.FormattingModelProvider;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;

/**
 * Entry point for formatting of the Nix Language.
 *
 * @see <a href="https://plugins.jetbrains.com/docs/intellij/code-formatting.html">Code Formatter Documentation</a>
 */
public final class NixFormattingModelBuilder implements FormattingModelBuilder {
    @Override
    public @NotNull FormattingModel createModel(@NotNull FormattingContext context) {
        CodeStyleSettings globalSettings = context.getCodeStyleSettings();
        return FormattingModelProvider.createFormattingModelForPsiFile(
                context.getContainingFile(),
                new NixBlock(
                        context.getNode(),
                        globalSettings
                ),
                globalSettings
        );
    }
}
