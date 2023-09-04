package org.nixos.idea.psi;

import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.interpretation.AttributePath;

public interface NixNamedElement extends NixPsiElement, PsiNameIdentifierOwner {
    @NotNull AttributePath getAttributePath();
    @NotNull NixPsiElement @NotNull [] getAttributeElements();
}
