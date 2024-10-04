package org.nixos.idea.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.lang.references.NixSymbolDeclaration;
import org.nixos.idea.lang.references.NixSymbolReference;
import org.nixos.idea.lang.references.Scope;

import java.util.Collection;
import java.util.List;

public interface NixPsiElement extends PsiElement {

    @NotNull Scope getScope();

    @Override
    @SuppressWarnings("UnstableApiUsage")
    @NotNull Collection<? extends NixSymbolDeclaration> getOwnDeclarations();

    @Override
    @SuppressWarnings("UnstableApiUsage")
    @NotNull List<? extends NixSymbolReference> getOwnReferences();

    <T> T accept(@NotNull NixElementVisitor<T> visitor);
}
