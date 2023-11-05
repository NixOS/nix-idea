package org.nixos.idea.lang.navigation.reference;

import com.intellij.model.SingleTargetReference;
import com.intellij.model.Symbol;
import com.intellij.model.psi.PsiSymbolReference;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.interpretation.AttributePath;
import org.nixos.idea.lang.navigation.symbol.NixSymbol;
import org.nixos.idea.psi.NixAttr;
import org.nixos.idea.psi.NixExpr;
import org.nixos.idea.psi.NixPsiElement;
import org.nixos.idea.util.NixTextRangeFactory;

@SuppressWarnings("UnstableApiUsage")
public final class NixAttributeReference extends SingleTargetReference implements PsiSymbolReference {
    // TODO: Implement PsiCompletableReference?

    private final @NotNull NixPsiElement myOwner;
    private final @NotNull NixExpr myAccessedObject;
    private final @NotNull AttributePath myPath;
    private final @NotNull NixAttr myAttribute;

    public NixAttributeReference(@NotNull NixPsiElement owner,
                                 @NotNull NixExpr accessedObject,
                                 @NotNull AttributePath path,
                                 @NotNull NixAttr attribute) {
        myOwner = owner;
        myAccessedObject = accessedObject;
        myPath = path;
        myAttribute = attribute;
    }

    @Override
    public @NotNull PsiElement getElement() {
        return myOwner;
    }

    @Override
    public @NotNull TextRange getRangeInElement() {
        return NixTextRangeFactory.relative(myAttribute, myOwner);
    }

    @Override
    protected @Nullable Symbol resolveSingleTarget() {
        return null; // TODO: Implement
    }

    @Override
    public boolean resolvesTo(@NotNull Symbol target) {
        // Check name as a shortcut to avoid resolving the reference when it cannot match anyway.
        return target instanceof NixSymbol &&
                ((NixSymbol) target).getName().matches(myPath.last()).may() &&
                PsiSymbolReference.super.resolvesTo(target);
    }
}
