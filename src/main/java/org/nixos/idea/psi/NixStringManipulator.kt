package org.nixos.idea.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import org.nixos.idea.util.NixStringUtil

/**
 * Handles modifications on string content of any [NixString].
 * This class is used by the fragment editor of IntelliJ.
 */
class NixStringManipulator : AbstractElementManipulator<NixString>() {

    // TODO Can we use this for copy&paste into strings?

    override fun handleContentChange(
        element: NixString,
        range: TextRange,
        newContent: String,
    ): NixString {
        // TODO Handle Insertion of `{` after `$`
        // TODO Only replace the tokens which are affected? (performance impact?)
        val oldText = element.text
        val newText = buildString((1.1 * oldText.length).toInt()) {
            append(oldText, 0, range.startOffset)
            when (element) {
                is NixStdString -> {
                    NixStringUtil.escapeStd(this, newContent, 0)
                }

                is NixIndString -> {
                    val indent = NixPsiUtil.getIndent(element)
                    NixStringUtil.escapeInd(this, newContent, indent, false, 0)
                }

                else -> throw IllegalStateException("Unexpected string type: " + element.javaClass)
            }
            append(oldText, range.endOffset, oldText.length)
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
