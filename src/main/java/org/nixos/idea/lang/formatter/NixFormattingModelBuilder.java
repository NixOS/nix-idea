package org.nixos.idea.lang.formatter;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.FormattingContext;
import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.formatting.FormattingModelProvider;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.WrapType;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
                        Wrap.createWrap(WrapType.NONE, false),
                        null,
                        NixSpacingUtil.spacingBuilder(globalSettings)
                ),
                globalSettings
        );
    }
}
