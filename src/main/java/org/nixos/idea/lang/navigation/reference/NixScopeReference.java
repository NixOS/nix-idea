package org.nixos.idea.lang.navigation.reference;

import com.intellij.model.SingleTargetReference;
import com.intellij.model.Symbol;
import com.intellij.model.psi.PsiSymbolReference;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.interpretation.Attribute;
import org.nixos.idea.interpretation.AttributePath;
import org.nixos.idea.lang.builtins.NixBuiltin;
import org.nixos.idea.lang.navigation.symbol.NixSymbol;
import org.nixos.idea.psi.NixExpr;
import org.nixos.idea.psi.NixExprLambda;
import org.nixos.idea.psi.NixPsiElement;
import org.nixos.idea.util.TextRangeFactory;

@SuppressWarnings("UnstableApiUsage")
public final class NixScopeReference extends SingleTargetReference implements PsiSymbolReference {
    // TODO: Implement PsiCompletableReference?

    private final @NotNull NixPsiElement myElement;
    private final @NotNull String myVariableName;

    public NixScopeReference(@NotNull NixPsiElement element, @NotNull String variableName) {
        myElement = element;
        myVariableName = variableName;
    }

    @Override
    public @NotNull PsiElement getElement() {
        return myElement;
    }

    @Override
    public @NotNull TextRange getRangeInElement() {
        return TextRangeFactory.root(myElement);
    }

    @Override
    protected @Nullable Symbol resolveSingleTarget() {
        NixPsiElement origin = myElement.getScope().getOrigin(myVariableName);
        if (origin == null) {
            NixBuiltin builtin = NixBuiltin.resolveGlobal(myVariableName);
            return builtin == null ? null : NixSymbol.builtin(builtin);
        } else {
            if (origin instanceof NixExprLambda) {
                return NixSymbol.parameter((NixExprLambda) origin, myVariableName);
            } else {
                return NixSymbol.attribute((NixExpr) origin, AttributePath.of(Attribute.of(myVariableName)));
            }
        }
    }

    @Override
    public boolean resolvesTo(@NotNull Symbol target) {
        // Check name as a shortcut to avoid resolving the reference when it cannot match anyway.
        return target instanceof NixSymbol &&
                myVariableName.equals(((NixSymbol) target).getName().getName()) &&
                PsiSymbolReference.super.resolvesTo(target);
    }
}
