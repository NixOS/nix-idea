package org.nixos.idea.lang.navigation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.interpretation.Attribute;
import org.nixos.idea.interpretation.AttributePath;
import org.nixos.idea.lang.navigation.symbol.NixSymbol;

public interface AttributeOwner {
    @Nullable NixSymbol resolveFast(@NotNull Attribute attribute);

    default @Nullable NixSymbol resolveFast(@NotNull AttributePath path) {
        NixSymbol current = resolveFast(path.first());
        for (int index = 1; index < path.size() && current != null; index++) {
            current = current.resolveFast(path.get(index));
        }
        return current;
    }

    default @Nullable NixSymbol resolveSlow(@NotNull Attribute attribute) {
        return resolveFast(attribute);
    }

    default @Nullable NixSymbol resolveSlow(@NotNull AttributePath path) {
        NixSymbol current = resolveSlow(path.first());
        for (int index = 1; index < path.size() && current != null; index++) {
            current = current.resolveSlow(path.get(index));
        }
        return current;
    }
}
