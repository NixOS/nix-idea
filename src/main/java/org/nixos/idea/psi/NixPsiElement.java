package org.nixos.idea.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public interface NixPsiElement extends PsiElement {
    <T> T accept(@NotNull NixElementVisitor<T> visitor);
}
