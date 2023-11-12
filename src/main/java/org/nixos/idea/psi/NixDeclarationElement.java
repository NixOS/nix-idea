package org.nixos.idea.psi;

import org.jetbrains.annotations.NotNull;
import org.nixos.idea.interpretation.AttributePath;

public interface NixDeclarationElement extends NixPsiElement {
    @NotNull NixDeclarationHost getDeclarationHost();

    @NotNull AttributePath getAttributePath();

    @NotNull NixPsiElement @NotNull [] getAttributeElements();
}
