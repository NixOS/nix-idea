package org.nixos.idea.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import org.nixos.idea.psi.impl.AbstractNixString.Companion.indentForNewText
import org.nixos.idea.util.NixStringUtil

class NixStringManipulator : AbstractElementManipulator<NixString>() {

    /**
     * This function's result changes the original text in the host language
     * when the fragment in the guest language changes
     */
    override fun handleContentChange(
        element: NixString,
        range: TextRange,
        newContent: String
    ): NixString {
        // TODO Handle Insertion of `{` after `$`
        // TODO Add separate tests
        // TODO Verify that the range makes sense.
        val oldText = element.text
        val newText = buildString((1.1 * oldText.length).toInt()) {
            append(oldText, 0, range.startOffset)
            when (element) {
                is NixStdString -> {
                    NixStringUtil.escapeStd(this, newContent)
                }
                is NixIndString -> {
                    val indent = indentForNewText(element)
                    NixStringUtil.escapeInd(this, newContent, indent, false, indent)
                }
                else -> throw IllegalStateException("Unexpected string type: " + element.javaClass)
            }
            append(oldText, range.endOffset, oldText.length)
        }
        return element.replace(NixElementFactory.createString(element.project, newText)) as NixString
    }

    override fun getRangeInElement(element: NixString): TextRange {
        val parts = element.stringParts
        return TextRange.create(parts.first().startOffset, parts.last().endOffset)
    }
}
