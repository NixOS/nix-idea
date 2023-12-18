package org.nixos.idea.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.interpretation.AttributeMap;
import org.nixos.idea.lang.navigation.scope.Scope;
import org.nixos.idea.interpretation.VariableUsage;

public interface NixPsiElement extends PsiElement {
    @NotNull Scope getScope();
    @NotNull AttributeMap<VariableUsage> getUsages();
    <T> T accept(@NotNull NixElementVisitor<T> visitor);
}
