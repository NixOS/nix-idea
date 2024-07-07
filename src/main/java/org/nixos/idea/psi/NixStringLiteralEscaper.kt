package org.nixos.idea.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost
import org.nixos.idea.psi.impl.AbstractNixString

class NixStringLiteralEscaper(host: AbstractNixString) : LiteralTextEscaper<PsiLanguageInjectionHost>(host) {

    override fun isOneLine(): Boolean = false

    private var outSourceOffsets: IntArray? = null

    override fun getRelevantTextRange(): TextRange {
        if (myHost.textLength <= 4) return TextRange.EMPTY_RANGE
        return TextRange.create(2, myHost.textLength - 2)
    }

    override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
        // TODO only indented strings supported for now
        // single line strings require a new decode function because
        // it uses different escaping mechanisms
        if (myHost !is NixIndString) return false

        val subText: String = rangeInsideHost.substring(myHost.text)
        val array = IntArray(subText.length + 1)
        val success = unescapeAndDecode(subText, outChars, array)
        outSourceOffsets = array
        return success
    }

    override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
        val offsets = outSourceOffsets ?: throw IllegalStateException("#decode was not called")
        val result = if (offsetInDecoded < offsets.size) offsets[offsetInDecoded] else -1
        return result.coerceIn(2..rangeInsideHost.length) + rangeInsideHost.startOffset
    }

    companion object {
        // this function does not consider interpolations so that
        // they do appear in the guest language and remain when we end up converting back to Nix
        fun unescapeAndDecode(chars: String, outChars: StringBuilder, sourceOffsets: IntArray?): Boolean {
            assert(sourceOffsets == null || sourceOffsets.size == chars.length + 1)

            var index = 0
            val outOffset = outChars.length
            var braces = 0


            while (index < chars.length) {
                fun updateOffsets(index: Int) {
                    if (sourceOffsets != null) {
                        sourceOffsets[outChars.length - outOffset] = index - 1
                        sourceOffsets[outChars.length - outOffset + 1] = index
                    }
                }
                var c = chars[index++]

                updateOffsets(index)


                if (braces > 0) {
                    if (c == '{') braces++
                    else if (c == '}') braces--
                    outChars.append(c)
                    continue
                }

                if (c == '\'') {
                    if (index == chars.length) return false
                    c = chars[index++]

                    if (c != '\'') {
                        // if what follows isn't another ' then we are not escaping anything,
                        // so we can continue
                        outChars.append("\'")
                        updateOffsets(index - 1)
                        outChars.append(c)
                        continue
                    }

                    if (index == chars.length) return false
                    c = chars[index++]

                    when(c) {
                        // '' can be escaped by prefixing it with ', i.e., '''.
                        '\'' -> {
                            outChars.append("\'")
                            updateOffsets(index - 1)
                            outChars.append(c)
                        }
                        //  $ can be escaped by prefixing it with '' (that is, two single quotes), i.e., ''$.
                        '$' -> outChars.append(c)
                        '\\' -> {
                            if (index == chars.length) return false
                            c = chars[index++]
                            when(c) {
                                // Linefeed, carriage-return and tab characters can
                                // be written as ''\n, ''\r, ''\t, and ''\ escapes any other character.
                                'a' -> outChars.append(0x07.toChar())
                                'b' -> outChars.append('\b')
                                'f' -> outChars.append(0x0c.toChar())
                                'n' -> outChars.append('\n')
                                't' -> outChars.append('\t')
                                'r' -> outChars.append('\r')
                                'v' -> outChars.append(0x0b.toChar())
                                else -> return false
                            }
                        }
                        else -> return false
                    }
                    if (sourceOffsets != null) {
                        sourceOffsets[outChars.length - outOffset] = index
                    }
                    continue
                }

                outChars.append(c)
            }
            return true
        }
    }

}