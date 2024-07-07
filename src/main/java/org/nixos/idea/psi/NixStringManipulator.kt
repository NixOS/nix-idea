package org.nixos.idea.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.refactoring.suggested.startOffset
import org.nixos.idea.psi.impl.AbstractNixString
import org.nixos.idea.util.NixIndStringUtil
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
    ): NixString? {
        val escaped = newContent
        val replacement = range.replace(element.text, escaped)
        return element.updateText(replacement) as? NixString
    }

    override fun getRangeInElement(element: NixString): TextRange = when {
        element.textLength == 0 -> TextRange.EMPTY_RANGE
        element is NixIndString && element.textLength < 4 -> TextRange(0, element.textLength)
        element is NixIndString -> TextRange(2, element.textLength - 2)
        // element is not IndString, so it must be StdString
        element.textLength == 1 -> TextRange(0, 1)
        else -> TextRange(1, element.textLength - 1)
    }
}