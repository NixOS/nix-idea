package org.nixos.idea.psi.impl

import com.intellij.lang.ASTNode
import org.nixos.idea.psi.NixIndString
import org.nixos.idea.psi.NixString
import org.nixos.idea.psi.NixStringLiteralEscaper
import org.nixos.idea.util.NixStringUtil


abstract class AbstractNixString(astNode: ASTNode) : AbstractNixPsiElement(astNode), NixString {

    override fun isValidHost() = true

    override fun updateText(s: String): NixString {
        // TODO Should we implement this method?
        throw UnsupportedOperationException("NixString doesn't support updateText")
    }

    override fun createLiteralTextEscaper() = NixStringLiteralEscaper(this)

    companion object {
        fun indentForNewText(string: NixIndString): Int {
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
