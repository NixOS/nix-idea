package org.nixos.idea.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.psi.ElementManipulators
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost
import org.intellij.grammar.psi.impl.BnfStringImpl
import org.intellij.grammar.psi.impl.BnfStringManipulator.getStringTokenRange
import org.nixos.idea.util.NixStringUtil


abstract class AbstractNixString(node: ASTNode) : PsiLanguageInjectionHost, AbstractNixPsiElement(node) {

    override fun isValidHost() = true

    override fun updateText(text: String): PsiLanguageInjectionHost =
        ElementManipulators.handleContentChange(this, text)

    override fun createLiteralTextEscaper() = NixStringLiteralEscaper(this)
}

class NixStringLiteralEscaper(host: AbstractNixString) : LiteralTextEscaper<PsiLanguageInjectionHost>(host) {

    override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
        val sb = StringBuilder()
        NixStringUtil.escape(sb, rangeInsideHost.toString())
        outChars.append(sb, rangeInsideHost.startOffset, rangeInsideHost.endOffset)

        return true
    }

    override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int = with(rangeInsideHost) {
        // todo implement proper java-like string escapes support
        val offset = offsetInDecoded + startOffset
        return when {
            offset < startOffset -> startOffset
            offset > endOffset -> endOffset
            else -> offset
        }
    }

    override fun isOneLine(): Boolean = false
}

class IndStringManipulator : AbstractElementManipulator<NixStringImpl>() {
    override fun handleContentChange(
        element: NixStringImpl,
        range: TextRange,
        newContent: String?
    ): NixStringImpl? {

        TODO("Not yet implemented")
    }

    fun getRangeInElement(element: NixStringImpl?): TextRange {
        TODO("Not yet implemented")
    }
}