package org.nixos.idea.lang.highlighter;

import com.intellij.codeInsight.daemon.RainbowVisitor;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class NixRainbowVisitor extends RainbowVisitor {

    public static final List<TextAttributesKey> RAINBOW_ATTRIBUTES = List.of(
            NixTextAttributes.LOCAL_VARIABLE,
            NixTextAttributes.PARAMETER);

    private Delegate myDelegate;

    @Override
    public boolean suitableForFile(@NotNull PsiFile file) {
        return NixHighlightVisitorDelegate.suitableForFile(file);
    }

    @Override
    public void visit(@NotNull PsiElement element) {
        myDelegate.visit(element);
    }

    @Override
    public boolean analyze(@NotNull PsiFile file, boolean updateWholeFile, @NotNull HighlightInfoHolder holder, @NotNull Runnable action) {
        myDelegate = new Delegate(file);
        try {
            return super.analyze(file, updateWholeFile, holder, action);
        } finally {
            myDelegate = null;
        }
    }

    @Override
    public @NotNull HighlightVisitor clone() {
        return new NixRainbowVisitor();
    }

    private final class Delegate extends NixHighlightVisitorDelegate {
        private final @NotNull PsiFile file;

        private Delegate(@NotNull PsiFile file) {
            this.file = file;
        }

        @Override
        void highlight(@NotNull PsiElement element, @Nullable PsiElement source, @NotNull String attrPath, @Nullable HighlightInfoType type) {
            if (type != LITERAL && type != IMPORT && type != BUILTIN) {
                TextAttributesKey attributesKey = type == null ? NixTextAttributes.IDENTIFIER : type.getAttributesKey();
                PsiElement context = source == null ? file : source;
                addInfo(getInfo(context, element, attrPath, attributesKey));
            }
        }
    }
}
