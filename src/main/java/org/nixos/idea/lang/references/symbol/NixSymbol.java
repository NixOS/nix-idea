package org.nixos.idea.lang.references.symbol;

import com.intellij.find.usages.api.SearchTarget;
import com.intellij.find.usages.api.UsageHandler;
import com.intellij.model.Pointer;
import com.intellij.model.Symbol;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.lang.builtins.NixBuiltin;
import org.nixos.idea.lang.references.NixSymbolResolver;

import java.util.Collection;

@SuppressWarnings("UnstableApiUsage")
public abstract sealed class NixSymbol implements Symbol, SearchTarget, NixSymbolResolver
        permits NixAdHocSymbol, NixBuiltinSymbol, NixUserSymbol {

    NixSymbol() {} // Can only be implemented within this package

    @Contract(pure = true)
    public static @NotNull NixSymbol builtin(@NotNull NixBuiltin builtin) {
        return new NixBuiltinSymbol(builtin);
    }

    @Contract(pure = true)
    public abstract @NotNull String getName();

    @Override
    public abstract @NotNull Pointer<? extends NixSymbol> createPointer();

    @Contract(pure = true)
    public abstract @NotNull Collection<NixSymbol> resolve(@NotNull String attributeName);

    @Override
    public @NotNull UsageHandler getUsageHandler() {
        return UsageHandler.createEmptyUsageHandler(getName());
    }
}
