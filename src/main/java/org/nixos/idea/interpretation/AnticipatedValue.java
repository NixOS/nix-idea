package org.nixos.idea.interpretation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.psi.NixExpr;

public sealed interface AnticipatedValue {

    default @Nullable AnticipatedValue resolve(@NotNull String attribute) {
        return null;
    }

    default @Nullable AnticipatedValue resolve(@NotNull AnticipatedValue attribute) {
        return null;
    }

    record String(java.lang.String str) implements AnticipatedValue {}

    record Lambda(NixExpr code) implements AnticipatedValue {}

    record Set(AttributeMap<NixExpr> expr) {
        //
    }
}
