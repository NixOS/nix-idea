package org.nixos.idea.lang;

import com.google.common.collect.Iterables;
import com.intellij.codeInsight.editorActions.moveUpDown.LineMover;
import com.intellij.codeInsight.editorActions.moveUpDown.LineRange;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.file.NixFile;
import org.nixos.idea.psi.NixExprIf;
import org.nixos.idea.psi.NixStatementLike;
import org.nixos.idea.psi.NixTypes;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;

/**
 * Implement handling of <em>Code | Move Statement Up/Down</em> for the Nix Language.
 *
 * @see <a href="https://plugins.jetbrains.com/docs/intellij/additional-minor-features.html#move-statements-up-and-down-in-the-editor">Move Statement Documentation</a>
 */
public final class NixStatementUpDownMover extends LineMover {

    private static final TokenSet STATEMENT_END_TOKENS = TokenSet.create(NixTypes.SEMI, NixTypes.IN, NixTypes.COLON);
    private static final TokenSet INSERTION_POINT_TOKENS = TokenSet.orSet(STATEMENT_END_TOKENS, TokenSet.create(NixTypes.THEN, NixTypes.ELSE));

    @Override
    public boolean checkAvailable(@NotNull Editor editor, @NotNull PsiFile file, @NotNull MoveInfo info, boolean down) {
        // Must return true if this instance shall handle the move.
        // If the method returns true, it must set MoveInfo.toMove and MoveInfo.toMove2.
        // The two line ranges set on MoveInfo will be swapped after this method returns.
        if (!(file instanceof NixFile)) {
            return false;
        }
        if (!super.checkAvailable(editor, file, info, down)) {
            return false;
        }

        Document document = editor.getDocument();
        assert document == file.getFileDocument();
        int startOffset = editor.logicalPositionToOffset(new LogicalPosition(info.toMove.startLine, 0));
        int endOffset = editor.logicalPositionToOffset(new LogicalPosition(info.toMove.endLine, 0));

        MoveRequest request = new MoveRequest(editor, file, document, info, startOffset, endOffset, down);
        tryMoveRecursive(request, file);

        return true;
    }

    private static void tryMoveRecursive(@NotNull MoveRequest request, @NotNull PsiFile file) {
        Deque<ElementInfo> elements = new ArrayDeque<>();

        PsiElement element = file;
        PsiElement lineStart = file;
        PsiElement lineEnd = file;
        int offset = 0;
        while (true) {
            Selection selection = Selection.resolve(request, element, offset);
            if (selection == null) {
                // We have only selected empty lines. Use default behavior of LineMover.
                return;
            }

            ElementInfo elementInfo = new ElementInfo(element, lineStart, lineEnd, selection, offset);
            elements.addFirst(elementInfo);

            element = asCodeElementForward(selection.firstElement());
            if (element == null || element != asCodeElementBackward(selection.lastElement()) || element.getFirstChild() == null) {
                break;
            }
            offset = offset + element.getStartOffsetInParent();
            lineStart = selection.firstOfLine(elementInfo);
            lineEnd = selection.lastOfLine(elementInfo);
        }

        Collection<ElementInfo> unmodifiableView = Collections.unmodifiableCollection(elements);
        while (!elements.isEmpty()) {
            ElementInfo elementInfo = elements.removeFirst();
            if (tryMoveSubElements(request, elementInfo)) {
                return;
            }
            if (tryMoveStatements(request, elementInfo, unmodifiableView)) {
                return;
            }
        }

        // We don't fall back to the default behavior.
        // If we don't know how to move the lines in a meaningful way, we will not move them.
        request.info().prohibitMove();
    }

    /**
     * Try moving elements from list-like elements as reported by {@link NixMoveElementLeftRightHandler}.
     */
    private static boolean tryMoveSubElements(@NotNull MoveRequest request, @NotNull ElementInfo elementInfo) {
        MoveInfo info = request.info();
        PsiElement commonParent = elementInfo.element();
        PsiElement first = expandLine(elementInfo.selection().firstElement(), true);
        PsiElement last = expandLine(elementInfo.selection().lastElement(), false);

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

        if (tryMoveOverBlankLine(request, first, last)) {
            return true;
        }

        PsiElement next = request.down() ? afterLast : PsiTreeUtil.skipWhitespacesBackward(first);
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

        return tryMoveOver(request, first, last, nextFirst, nextLast);
    }

    /**
     * Try moving expressions up or down in the tree.
     * For example moving an assertion into the body of a let-expression.
     */
    private static boolean tryMoveStatements(
            @NotNull MoveRequest request,
            @NotNull ElementInfo elementInfo,
            @NotNull Collection<ElementInfo> parents
    ) {
        BlockOfStatements block = resolveSelectedStatement(request, elementInfo, parents);
        if (block == null) {
            return false;
        }

        if (tryMoveOverBlankLine(request, block.first(), block.last())) {
            return true;
        }

        if (request.down()) {
            PsiElement insertionPoint = null;
            if (block.next() != null) {
                insertionPoint = findNextInsertionPoint(block.next(), null);
            }
            if (insertionPoint == null) {
                insertionPoint = findNextInsertionPoint(block.parents(), block.lastInParent());
            }
            if (insertionPoint != null) {
                // TODO Do I need `next`?
                PsiElement next = nextLeaf(block.last());
                if (next == null) {
                    assert false : "Should be unreachable";
                } else {
                    return tryMoveOver(request, block.first(), block.last(), next, insertionPoint);
                }
            }
        } else {
            PsiElement insertionPoint = findPreviousInsertionPoint(block.parents(), block.firstInParent());
            if (insertionPoint != null) {
                PsiElement previous = prevLeaf(block.first());
                if (previous == null) {
                    assert false : "Should be unreachable";
                } else {
                    return tryMoveOver(request, block.first(), block.last(), insertionPoint, previous);
                }
            }
        }

        // We do not want to move statements much further up in the tree
        // if the user has selected statements inside the current subtree.
        request.info().prohibitMove();
        return true;
    }

    private static @Nullable BlockOfStatements resolveSelectedStatement(
            @NotNull MoveRequest request,
            @NotNull ElementInfo elementInfo,
            @NotNull Collection<ElementInfo> parents
    ) {
        Selection selection = elementInfo.selection();

        PsiElement firstSelected = expandLine(selection.firstElement(), true);
        PsiElement lastSelected = expandLine(selection.lastElement(), false);
        PsiElement next = asCodeElementForward(firstSelected);
        if (lastSelected != null && next != null && next.getStartOffsetInParent() > lastSelected.getStartOffsetInParent()) {
            return new BlockOfStatements(
                    firstSelected, lastSelected, next,
                    Iterables.concat(List.of(elementInfo), parents), firstSelected, lastSelected
            );
        }

        PsiElement lineStart = elementInfo.lineStart();
        if (!(elementInfo.element() instanceof NixStatementLike element) || lineStart == null) {
            return null;
        }

        PsiElement statementTerminator = findStatementTerminator(element);
        next = PsiTreeUtil.skipWhitespacesAndCommentsForward(statementTerminator);
        if (next != null && next == firstSelected) {
            return null;
        }

        PsiElement selectionEnd = findSelectionEnd(request, element, elementInfo.offset(), elementInfo.selection());
        if (selectionEnd == null) {
            return null;
        }

        return new BlockOfStatements(
                lineStart, selectionEnd, PsiTreeUtil.skipWhitespacesAndCommentsForward(selectionEnd),
                parents, element, element
        );
    }

    private static @Nullable PsiElement findSelectionEnd(@NotNull MoveRequest request, @NotNull PsiElement parent, int offset, @Nullable Selection selection) {
        if (!(parent instanceof NixStatementLike stmt)) {
            return null;
        }

        PsiElement stmtEnd = findStatementTerminator(stmt);
        if (stmtEnd == null) {
            return null;
        }

        PsiElement next = PsiTreeUtil.skipWhitespacesAndCommentsForward(stmtEnd);
        PsiElement endOfLine = expandLine(stmtEnd, false);
        if (next == null) {
            return null;
        }

        if (selection != null && endOfLine != null && selection.lastElement().getStartOffsetInParent() > endOfLine.getStartOffsetInParent()) {
            endOfLine = expandLine(selection.lastElement(), false);
        }

        if (endOfLine != null && next.getStartOffsetInParent() > endOfLine.getStartOffsetInParent()) {
            return endOfLine;
        } else if (selection != null && next.getStartOffsetInParent() <= selection.lastElement().getStartOffsetInParent()) {
            int childOffset = offset + next.getStartOffsetInParent();
            return findSelectionEnd(request, next, childOffset, Selection.resolve(request, next, childOffset));
        } else {
            return findSelectionEnd(request, next, offset + next.getStartOffsetInParent(), null);
        }
    }

    private static @Nullable PsiElement findPreviousInsertionPoint(@NotNull Iterable<ElementInfo> parents, @NotNull PsiElement before) {
        for (ElementInfo parent : parents) {
            if (parent.element() instanceof NixExprIf let) {
                PsiElement insertionPoint = findPreviousInsertionPoint(let, before);
                if (insertionPoint != null) {
                    return insertionPoint;
                }
                if (parent.lineStart() != null) {
                    return parent.lineStart();
                }
            } else if (parent.element() instanceof NixStatementLike) {
                if (parent.lineStart() != null) {
                    return parent.lineStart();
                }
            } else {
                // We cannot move our statement out of arbitrary expressions.
                return null;
            }
            before = parent.element();
        }
        return null;
    }

    private static @Nullable PsiElement findPreviousInsertionPoint(@NotNull PsiElement parent, @Nullable PsiElement before) {
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

    private static @Nullable PsiElement findNextInsertionPoint(@NotNull Iterable<ElementInfo> parents, @NotNull PsiElement after) {
        for (ElementInfo parent : parents) {
            if (parent.element() instanceof NixExprIf let) {
                PsiElement insertionPoint = findNextInsertionPoint(let, after);
                if (insertionPoint != null) {
                    return insertionPoint;
                }
            } else if (!(parent.element() instanceof NixStatementLike)) {
                // We cannot move our statement out of arbitrary expressions.
                return null;
            }
            after = parent.element();
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

    private static @Nullable PsiElement findStatementTerminator(@NotNull NixStatementLike element) {
        PsiElement lastElement = element.getLastChild();
        if (STATEMENT_END_TOKENS.contains(PsiUtilBase.getElementType(lastElement))) {
            return lastElement;
        } else {
            PsiElement statementEnd = PsiTreeUtil.skipWhitespacesAndCommentsBackward(lastElement);
            if (!STATEMENT_END_TOKENS.contains(PsiUtilBase.getElementType(statementEnd))) {
                return null;
            }
            return statementEnd;
        }
    }

    /**
     * Move the given range one line up/down if the crossed line is empty.
     *
     * @return {@code true} if the line to cross was empty and {@code info} has been adjusted accordingly.
     */
    private static boolean tryMoveOverBlankLine(
            @NotNull MoveRequest request,
            @NotNull PsiElement first,
            @NotNull PsiElement last
    ) {
        MoveInfo info = request.info();
        Document document = request.document();
        LineRange range = createRange(document, info.toMove, first, last);

        int target = request.down() ? range.endLine : range.startLine - 1;
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
            @NotNull MoveRequest request,
            @NotNull PsiElement move1First,
            @NotNull PsiElement move1Last,
            @NotNull PsiElement move2First,
            @NotNull PsiElement move2Last
    ) {
        MoveInfo info = request.info();
        Document document = request.document();
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

    private static @Nullable PsiElement expandLine(@NotNull PsiElement element, boolean left) {
        while (true) {
            PsiElement next = left ? element.getPrevSibling() : element.getNextSibling();
            if (next == null) {
                // TODO Can we simplify this again?
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

    @Contract("null -> null")
    private static @Nullable PsiElement asCodeElementBackward(@Nullable PsiElement element) {
        if (element instanceof PsiComment) {
            return PsiTreeUtil.skipWhitespacesAndCommentsBackward(element);
        } else {
            return element;
        }
    }

    @Contract("null -> null")
    private static @Nullable PsiElement asCodeElementForward(@Nullable PsiElement element) {
        if (element instanceof PsiComment) {
            return PsiTreeUtil.skipWhitespacesAndCommentsForward(element);
        } else {
            return element;
        }
    }

    private record MoveRequest(
            @NotNull Editor editor,
            @NotNull PsiFile file,
            @NotNull Document document,
            @NotNull MoveInfo info,
            int startOffset,
            int endOffset,
            boolean down
    ) {}

    private record ElementInfo(
            @NotNull PsiElement element,
            @Nullable PsiElement lineStart,
            @Nullable PsiElement lineEnd,
            @NotNull Selection selection,
            int offset
    ) {}

    private record BlockOfStatements(
            @NotNull PsiElement first,
            @NotNull PsiElement last,
            @Nullable PsiElement next,
            @NotNull Iterable<ElementInfo> parents,
            @NotNull PsiElement firstInParent,
            @NotNull PsiElement lastInParent
    ) {}

    /**
     * First and last direct child which is (partially) selected.
     *
     * @param firstElement First non-whitespace element which is at least partially selected.
     * @param lastElement  Last non-whitespace element which is at least partially.
     */
    private record Selection(
            @NotNull PsiElement firstElement,
            @NotNull PsiElement lastElement
    ) {
        /**
         * Resolves the selection within a specific parent element.
         * Returns {@code null} if the selection contains only whitespaces.
         *
         * @param request The request containing the selected range.
         * @param parent  The parent element for which to resolve the selection.
         * @param offset  The start offset of {@code parent}.
         * @return The first and last non-whitespace element which is at least partially selected, or {@code null}.
         */
        private static @Nullable Selection resolve(@NotNull MoveRequest request, @NotNull PsiElement parent, int offset) {
            assert request.endOffset() > offset || request.startOffset() < offset + parent.getTextLength()
                    : "no selection within parent";

            PsiElement firstSelected = null;
            PsiElement lastSelected = null;

            for (PsiElement cur = parent.getFirstChild(); cur != null; cur = cur.getNextSibling()) {
                int curStartOffset = offset + cur.getStartOffsetInParent();
                int curEndOffset = curStartOffset + cur.getTextLength();
                if (curStartOffset >= request.endOffset()) {
                    break;
                }
                if (curEndOffset <= request.startOffset() || cur instanceof PsiWhiteSpace) {
                    continue;
                }
                if (firstSelected == null) {
                    firstSelected = cur;
                }
                lastSelected = cur;
            }

            if (firstSelected == null /* implicit: || lastSelected == null */) {
                return null;
            } else {
                return new Selection(firstSelected, lastSelected);
            }
        }

        /**
         * First element of the line at the start of the selection.
         * This is either the same element as {@link #firstElement()}, or a comment.
         */
        private @Nullable PsiElement firstOfLine(@NotNull ElementInfo parent) {
            PsiElement firstInLine = expandLine(firstElement, true);
            PsiElement elementBeforeSelection = PsiTreeUtil.skipWhitespacesAndCommentsBackward(firstElement);
            if (elementBeforeSelection != null &&
                (firstInLine == null || firstInLine.getStartOffsetInParent() <= elementBeforeSelection.getStartOffsetInParent())) {
                return null; // There is another element in the same (extended) line
            } else {
                return firstInLine == null ? parent.lineStart() : firstInLine;
            }
        }

        /**
         * Last element of the line at the end of the selection.
         * This is either the same element as {@link #lastElement()}, or a comment.
         */
        private @Nullable PsiElement lastOfLine(@NotNull ElementInfo parent) {
            PsiElement lastInLine = expandLine(lastElement, false);
            PsiElement elementBehindSelection = PsiTreeUtil.skipWhitespacesAndCommentsForward(lastElement);
            if (elementBehindSelection != null &&
                (lastInLine == null || lastInLine.getStartOffsetInParent() >= elementBehindSelection.getStartOffsetInParent())) {
                return null; // There is another element in the same (extended) line
            } else {
                return lastInLine == null ? parent.lineEnd() : lastInLine;
            }
        }
    }
}
