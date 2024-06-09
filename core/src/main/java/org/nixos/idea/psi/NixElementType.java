package org.nixos.idea.psi;

import org.nixos.idea.lang.NixLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class NixElementType extends IElementType {

    public NixElementType(@NotNull @NonNls String debugName) {
        super(debugName, NixLanguage.INSTANCE);
    }

}
