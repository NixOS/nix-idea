package org.nixos.idea.util;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.UnaryOperator;

public final class PsiSiblingIterator implements Iterator<PsiElement> {

    //region Factory Methods

    /**
     * Iterates all children of {@code parent}.
     *
     * @param parent .Element from which to iterate the children.
     * @return A newly created iterable.
     */
    public static @NotNull StreamableIterable<PsiElement> children(@NotNull PsiElement parent) {
        return siblings(parent.getFirstChild(), parent.getLastChild());
    }

    /**
     * Iterates all siblings between {@code first} and {@code last}.
     *
     * @param first First element to be returned. Can be {@code null} to create an empty iterable.
     * @param last  Last element to be returned. Can be {@code null} to return all siblings behind {@code first},
     * @return A newly created iterable.
     */
    public static @NotNull StreamableIterable<PsiElement> siblings(@Nullable PsiElement first, @Nullable PsiElement last) {
        assert first == null || last == null || first.getParent() == last.getParent()
                : "first and last have different parents: " + first + ", " + last;
        return iterable(first, last, PsiElement::getNextSibling);
    }

    /**
     * Iterates the children of {@code parent}, ignoring {@linkplain PsiWhiteSpace white spaces}.
     *
     * @param parent Element from which to iterate the children.
     * @return A newly created iterable.
     */
    public static @NotNull StreamableIterable<PsiElement> visibleChildren(@NotNull PsiElement parent) {
        return visibleSiblings(parent.getFirstChild(), parent.getLastChild());
    }

    /**
     * Iterates the children of {@code parent} between {@code first} and {@code last}, ignoring {@linkplain PsiWhiteSpace white spaces}.
     *
     * @param parent Element from which to iterate the children.
     * @param first  First element to be returned. Can be {@code null} to start with the first child.
     * @param last   Last element to be returned. Can be {@code null} to return all siblings behind {@code first},
     * @return A newly created iterable.
     */
    public static @NotNull StreamableIterable<PsiElement> visibleChildren(@NotNull PsiElement parent, @Nullable PsiElement first, @Nullable PsiElement last) {
        assert first == null || first.getParent() == parent
                : "first not a child of parent: first=" + first + "; parent=" + parent;
        assert last == null || last.getParent() == parent
                : "last not a child of parent: last=" + last + "; parent=" + parent;
        return visibleSiblings(first == null ? parent.getFirstChild() : first, parent.getLastChild());
    }

    /**
     * Iterates the siblings between {@code first} and {@code last}, ignoring {@linkplain PsiWhiteSpace white spaces}.
     *
     * @param first First element to be returned. Can be {@code null} to create an empty iterable.
     * @param last  Last element to be returned. Can be {@code null} to return all siblings behind {@code first},
     * @return A newly created iterable.
     */
    public static @NotNull StreamableIterable<PsiElement> visibleSiblings(@Nullable PsiElement first, @Nullable PsiElement last) {
        assert first == null || last == null || first.getParent() == last.getParent()
                : "first and last have different parents: " + first + ", " + last;
        if (first instanceof PsiWhiteSpace) {
            first = PsiTreeUtil.skipWhitespacesForward(first);
        }
        if (last instanceof PsiWhiteSpace) {
            last = PsiTreeUtil.skipWhitespacesBackward(last);
        }
        return iterable(first, last, PsiTreeUtil::skipWhitespacesForward);
    }

    private static @NotNull StreamableIterable<PsiElement> iterable(
            @Nullable PsiElement next, @Nullable PsiElement last,
            @NotNull UnaryOperator<PsiElement> incrementer) {
        return () -> new PsiSiblingIterator(next, last, incrementer);
    }

    //endregion
    //region Iterator Implementation

    private final @NotNull UnaryOperator<PsiElement> myIncrementer;
    private final @Nullable PsiElement myLast;
    private @Nullable PsiElement myNext;

    private PsiSiblingIterator(@Nullable PsiElement next, @Nullable PsiElement last, @NotNull UnaryOperator<PsiElement> incrementer) {
        myIncrementer = incrementer;
        myNext = next;
        myLast = last;
    }

    @Override
    public boolean hasNext() {
        return myNext != null;
    }

    @Override
    public PsiElement next() {
        PsiElement current = myNext;
        if (current == null) {
            throw new NoSuchElementException();
        }

        if (current == myLast) {
            myNext = null;
        } else {
            myNext = myIncrementer.apply(current);
        }

        return current;
    }

    //endregion
}
