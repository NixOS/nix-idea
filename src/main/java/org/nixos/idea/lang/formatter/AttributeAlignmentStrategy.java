package org.nixos.idea.lang.formatter;

import com.intellij.formatting.Alignment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.psi.NixBindAttr;
import org.nixos.idea.psi.NixPsiElement;
import org.nixos.idea.settings.NixCodeStyleSettings;

import java.util.NoSuchElementException;

enum AttributeAlignmentStrategy {
    DO_NOT_ALIGN(NixCodeStyleSettings.AttributeAlignment.DO_NOT_ALIGN),
    ALIGN_CONSECUTIVE(NixCodeStyleSettings.AttributeAlignment.ALIGN_CONSECUTIVE),
    ALIGN_SIBLINGS(NixCodeStyleSettings.AttributeAlignment.ALIGN_SIBLINGS),
    ALIGN_NESTED(NixCodeStyleSettings.AttributeAlignment.ALIGN_NESTED),

    ;

    private final @NixCodeStyleSettings.AttributeAlignment int myId;

    AttributeAlignmentStrategy(@NixCodeStyleSettings.AttributeAlignment int id) {
        myId = id;
    }

    static @NotNull AttributeAlignmentStrategy of(@NixCodeStyleSettings.AttributeAlignment int id) {
        for (AttributeAlignmentStrategy strategy : values()) {
            if (strategy.myId == id) {
                return strategy;
            }
        }
        throw new NoSuchElementException("Unknown alignment strategy: " + id);
    }

    @Nullable Alignment createAlignment(@Nullable Alignment parent) {
        switch (this) {
            case DO_NOT_ALIGN:
                return null;
            case ALIGN_NESTED:
                if (parent != null) {
                    return parent;
                }
                // fallthrough
            default:
                return Alignment.createAlignment(true);
        }
    }

    @NotNull Alignment updateAlignment(@NotNull Alignment previousAlignment, @Nullable NixBindAttr previousBinding, @NotNull NixBindAttr currentBinding) {
        switch (this) {
            case ALIGN_CONSECUTIVE:
                if (previousBinding != null && isSeparationBetween(previousBinding, currentBinding)) {
                    return Alignment.createAlignment(true);
                }
                // fallthrough
            default:
                return previousAlignment;
        }
    }

    private static boolean isSeparationBetween(@NotNull NixPsiElement before, @NotNull NixPsiElement after) {
        if (PsiTreeUtil.skipWhitespacesAndCommentsForward(before) == after) {
            int lineFeedCount = 0;
            for (PsiElement c = before.getNextSibling(); c != after; c = c.getNextSibling()) {
                if (c.textContains('\n') && ++lineFeedCount > 1) {
                    return true;
                }
            }
        }
        return false;
    }
}
