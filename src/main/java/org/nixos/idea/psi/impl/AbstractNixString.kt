package org.nixos.idea.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiLanguageInjectionHost
import org.nixos.idea.psi.NixElementFactory
import org.nixos.idea.psi.NixIndString
import org.nixos.idea.psi.NixStdString
import org.nixos.idea.psi.NixStringLiteralEscaper
import org.nixos.idea.psi.NixStringText
import org.nixos.idea.util.NixStringUtil


abstract class AbstractNixString(private val astNode: ASTNode) : PsiLanguageInjectionHost,
    AbstractNixPsiElement(astNode), NixStringText {

    override fun isValidHost() = true

    override fun updateText(s: String): NixStringText {
        val project = project
        val replacement = when (val string = parent) {
            is NixStdString -> {
                val escaped = buildString { NixStringUtil.escapeStd(this, s) }
                NixElementFactory.createStdStringText(project, escaped)
            }

            is NixIndString -> {
                val indent = indentForNewText(string)
                val indentStart = prevSibling == null
                val indentEnd = if (nextSibling == null) trailingIndent(text) ?: baseIndent(string) else indent
                val escaped = buildString { NixStringUtil.escapeInd(this, s, indent, indentStart, indentEnd) }
                NixElementFactory.createIndStringText(project, escaped)
            }

            else -> throw IllegalStateException("Unexpected parent: " + parent.javaClass)
        }
        return replace(replacement) as NixStringText
    }

    override fun createLiteralTextEscaper() = NixStringLiteralEscaper(this)

    companion object {
        private fun indentForNewText(string: NixIndString): Int {
            return NixStringUtil.detectMaxIndent(string).takeIf { it != Int.MAX_VALUE } ?: (baseIndent(string) + 2)
        }

        private fun baseIndent(string: NixIndString): Int {
            // TODO Detect indent of string
            //  This should be the indent of the line where the string starts.
            return 0
        }

        private fun trailingIndent(str: String): Int? {
            val lastLineFeed = str.lastIndexOf('\n')
            val lastLine = if (lastLineFeed != -1) str.substring(lastLineFeed + 1) else null
            return if (lastLine != null && lastLine.all { it == ' ' }) {
                lastLine.length
            } else {
                null
            }
        }
    }
}
