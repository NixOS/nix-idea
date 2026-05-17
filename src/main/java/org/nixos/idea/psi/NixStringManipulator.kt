package org.nixos.idea.psi

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.startOffset
import org.nixos.idea.util.NixStringUtil

/**
 * Handles modifications on string content of any [NixString].
 * This class is used by the fragment editor of IntelliJ.
 */
class NixStringManipulator : AbstractElementManipulator<NixString>() {

    // TODO Can we use this for copy&paste into strings?
    // TODO Can we improve performance by only replacing the tokens which are affected?

    override fun handleContentChange(
        element: NixString,
        range: TextRange,
        newContent: String,
    ): NixString {
        val oldText = element.text
        val elementStartOffset = element.startOffset
        val indent = NixPsiUtil.getIndent(element)

        var startOffset = range.startOffset
        var endOffset = range.endOffset

        // Extend selection to include surrounding tokens of type STR, IND_STR, and IND_STR_INDENT.
        // STR and IND_STR are consumed to avoid bugs regarding escape sequences.
        // Like when inserting `$` before `{x}`, or a second `'` next to another one in indented strings.
        // IND_STR_INDENT is consumed, because the indent is re-applied later.
        val unescaped = StringBuilder(newContent.length).apply {
            fun consumePrevious(token: ASTNode, offset: Int = token.textLength) {
                val type = token.elementType
                if (type === NixTypes.STR || type === NixTypes.IND_STR) {
                    startOffset -= offset
                    token.treePrev?.let { consumePrevious(it) }
                    append(token.text, 0, offset)
                } else if (type === NixTypes.IND_STR_INDENT) {
                    startOffset -= offset.coerceAtMost(indent)
                }
            }

            val prevPartialToken = element.node.findLeafElementAt(range.startOffset - 1)!!
            val prevPartialTokenOffset = elementStartOffset + range.startOffset - prevPartialToken.startOffset
            if (NixTokenSets.STRING_ESCAPE.contains(prevPartialToken.elementType) && prevPartialTokenOffset != prevPartialToken.textLength) {
                val remaining = prevPartialToken.textLength - prevPartialTokenOffset
                val parsed = NixStringUtil.parse(prevPartialToken, indent)
                startOffset -= prevPartialTokenOffset
                prevPartialToken.treePrev?.let { consumePrevious(it) }
                append(parsed, 0, (parsed.length - remaining).coerceAtLeast(0))
            } else {
                consumePrevious(prevPartialToken, prevPartialTokenOffset)
            }

            append(newContent)

            fun consumeNext(token: ASTNode, offset: Int = 0) {
                val type = token.elementType
                if (type === NixTypes.STR || type === NixTypes.IND_STR) {
                    endOffset += token.textLength - offset
                    append(token.text, offset, token.textLength)
                    token.treeNext?.let { consumeNext(it) }
                } else if (type === NixTypes.IND_STR_INDENT) {
                    val tokenEnd = token.startOffset + token.textLength.coerceAtMost(indent)
                    endOffset = (tokenEnd - elementStartOffset).coerceAtLeast(endOffset)
                }
            }

            val nextPartialToken = element.node.findLeafElementAt(range.endOffset)!!
            val nextPartialTokenOffset = elementStartOffset + range.endOffset - nextPartialToken.startOffset
            if (NixTokenSets.STRING_ESCAPE.contains(nextPartialToken.elementType) && nextPartialTokenOffset != 0) {
                val remaining = nextPartialToken.textLength - nextPartialTokenOffset
                val parsed = NixStringUtil.parse(nextPartialToken, indent)
                endOffset += remaining
                append(parsed, (parsed.length - remaining).coerceAtLeast(0), parsed.length)
                nextPartialToken.treeNext?.let { consumeNext(it) }
            } else {
                consumeNext(nextPartialToken, nextPartialTokenOffset)
            }
        }

        val newText = buildString((1.1 * oldText.length).toInt()) {
            when (element) {
                is NixStdString -> {
                    append(oldText, 0, startOffset)
                    NixStringUtil.escapeStd(this, unescaped, 0)
                    append(oldText, endOffset, oldText.length)
                }

                is NixIndString -> {
                    var indentStart = false
                    var indentEnd = indent

                    // If we start right after the indent, trim the ident and re-apply it later
                    val firstToken = element.node.findLeafElementAt(startOffset)!!
                    if (firstToken.elementType === NixTypes.IND_STR_INDENT) {
                        indentStart = true
                    }

                    // Preserve indent of closing quotes.
                    // If (adjusted) range ends just before the closing quotes, overwrite [indentEnd].
                    val closingQuotes = element.node.lastChildNode
                    if (endOffset == closingQuotes.startOffsetInParent) {
                        indentEnd = element.node.findLeafElementAt(endOffset)!!
                            .let { if (it === closingQuotes) PsiTreeUtil.prevLeaf(it.psi)!!.node else it }
                            .let { if (it.elementType === NixTypes.IND_STR_INDENT) it else null }
                            .let { it?.textLength ?: NixPsiUtil.getBaseIndent(element) }
                    }

                    // Potentially add a line feed after the starting quotes ('') to
                    // convert single-line strings into multiline strings.
                    if (!newContent.contains('\n')) {
                        append(oldText, 0, startOffset)
                    } else if (range.startOffset < element.stringParts.first().startOffsetInParent) {
                        append("''\n")
                        indentStart = true
                    } else if (NixPsiUtil.isSingleLine(element)) {
                        append("''\n")
                        if (startOffset > 2) {
                            repeat(" ", indent)
                            append(oldText, 2, startOffset)
                        } else {
                            indentStart = true
                        }
                    } else {
                        append(oldText, 0, startOffset)
                    }

                    NixStringUtil.escapeInd(this, unescaped, indent, indentStart, indentEnd, 0)
                    append(oldText, endOffset, oldText.length)
                }

                else -> throw IllegalStateException("Unexpected string type: " + element.javaClass)
            }
        }

        return element.replace(NixElementFactory.createString(element.project, newText)) as NixString
    }

    override fun getRangeInElement(element: NixString): TextRange {
        val parts = element.stringParts
        return TextRange.create(parts.first().startOffsetInParent, parts.last().textRangeInParent.endOffset)
    }

    // Rational for suppressing inspection:
    //   A static method, which only references the same types as the extension interface,
    //   should not cause any additional class loading.
    @Suppress("CompanionObjectInExtension")
    companion object {
        @JvmStatic
        fun changeContent(element: NixString, range: TextRange, newContent: String) =
            NixStringManipulator().handleContentChange(element, range, newContent)

        @JvmStatic
        fun rangeInElement(element: NixString) =
            NixStringManipulator().getRangeInElement(element)
    }
}
