package org.nixos.idea.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator

class NixStringManipulator : AbstractElementManipulator<NixStringText>() {

    /**
     * This function's result changes the original text in the host language
     * when the fragment in the guest language changes
     */
    override fun handleContentChange(
        element: NixStringText,
        range: TextRange,
        newContent: String
    ): NixStringText {
        // TODO This implementation is wrong as escaped and non-escaped strings a mixed together.
        //  The variable `replacement` is not supposed to be escaped,
        //  but `element.text` is from the Nix source code, and therefore escaped.
        //  We probably have to switch the call dependency and let `updateText` call this more generic method.
        val escaped = newContent
        val replacement = range.replace(element.text, escaped)
        return element.updateText(replacement) as NixStringText
    }

    override fun getRangeInElement(element: NixStringText): TextRange = when {
        element.textLength == 0 -> TextRange.EMPTY_RANGE
        else -> TextRange.from(0, element.textLength)
    }
}
