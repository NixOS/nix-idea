package org.nixos.idea.lang;

import com.intellij.codeInsight.editorActions.moveLeftRight.MoveElementLeftRightHandler;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.psi.NixBindInherit;
import org.nixos.idea.psi.NixExprApp;
import org.nixos.idea.psi.NixExprAttrs;
import org.nixos.idea.psi.NixExprLambda;
import org.nixos.idea.psi.NixExprLet;
import org.nixos.idea.psi.NixExprList;
import org.nixos.idea.psi.NixFormals;
import org.nixos.idea.psi.NixPsiUtil;

import java.util.Collection;

public final class NixMoveElementLeftRightHandler extends MoveElementLeftRightHandler {
    @Override
    public PsiElement @NotNull [] getMovableSubElements(@NotNull PsiElement element) {
        if (element instanceof NixExprList list) {
            return asArray(list.getItems());
        } else if (element instanceof NixBindInherit inherit) {
            return asArray(inherit.getAttributes());
        } else if (element instanceof NixExprAttrs attrs) {
            return asArray(attrs.getBindList());
        } else if (element instanceof NixExprLet let) {
            return asArray(let.getBindList());
        } else if (element instanceof NixExprLambda lambda) {
            return new PsiElement[]{lambda.getArgument(), lambda.getFormals()};
        } else if (element instanceof NixFormals formals) {
            return asArray(formals.getFormalList());
        } else if (element instanceof NixExprApp app) {
            return asArray(NixPsiUtil.getArguments(app));
        } else {
            return PsiElement.EMPTY_ARRAY;
        }
    }

    private PsiElement @NotNull [] asArray(@NotNull Collection<? extends PsiElement> items) {
        return items.toArray(PsiElement[]::new);
    }
}
