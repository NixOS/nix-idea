package org.nixos.idea.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.psi.NixPsiElement;

abstract public class AbstractNixPsiElement extends ASTWrapperPsiElement implements NixPsiElement {

    AbstractNixPsiElement(@NotNull ASTNode node) {
        super(node);
    }

}
