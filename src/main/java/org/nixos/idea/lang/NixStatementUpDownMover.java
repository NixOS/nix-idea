package org.nixos.idea.lang;

import com.intellij.codeInsight.editorActions.moveUpDown.LineMover;
import com.intellij.codeInsight.editorActions.moveUpDown.LineRange;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.file.NixFile;

import java.util.Set;

/**
 * Implement handling of <em>Code | Move Statement Up/Down</em> for the Nix Language.
 *
 * @see <a href="https://plugins.jetbrains.com/docs/intellij/additional-minor-features.html#move-statements-up-and-down-in-the-editor">Move Statement Documentation</a>
 */
public final class NixStatementUpDownMover extends LineMover {
    @Override
    public boolean checkAvailable(@NotNull Editor editor, @NotNull PsiFile file, @NotNull MoveInfo info, boolean down) {
        // Must return true if this instance shall handle the move.
        // If the method returns true, it must set the MoveInfo.toMove and MoveInfo.toMove2.
        // The two line ranges set on MoveInfo will be swapped after this method returns.
        if (!(file instanceof NixFile)) {
            return false;
        }
        if (!super.checkAvailable(editor, file, info, down)) {
            return false;
        }

        Pair<PsiElement, PsiElement> range = getElementRange(editor, file, info.toMove);
        if (range == null) {
            return false;
        }

        PsiElement commonParent = findCommonParentStrict(range.getFirst(), range.getSecond());
        if (commonParent == null) {
            return false;
        }

        PsiElement first = PsiTreeUtil.findPrevParent(commonParent, range.getFirst());
        PsiElement last = PsiTreeUtil.findPrevParent(commonParent, range.getSecond());
        while (commonParent != null) {
            if (tryMoveSubElements(editor, commonParent, first, last, info, down)) {
                return true;
            }
            if (tryMoveStatements(editor, commonParent, first, last, info, down)) {
                return true;
            }

            first = last = commonParent;
            commonParent = commonParent.getParent();
        }

        // We don't fall back to the default behavior.
        // If we don't know how to move the lines in a meaningful way, we will not move them.
        info.prohibitMove();
        return true;
    }

    /**
     * Try moving elements from list-like elements as reported by {@link NixMoveElementLeftRightHandler}.
     */
    private boolean tryMoveSubElements(
            @NotNull Editor editor,
            @NotNull PsiElement commonParent,
            @NotNull PsiElement first,
            @NotNull PsiElement last,
            @NotNull MoveInfo info,
            boolean down
    ) {
        first = expandLine(first, true);
        last = expandLine(last, false);
        if (first == null || last == null) {
            return false;
        }

        Set<PsiElement> subElements = Set.of(NixMoveElementLeftRightHandler.getMovableSubElements0(commonParent));
        if (subElements.isEmpty()) {
            return false;
        }

        PsiElement afterLast = PsiTreeUtil.skipWhitespacesForward(last);
        for (PsiElement child = first; child != afterLast; child = PsiTreeUtil.skipWhitespacesForward(child)) {
            if (!subElements.contains(child) && !(child instanceof PsiComment)) {
                return false;
            }
        }

        if (tryMoveOverBlankLine(editor, info, first, last, down)) {
            return true;
        }

        PsiElement next = down ? afterLast : PsiTreeUtil.skipWhitespacesBackward(first);
        if (next == null) {
            info.prohibitMove();
            return true;
        }
        PsiElement nextFirst = expandLine(next, true);
        PsiElement nextLast = expandLine(next, false);
        if (nextFirst == null || nextLast == null) {
            info.prohibitMove();
            return true;
        }

        PsiElement afterNextLast = PsiTreeUtil.skipWhitespacesForward(nextLast);
        for (PsiElement child = nextFirst; child != afterNextLast; child = PsiTreeUtil.skipWhitespacesForward(child)) {
            if (!subElements.contains(child) && !(child instanceof PsiComment)) {
                info.prohibitMove();
                return true;
            }
        }

        return tryMoveOver(editor, info, first, last, nextFirst, nextLast);
    }

    /**
     * Try moving expressions up or down in the tree.
     * For example moving an assertion into the body of a let-expression.
     */
    private boolean tryMoveStatements(
            @NotNull Editor editor,
            @NotNull PsiElement commonParent,
            @NotNull PsiElement first,
            @NotNull PsiElement last,
            @NotNull MoveInfo info,
            boolean down
    ) {
        // TODO ...
        return false;
    }

    /**
     * Move the given range one line up/down if the crossed line is empty.
     *
     * @return {@code true} if the line to cross was empty and {@code info} has been adjusted accordingly.
     */
    private static boolean tryMoveOverBlankLine(
            @NotNull Editor editor,
            @NotNull MoveInfo info,
            @NotNull PsiElement first,
            @NotNull PsiElement last,
            boolean down
    ) {
        Document document = editor.getDocument();
        LineRange range = createRange(document, info.toMove, first, last);

        int target = down ? range.endLine : range.startLine - 1;
        int lineStartOffset = document.getLineStartOffset(target);
        int lineEndOffset = document.getLineEndOffset(target);
        if (!CharArrayUtil.isEmptyOrSpaces(document.getCharsSequence(), lineStartOffset, lineEndOffset)) {
            // Would probably be better to check the tokens reported by the lexer instead of using the condition above.
            // With the current implementation, we may have inconsistent identification of whitespaces.
            // However, it shouldn't really matter and this was much easier to implement.
            return false;
        }

        info.toMove = range;
        info.toMove2 = new LineRange(target, target + 1);
        return true;
    }

    /**
     * Move the given lines.
     * The line range defined by {@code move1First} and {@code move1Last} will be swapped
     * with the line range defined by {@code move2First} and {@code move2Last}.
     *
     * @return {@code true}
     */
    private static boolean tryMoveOver(
            @NotNull Editor editor,
            @NotNull MoveInfo info,
            @NotNull PsiElement move1First,
            @NotNull PsiElement move1Last,
            @NotNull PsiElement move2First,
            @NotNull PsiElement move2Last
    ) {
        Document document = editor.getDocument();
        info.toMove = createRange(document, info.toMove, move1First, move1Last);
        info.toMove2 = createRange(document, null, move2First, move2Last);
        assert Integer.max(info.toMove.startLine, info.toMove2.endLine) > Integer.min(info.toMove.endLine, info.toMove2.startLine)
                : "Overlapping ranges";
        return true;
    }

    private static @NotNull LineRange createRange(
            @NotNull Document document, @Nullable LineRange selection,
            @NotNull PsiElement firstElement, @NotNull PsiElement lastElement
    ) {
        CharSequence text = document.getCharsSequence();

        int startOffset = firstElement.getTextRange().getStartOffset();
        int firstLine = document.getLineNumber(startOffset);
        if (selection != null) {
            firstLine = Integer.min(firstLine, selection.startLine);
        }
        if (!CharArrayUtil.isEmptyOrSpaces(text, document.getLineStartOffset(firstLine), startOffset)) {
            throw new IllegalArgumentException("firstElement is not the first element");
        }

        int endOffset = lastElement.getTextRange().getEndOffset();
        int lastLine = document.getLineNumber(endOffset);
        if (selection != null) {
            lastLine = Integer.max(lastLine, selection.endLine - 1);
        }
        if (!CharArrayUtil.isEmptyOrSpaces(text, endOffset, document.getLineEndOffset(lastLine))) {
            throw new IllegalArgumentException("lastElement is not the last element");
        }

        return new LineRange(firstLine, lastLine + 1);
    }

    private static @Nullable PsiElement findCommonParentStrict(@NotNull PsiElement element1, @NotNull PsiElement element2) {
        PsiElement commonParent = PsiTreeUtil.findCommonParent(element1, element2);
        if (commonParent == element1 || commonParent == element2) {
            return commonParent.getParent();
        } else {
            return commonParent;
        }
    }

    private static PsiElement expandLine(@NotNull PsiElement element, boolean left) {
        while (true) {
            PsiElement next = left ? element.getPrevSibling() : element.getNextSibling();
            if (next == null) {
                // TODO Cover case when a newline is before the first element
                return null;
            } else if (next instanceof PsiWhiteSpace && next.textContains('\n')) {
                return element;
            }
            element = next;
        }
    }
}
