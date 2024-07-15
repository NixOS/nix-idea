package org.nixos.idea.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import org.nixos.idea.psi.impl.AbstractNixString
import org.nixos.idea.util.NixStringUtil

class NixStringLiteralEscaper(host: AbstractNixString) : LiteralTextEscaper<NixString>(host) {

    override fun isOneLine(): Boolean = false // TODO Check

    private var outSourceOffsets: IntArray? = null

    override fun getRelevantTextRange(): TextRange {
        val parts = myHost.stringParts
        return if (parts.isEmpty()) TextRange.EMPTY_RANGE
        else TextRange.create(parts.first().startOffsetInParent, parts.last().textRangeInParent.endOffset)
    }

    override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
        val maxIndent = NixStringUtil.detectMaxIndent(myHost)
        val subText: String = rangeInsideHost.substring(myHost.text)
        val outOffset = outChars.length
        val array = IntArray(subText.length + 1)
        var success = true

        for (part in myHost.stringParts) {
            assert(part.parent == myHost)
            val partRange = part.textRangeInParent
            if (partRange.startOffset >= rangeInsideHost.endOffset) {
                break
            } else if (partRange.endOffset < rangeInsideHost.startOffset) {
                continue
            }

            fun addText(text: CharSequence, offset: Int): Boolean {
                val start = partRange.startOffset + offset
                for (i in text.indices) {
                    if (start + i >= rangeInsideHost.startOffset) {
                        array[outChars.length - outOffset] = start + i
                        outChars.append(text[i])
                    } else if (start + i >= rangeInsideHost.endOffset) {
                        return false
                    }
                }
                return true
            }

            if (part is NixStringText) {
                NixStringUtil.visit(object : NixStringUtil.StringVisitor {
                    override fun text(text: CharSequence, offset: Int): Boolean {
                        return addText(text, offset)
                    }

                    override fun escapeSequence(text: String, offset: Int, escapeSequence: CharSequence): Boolean {
                        val start = partRange.startOffset + offset
                        val end = start + escapeSequence.length
                        return if (start < rangeInsideHost.startOffset || end > rangeInsideHost.endOffset) {
                            success = false
                            false
                        } else {
                            for (i in escapeSequence.indices) {
                                array[outChars.length - outOffset + i] = start
                            }
                            outChars.append(text)
                            true
                        }
                    }
                }, part, maxIndent)
            } else {
                assert(part is NixAntiquotation)
                addText(part.text, 0)
            }
        }

        outSourceOffsets = array
        return success
    }

    override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
        val offsets = outSourceOffsets ?: throw IllegalStateException("#decode was not called")
        val result = if (offsetInDecoded < offsets.size) offsets[offsetInDecoded] else -1
        return result.coerceIn(0..rangeInsideHost.length) + rangeInsideHost.startOffset
    }

}
