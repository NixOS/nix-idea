package org.nixos.idea.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.nixos.idea.psi.NixIndString
import org.nixos.idea.psi.NixString
import org.nixos.idea.psi.NixStringLiteralEscaper


abstract class AbstractNixString(private val astNode: ASTNode) : PsiLanguageInjectionHost,
    AbstractNixPsiElement(astNode), NixString {

    override fun isValidHost() = true

    override fun updateText(s: String): NixString {
        // TODO issue #81 also support single-line strings
        if (this !is NixIndString) {
            LOG.info("not a nix ind string")
            return this
        }
        val originalNode = astNode.firstChildNode.treeNext.firstChildNode as? LeafPsiElement
        val minIndentInOriginal = originalNode?.text?.lines()
            ?.filterNot { it.isEmpty() }
            ?.minOfOrNull { it.takeWhile(Char::isWhitespace).count() } ?: 0

        val leadingSpace = buildString { repeat(minIndentInOriginal) { append(' ') } }

        val withoutQuotesWithIndent = s
            // remove quotes
            .substring(2..(s.lastIndex - 2))
            // restore indent
            .lines()
            .withIndex()
            .joinToString(separator = System.lineSeparator()) { (index, line) ->
                if (index != 0) leadingSpace + line else line
            }

        originalNode?.replaceWithText(withoutQuotesWithIndent)
        return this
    }

    override fun createLiteralTextEscaper() = NixStringLiteralEscaper(this)

    companion object {
        val LOG = Logger.getInstance(AbstractNixString::class.java)
    }
}

