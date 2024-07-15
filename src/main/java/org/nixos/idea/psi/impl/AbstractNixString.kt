package org.nixos.idea.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.nixos.idea.psi.NixIndString
import org.nixos.idea.psi.NixString
import org.nixos.idea.psi.NixStringLiteralEscaper


abstract class AbstractNixString(private val astNode: ASTNode) : PsiLanguageInjectionHost,
    AbstractNixPsiElement(astNode), NixString {

    override fun isValidHost() = this is NixIndString

    override fun updateText(s: String): NixString {
        // TODO issue #81 also support single-line strings
        if (this !is NixIndString) {
            TODO("Can we implement this easily?")
            TODO("Does this behave well?")
            LOG.info("not a nix ind string")
            return this
        }
        val originalNode = astNode.firstChildNode.treeNext.firstChildNode as? LeafPsiElement
        val minIndentInOriginal = originalNode?.text?.lines()
            ?.filterNot { it.isEmpty() }
            ?.minOfOrNull { it.takeWhile(Char::isWhitespace).count() } ?: 0

        val leadingSpace = buildString { repeat(minIndentInOriginal) { append(' ') } }

        val lines = s.substring(2..(s.lastIndex - 2)) // remove quotes
            .lines()

        // restore indent
        val withIndent = lines
            .withIndex()
            .map { (index, line) -> if (index != 0) leadingSpace + line else line }

        // if the first line was removed in the fragment, add it back to preserve a multiline string
        val withLeadingBlankLine = if (lines.first().isNotEmpty()) listOf("") + withIndent else withIndent

        originalNode?.replaceWithText(withLeadingBlankLine.joinToString(separator = "\n"))
        return this
    }

    override fun createLiteralTextEscaper() = NixStringLiteralEscaper(this)

    companion object {
        val LOG = Logger.getInstance(AbstractNixString::class.java)
    }
}

