package org.nixos.idea.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry

open class NixExprStdPathMixin(node: ASTNode): NixExprPathImpl(node) {
    override fun getReferences(): Array<PsiReference> =
        ReferenceProvidersRegistry.getReferencesFromProviders(this)
}