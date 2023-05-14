package org.nixos.idea.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.interpretation.AttributeMap;
import org.nixos.idea.interpretation.Declaration;
import org.nixos.idea.interpretation.Scope;
import org.nixos.idea.interpretation.VariableUsage;

import java.util.Collection;

public interface NixPsiElement extends PsiElement {
    @NotNull AttributeMap<Collection<Declaration>> getDeclarations();
    @NotNull Scope getScope();
    @NotNull AttributeMap<Collection<VariableUsage>> getUsages();
}
