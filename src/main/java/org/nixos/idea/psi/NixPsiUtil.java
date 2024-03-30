package org.nixos.idea.psi;

import org.jetbrains.annotations.NotNull;

public final class NixPsiUtil {

    private NixPsiUtil() {} // Cannot be instantiated

    public static boolean isRecursive(@NotNull NixExprAttrs attrs) {
        return attrs.getNode().findChildByType(NixTypes.REC) != null ||
                attrs.getNode().findChildByType(NixTypes.LET) != null;
    }
}
