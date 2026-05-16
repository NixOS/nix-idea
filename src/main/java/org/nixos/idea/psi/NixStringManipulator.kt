package org.nixos.idea.psi

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
        val globalStartOffset = element.startOffset
        val indent = NixPsiUtil.getIndent(element)

        val (firstToken, offsetInFirstToken) = let {
            val token = element.node.findLeafElementAt(range.startOffset)!!
            val offset = globalStartOffset + range.startOffset - token.startOffset
            if (offset == 0) {
                val prevToken = PsiTreeUtil.prevLeaf(token.psi)!!.node
                val prevTokenType = prevToken.elementType
                if (prevTokenType === NixTypes.STR || prevTokenType === NixTypes.IND_STR || prevTokenType === NixTypes.IND_STR_INDENT) {
                    return@let Pair(prevToken, prevToken.textLength)
                }
            }
            Pair(token, offset)
        }

        val lookback = let {
            val firstTokenType = firstToken.elementType
            if (firstTokenType === NixTypes.STR || firstTokenType === NixTypes.IND_STR) {
                offsetInFirstToken
            } else {
                0
            }
        }

        val newText = buildString((1.1 * oldText.length).toInt()) {
            when (element) {
                is NixStdString -> {
                    append(oldText, 0, range.startOffset)
                    NixStringUtil.escapeStd(this, newContent, lookback)
                    append(oldText, range.endOffset, oldText.length)
                }

                is NixIndString -> {
                    var indentStart = false
                    var indentEnd = indent
                    var startOffset = range.startOffset
                    var endOffset = range.endOffset

                    // If we start right after the indent, trim the ident and re-apply it later
                    if (firstToken.elementType === NixTypes.IND_STR_INDENT) {
                        indentStart = true
                        startOffset -= offsetInFirstToken.coerceAtMost(indent)
                    }

                    // Remove trailing indent and re-applied it later.
                    val nextPartialToken = element.node.findLeafElementAt(range.endOffset)!!
                    if (nextPartialToken.elementType === NixTypes.IND_STR_INDENT) {
                        val tokenEnd = nextPartialToken.startOffset + nextPartialToken.textLength.coerceAtMost(indent)
                        endOffset = (tokenEnd - globalStartOffset).coerceAtLeast(endOffset)
                    }
                    val closingQuotes = element.node.lastChildNode
                    if (endOffset == closingQuotes.startOffsetInParent) {
                        indentEnd = nextPartialToken
                            .let { if (nextPartialToken === closingQuotes) PsiTreeUtil.prevLeaf(nextPartialToken.psi)!!.node else it }
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

                    NixStringUtil.escapeInd(this, newContent, indent, indentStart, indentEnd, lookback)
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
