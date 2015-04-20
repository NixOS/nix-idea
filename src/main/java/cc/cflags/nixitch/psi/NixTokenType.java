package cc.cflags.nixitch.psi;

import cc.cflags.nixitch.lang.NixLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class NixTokenType extends IElementType {

    public NixTokenType(@NotNull @NonNls String debugName) {
        super(debugName, NixLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "NixTokenType." + super.toString();
    }
}