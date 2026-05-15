package org.nixos.idea.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import org.nixos.idea.util.NixStringUtil

class NixStringLiteralEscaper(host: NixString) : LiteralTextEscaper<NixString>(host) {

    private var startOffset = 0
    private var outSourceOffsets: IntArray? = null

    override fun isOneLine(): Boolean = false

    override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
        val maxIndent = NixStringUtil.detectMaxIndent(myHost)
        val array = IntArray(rangeInsideHost.length + 1) { -1 } // escape sequences can only make the result smaller
        var success = true

        var currentToken = myHost.node.findLeafElementAt(rangeInsideHost.startOffset)
        val stringPart = currentToken?.treeParent?.psi as? NixStringText ?: return true // TODO When to return false?
        assert(stringPart.parent === myHost)

        var offset = currentToken.startOffset - myHost.node.startOffset
        var written = 0
        while (currentToken != null && offset < rangeInsideHost.endOffset) {
            val decodedText = NixStringUtil.parse(currentToken, maxIndent)
            outChars.append(decodedText)

            // Skip indention and start of escape sequences
            offset += currentToken.textLength - decodedText.length
            // establish 1-to-1 mapping of all remaining characters
            decodedText.forEach { _ -> array[written++] = offset++ }

            currentToken = currentToken.treeNext
        }
        array[written] = offset

        assert(array[0] >= rangeInsideHost.startOffset) { "${array[0]} < ${rangeInsideHost.startOffset}" }
        assert(offset == rangeInsideHost.endOffset) { "$offset != ${rangeInsideHost.endOffset}" }

        startOffset = rangeInsideHost.startOffset
        outSourceOffsets = array
        return success
    }

    override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
        val offsets = outSourceOffsets ?: throw IllegalStateException("#decode was not called")
        return if (offsetInDecoded >= offsets.size || offsets[offsetInDecoded] < 0) {
            -1
        } else {
            val offsetInHost = offsets[offsetInDecoded]
            // Workaround for IJPL-244922 (https://youtrack.jetbrains.com/issue/IJPL-244922/):
            //   InjectedLanguageUtil.hostToInjectedUnescaped provides an inappropriate value for `rangeInsideHost`,
            //   but then also expects an equally wrong return value.
            val fixed = offsetInHost - startOffset + rangeInsideHost.startOffset
            fixed.coerceIn(rangeInsideHost.startOffset..rangeInsideHost.endOffset)
        }
    }

}
