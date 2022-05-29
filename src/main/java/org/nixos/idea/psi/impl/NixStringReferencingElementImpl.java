package org.nixos.idea.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.reference.ReferenceUtil;

public class NixStringReferencingElementImpl extends NixStringPartImpl {
    public NixStringReferencingElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        return ReferenceUtil.getReferences(this);
    }

    @Override
    public PsiReference getReference() {
        return ReferenceUtil.getReference(this);
    }
}
