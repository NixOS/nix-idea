package org.nixos.idea.util;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.psi.NixPsiElement;

/**
 * Factory methods to construct {@link TextRange} instances from PSI elements.
 */
public final class TextRangeFactory {

    private TextRangeFactory() {
        // Cannot be instantiated
    }

    /**
     * Creates {@link TextRange} for an element relative to itself.
     *
     * @param element The element for which to create the range.
     * @return The range with an offset of zero and a length of the given element.
     */
    public static @NotNull TextRange root(@NotNull NixPsiElement element) {
        return TextRange.from(0, element.getTextLength());
    }

    /**
     * Creates {@link TextRange} for an element relative to the given parent.
     *
     * @param element The element for which to create the range.
     * @param parent  The parent element which becomes the reference frame for the returned range.
     * @return The range of the given {@code element} relative to the given {@code parent}.
     */
    public static @NotNull TextRange relative(@NotNull NixPsiElement element, @NotNull NixPsiElement parent) {
        assert isChild(element, parent) : element + " not a child of " + parent;
        int offset = element.getNode().getStartOffset() - parent.getNode().getStartOffset();
        return TextRange.from(offset, element.getTextLength());
    }

    private static boolean isChild(@NotNull NixPsiElement child, @NotNull NixPsiElement parent) {
        for (PsiElement current = child; current != null; current = current.getParent()) {
            if (current == parent) {
                return true;
            }
        }
        return false;
    }

}
