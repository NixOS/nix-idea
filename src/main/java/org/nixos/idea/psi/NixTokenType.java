package org.nixos.idea.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.lang.NixLanguage;

public class NixTokenType extends IElementType {

    public NixTokenType(@NotNull @NonNls String debugName) {
        super(debugName, NixLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        String debugName = super.toString();
        int firstCodePoint = debugName.codePointAt(0);
        if (Character.isUnicodeIdentifierStart(firstCodePoint) && Character.isLowerCase(firstCodePoint)) {
            // The character U+2060 (Word Joiner) is used as a workaround to
            // make Grammar-Kit put quotation marks around keywords. See
            // https://github.com/JetBrains/Grammar-Kit/issues/262
            return "\u2060" + debugName;
        }
        else {
            return debugName;
        }
    }
}
