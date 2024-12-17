package org.nixos.idea.lang;

import com.intellij.codeInsight.editorActions.moveUpDown.LineMover;
import com.intellij.codeInsight.editorActions.moveUpDown.LineRange;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.file.NixFile;
import org.nixos.idea.psi.NixStatementLike;
import org.nixos.idea.psi.NixTypes;

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

        PsiElement first = PsiTreeUtil.getDeepestFirst(range.getFirst());
        PsiElement last = PsiTreeUtil.getDeepestLast(range.getSecond());
        PsiElement commonParent = findCommonParentStrict(first, last);
        if (commonParent == null) {
            return false;
        }

        StatementLikeHandler statementHandler = new StatementLikeHandler();
        for (PsiElement parent = first.getParent(); parent != commonParent; parent = first.getParent()) {
            statementHandler.firstPart(first, parent);
            first = parent;
        }
        for (PsiElement parent = last.getParent(); parent != commonParent; parent = last.getParent()) {
            statementHandler.lastPart(last, parent);
            last = parent;
        }

        while (commonParent != null) {
            if (tryMoveSubElements(editor, commonParent, first, last, info, down)) {
                return true;
            }
            if (statementHandler.tryMove(editor, commonParent, first, last, info, down)) {
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
     * Handler for moving expressions up or down in the tree.
     * For example moving an assertion into the body of a let-expression.
     */
    private static final class StatementLikeHandler {
        // I don't really like the implementation of this class.
        // It is full of implicit assumptions about the syntax of the Nix expression language.
        // Anyway, I did not have a good idea how to make it better and at least we have decent test coverage.

        private static final TokenSet STATEMENT_END_TOKENS = TokenSet.create(NixTypes.SEMI, NixTypes.IN, NixTypes.COLON);
        private static final TokenSet INSERTION_POINT_TOKENS = TokenSet.orSet(STATEMENT_END_TOKENS, TokenSet.create(NixTypes.THEN, NixTypes.ELSE));

        private @Nullable PsiElement myFirst;
        private @Nullable PsiElement myLast;
        private @Nullable PsiElement myNextInsertionPoint;

        private record JumpRange(@NotNull PsiElement lastOfRange, @Nullable PsiElement insertionPoint) {}

        private void firstPart(@NotNull PsiElement first, @NotNull PsiElement parent) {
            if (!(parent instanceof NixStatementLike)) {
                myFirst = null;
            } else if (myFirst == null) {
                myFirst = findStatementStartLine(parent);
            }
        }

        private void lastPart(@NotNull PsiElement last, @NotNull PsiElement parent) {
            if (!(parent instanceof NixStatementLike)) {
                myLast = null;
                myNextInsertionPoint = null;
            } else {
                JumpRange end = findSelectionEnd(parent, last);
                if (end != null) {
                    myLast = end.lastOfRange();
                    myNextInsertionPoint = end.insertionPoint();
                } else {
                    myLast = null;
                    myNextInsertionPoint = null;
                }
            }
        }

        private boolean tryMove(
                @NotNull Editor editor,
                @NotNull PsiElement commonParent,
                @NotNull PsiElement first,
                @NotNull PsiElement last,
                @NotNull MoveInfo info,
                boolean down
        ) {
            if (!(commonParent instanceof NixStatementLike)) {
                if (myFirst != null && myLast != null) {
                    // We do not want to move statements much further up in the tree
                    // if the user has selected statements inside the current subtree.
                    info.prohibitMove();
                    return true;
                } else {
                    myFirst = null;
                    myLast = null;
                    myNextInsertionPoint = null;
                    return false;
                }
            }

            firstPart(first, commonParent);
            if (first != last || myLast == null) {
                lastPart(last, commonParent);
            }

            if (myFirst == null || myLast == null) {
                return false;
            }
            if (tryMoveOverBlankLine(editor, info, myFirst, myLast, down)) {
                return true;
            }

            if (down) {
                PsiElement insertionPoint = myNextInsertionPoint;
                if (insertionPoint == null && first == last) {
                    insertionPoint = findNextInsertionPoint(commonParent, last);
                }
                if (insertionPoint != null && insertionPoint != myLast) {
                    PsiElement next = nextLeaf(myLast);
                    if (next == null) {
                        assert false : "Should be unreachable";
                    } else {
                        return tryMoveOver(editor, info, myFirst, myLast, next, insertionPoint);
                    }
                }
            } else if (first == last) {
                PsiElement insertionPoint = findPreviousInsertionPoint(commonParent, first);
                if (insertionPoint != null) {
                    PsiElement previous = prevLeaf(myFirst);
                    if (previous == null) {
                        assert false : "Should be unreachable";
                    } else {
                        return tryMoveOver(editor, info, myFirst, myLast, insertionPoint, previous);
                    }
                }
                PsiElement startOfLine = findStatementStartLine(commonParent);
                if (startOfLine != null && startOfLine != myFirst) {
                    PsiElement previous = prevLeaf(myFirst);
                    if (previous == null) {
                        assert false : "Should be unreachable";
                    } else {
                        return tryMoveOver(editor, info, myFirst, myLast, startOfLine, previous);
                    }
                }
            }
            return false;
        }

        private @Nullable PsiElement findStatementStartLine(@NotNull PsiElement statement) {
            if (!(statement instanceof NixStatementLike)) {
                return null;
            }

            PsiElement current = statement;
            while (true) {
                PsiElement previous = current.getPrevSibling();
                if (previous == null) {
                    PsiElement parent = current.getParent();
                    if (parent == null || parent instanceof PsiFile) {
                        return current;
                    } else {
                        current = parent;
                    }
                } else if (previous instanceof PsiWhiteSpace) {
                    if (previous.textContains('\n')) {
                        return current;
                    } else {
                        current = previous;
                    }
                } else if (previous instanceof PsiComment) {
                    current = previous;
                } else {
                    return null;
                }
            }
        }

        private @Nullable JumpRange findSelectionEnd(@NotNull PsiElement parent, @Nullable PsiElement lastSelected) {
            if (!(parent instanceof NixStatementLike)) {
                return null;
            }

            ASTNode stmtEndToken = parent.getNode().findChildByType(STATEMENT_END_TOKENS);
            if (stmtEndToken == null) {
                return null;
            }

            PsiElement stmtEnd = stmtEndToken.getPsi();
            PsiElement next = PsiTreeUtil.skipWhitespacesAndCommentsForward(stmtEnd);
            PsiElement endOfLine = expandLine(stmtEnd, false);
            if (next == null) {
                return null;
            }
            if (lastSelected != null && endOfLine != null && lastSelected.getStartOffsetInParent() > endOfLine.getStartOffsetInParent()) {
                endOfLine = expandLine(lastSelected, false);
            }

            if (endOfLine != null && next.getStartOffsetInParent() > endOfLine.getStartOffsetInParent()) {
                return new JumpRange(endOfLine, findNextInsertionPoint(next, null));
            } else if (next == lastSelected) {
                // Optimization: `lastSelected` has already been processed
                return myLast == null ? null : new JumpRange(myLast, myNextInsertionPoint);
            } else {
                return findSelectionEnd(next, null);
            }
        }

        private @Nullable PsiElement findPreviousInsertionPoint(@NotNull PsiElement parent, @Nullable PsiElement before) {
            if (!(parent instanceof NixStatementLike)) {
                return null;
            }

            PsiElement start = before == null ? parent.getLastChild() :
                    PsiTreeUtil.skipWhitespacesBackward(PsiTreeUtil.skipWhitespacesAndCommentsBackward(before));
            for (PsiElement cur = start; cur != null; cur = PsiTreeUtil.skipWhitespacesBackward(cur)) {
                if (INSERTION_POINT_TOKENS.contains(cur.getNode().getElementType())) {
                    PsiElement childExpression = PsiTreeUtil.skipWhitespacesAndCommentsForward(cur);
                    if (childExpression == null /* || childExpression == myFirst */) {
                        return null;
                    }

                    PsiElement insertionPoint = findPreviousInsertionPoint(childExpression, null);
                    if (insertionPoint != null) {
                        return insertionPoint;
                    }

                    PsiElement startOfLine = expandLine(childExpression, true);
                    if (startOfLine != null && startOfLine.getStartOffsetInParent() > cur.getStartOffsetInParent()) {
                        return startOfLine;
                    }
                }
            }
            return null;
        }

        private static @Nullable PsiElement findNextInsertionPoint(@NotNull PsiElement parent, @Nullable PsiElement after) {
            if (!(parent instanceof NixStatementLike)) {
                return null;
            }

            PsiElement first = after == null ? parent.getFirstChild() : PsiTreeUtil.skipWhitespacesForward(after);
            for (PsiElement cur = first; cur != null; cur = PsiTreeUtil.skipWhitespacesForward(cur)) {
                if (INSERTION_POINT_TOKENS.contains(cur.getNode().getElementType())) {
                    PsiElement next = PsiTreeUtil.skipWhitespacesAndCommentsForward(cur);
                    PsiElement endOfLine = expandLine(cur, false);
                    if (next == null || endOfLine == null) {
                        return null;
                    }

                    if (next.getStartOffsetInParent() > endOfLine.getStartOffsetInParent()) {
                        return endOfLine;
                    } else {
                        return findNextInsertionPoint(next, null);
                    }
                }
            }
            return null;
        }
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
        if (target < 0 || target > document.getLineCount()) {
            return false;
        }
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

    private static @Nullable PsiElement expandLine(@NotNull PsiElement element, boolean left) {
        while (true) {
            PsiElement next = left ? element.getPrevSibling() : element.getNextSibling();
            if (next == null) {
                PsiElement nextLeaf = left ? PsiTreeUtil.prevLeaf(element) : PsiTreeUtil.nextLeaf(element);
                if (nextLeaf == null || nextLeaf instanceof PsiWhiteSpace && nextLeaf.textContains('\n')) {
                    return element;
                } else {
                    return null;
                }
            } else if (next instanceof PsiWhiteSpace) {
                if (next.textContains('\n')) {
                    return element;
                }
            }
            element = next;
        }
    }

    private static @Nullable PsiElement prevLeaf(@NotNull PsiElement element) {
        PsiElement previous = PsiTreeUtil.prevLeaf(element, true);
        while (previous instanceof PsiWhiteSpace) {
            previous = PsiTreeUtil.prevLeaf(previous, true);
        }
        return previous;
    }

    private static @Nullable PsiElement nextLeaf(@NotNull PsiElement element) {
        PsiElement previous = PsiTreeUtil.nextLeaf(element, true);
        while (previous instanceof PsiWhiteSpace) {
            previous = PsiTreeUtil.nextLeaf(previous, true);
        }
        return previous;
    }
}
