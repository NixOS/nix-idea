package org.nixos.idea.lang.highlighter;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NixHighlightVisitor implements HighlightVisitor {

    private HighlightInfoHolder myHolder;
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
        try {
            myHolder = holder;
            myDelegate = new Delegate();
            action.run();
        } finally {
            myHolder = null;
            myDelegate = null;
        }
        return true;
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public @NotNull HighlightVisitor clone() {
        return new NixHighlightVisitor();
    }

    private final class Delegate extends NixHighlightVisitorDelegate {
        @Override
        void highlight(@NotNull PsiElement element, @NotNull PsiElement source, @NotNull String attrPath, @Nullable HighlightInfoType type) {
            if (type != null) {
                myHolder.add(HighlightInfo.newHighlightInfo(type)
                        .range(element)
                        .create());
            }
        }
    }
}
