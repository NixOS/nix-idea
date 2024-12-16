package org.nixos.idea.lang;

import com.intellij.codeInsight.editorActions.moveLeftRight.MoveElementLeftRightHandler;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.psi.NixBindInherit;
import org.nixos.idea.psi.NixExprApp;
import org.nixos.idea.psi.NixExprAttrs;
import org.nixos.idea.psi.NixExprLambda;
import org.nixos.idea.psi.NixExprLet;
import org.nixos.idea.psi.NixExprList;
import org.nixos.idea.psi.NixFormals;
import org.nixos.idea.psi.NixPsiUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public final class NixMoveElementLeftRightHandler extends MoveElementLeftRightHandler {
    @Override
    public PsiElement @NotNull [] getMovableSubElements(@NotNull PsiElement element) {
        return getMovableSubElements0(element);
    }

    static PsiElement @NotNull [] getMovableSubElements0(@NotNull PsiElement element) {
        if (element instanceof NixExprList list) {
            return asArray(list.getItems());
        } else if (element instanceof NixBindInherit inherit) {
            return asArray(inherit.getAttributes());
        } else if (element instanceof NixExprAttrs attrs) {
            return asArray(attrs.getBindList());
        } else if (element instanceof NixExprLet let) {
            return asArray(let.getBindList());
        } else if (element instanceof NixExprLambda lambda) {
            return asArray(lambda.getArgument(), lambda.getFormals());
        } else if (element instanceof NixFormals formals) {
            return asArray(formals.getFormalList());
        } else if (element instanceof NixExprApp app) {
            return asArray(NixPsiUtil.getArguments(app));
        } else {
            return PsiElement.EMPTY_ARRAY;
        }
    }

    private static PsiElement @NotNull [] asArray(@Nullable PsiElement... items) {
        return Arrays.stream(items).filter(Objects::nonNull).toArray(PsiElement[]::new);
    }

    private static PsiElement @NotNull [] asArray(@NotNull Collection<? extends PsiElement> items) {
        assert !items.contains(null) : "Must not contain null: " + items;
        return items.toArray(PsiElement[]::new);
    }
}
