package org.nixos.idea.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator
import com.intellij.refactoring.suggested.startOffset
import org.nixos.idea.psi.impl.AbstractNixString
import org.nixos.idea.util.NixIndStringUtil
import org.nixos.idea.util.NixStringUtil

class NixStringManipulator : AbstractElementManipulator<NixString>() {

    /**
     * This function's result is in fact unused because
     * [AbstractNixString.updateText] does not do anything yet,
     */
    override fun handleContentChange(
        element: NixString,
        range: TextRange,
        newContent: String
    ): NixString? {
        var replacement = ""
        for (part in element.stringParts) {
            val parsed = NixStringUtil.parse(part)
            replacement = range.replace(element.text, parsed)
        }
        return (element as? AbstractNixString)?.updateText(replacement)
    }

    override fun getRangeInElement(element: NixString): TextRange =
        when {
            element.textLength == 0 -> TextRange.EMPTY_RANGE
            element is NixIndString && element.textLength < 4 -> TextRange(0, element.textLength)
            element is NixIndString -> TextRange(2, element.textLength - 2)
            // element is not IndString, so it must be StdString
            element.textLength == 1 -> TextRange(0, 1)
            else -> TextRange(1, element.textLength - 1)
        }
}