package org.nixos.idea.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.IncorrectOperationException
import org.nixos.idea.psi.NixIndString
import org.nixos.idea.psi.NixString
import org.nixos.idea.psi.NixStringLiteralEscaper


abstract class AbstractNixString(private val astNode: ASTNode) : PsiLanguageInjectionHost, AbstractNixPsiElement(astNode),NixString {

    override fun isValidHost() = true

    override fun updateText(s: String): NixString {
        // TODO also support single-line strings
        if (this !is NixIndString) {
            LOG.info("not a nix ind string")
            return this
        }
        (astNode.firstChildNode.treeNext.firstChildNode as? LeafPsiElement)
            ?.replaceWithText(s)
        return this
    }

    override fun createLiteralTextEscaper() = NixStringLiteralEscaper(this)

    companion object {
        val LOG = Logger.getInstance(AbstractNixString::class.java)
    }
}

