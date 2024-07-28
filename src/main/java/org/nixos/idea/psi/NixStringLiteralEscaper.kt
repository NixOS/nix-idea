package org.nixos.idea.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import org.nixos.idea.psi.impl.AbstractNixString
import org.nixos.idea.util.NixStringUtil

class NixStringLiteralEscaper(host: AbstractNixString) : LiteralTextEscaper<NixStringText>(host) {

    override fun isOneLine(): Boolean = false

    private var outSourceOffsets: IntArray? = null

    override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
        val maxIndent = NixStringUtil.detectMaxIndent(myHost.parent as NixString)
        val subText: String = rangeInsideHost.substring(myHost.text)
        val outOffset = outChars.length
        val array = IntArray(subText.length + 1)
        var success = true

        fun addText(text: CharSequence, offset: Int): Boolean {
            for (i in text.indices) {
                if (offset + i >= rangeInsideHost.startOffset) {
                    array[outChars.length - outOffset] = offset + i
                    outChars.append(text[i])
                } else if (offset + i >= rangeInsideHost.endOffset) {
                    return false
                }
            }
            return true
        }

        NixStringUtil.visit(object : NixStringUtil.StringVisitor {
            override fun text(text: CharSequence, offset: Int): Boolean {
                return addText(text, offset)
            }

            override fun escapeSequence(text: String, offset: Int, escapeSequence: CharSequence): Boolean {
                val end = offset + escapeSequence.length
                return if (offset < rangeInsideHost.startOffset || end > rangeInsideHost.endOffset) {
                    success = false
                    false
                } else {
                    for (i in escapeSequence.indices) {
                        array[outChars.length - outOffset + i] = offset
                    }
                    outChars.append(text)
                    true
                }
            }
        }, myHost, maxIndent)
        // TODO Fix ArrayIndexOutOfBoundsException in the following line. (Not sure how to reproduce it.)
        array[outChars.length - outOffset] = rangeInsideHost.endOffset
        for (i in (outChars.length - outOffset + 1)..<array.size) {
            array[i] = -1
        }

        outSourceOffsets = array
        return success
    }

    override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
        val offsets = outSourceOffsets ?: throw IllegalStateException("#decode was not called")
        val result = if (offsetInDecoded < offsets.size) offsets[offsetInDecoded] else -1
        return result.coerceIn(-1..rangeInsideHost.length)
    }

}
