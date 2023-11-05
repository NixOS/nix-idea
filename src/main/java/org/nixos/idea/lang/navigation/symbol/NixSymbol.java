package org.nixos.idea.lang.navigation.symbol;

import com.intellij.find.usages.api.SearchTarget;
import com.intellij.find.usages.api.UsageHandler;
import com.intellij.model.Pointer;
import com.intellij.model.Symbol;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.interpretation.AttributePath;
import org.nixos.idea.lang.builtins.NixBuiltin;
import org.nixos.idea.psi.NixExpr;
import org.nixos.idea.psi.NixExprLambda;

@SuppressWarnings("UnstableApiUsage")
public abstract sealed class NixSymbol implements Symbol, SearchTarget
        permits NixAttributeSymbol, NixBuiltinSymbol, NixParameterSymbol {

    // TODO: Should I also implement PresentableSymbol?

    NixSymbol() {} // Can only be implemented within this package

    @Contract(pure = true)
    public static @NotNull NixSymbol builtin(@NotNull NixBuiltin builtin) {
        return new NixBuiltinSymbol(builtin);
    }

    @Contract(pure = true)
    public static @NotNull NixSymbol attribute(@NotNull NixExpr owner, @NotNull AttributePath path) {
        return new NixAttributeSymbol(owner, path);
    }

    @Contract(pure = true)
    public static @NotNull NixSymbol parameter(@NotNull NixExprLambda owner, @NotNull String name) {
        return new NixParameterSymbol(owner, name);
    }

    @Contract(pure = true)
    public abstract @NotNull org.nixos.idea.interpretation.Attribute getName();

    @Override
    public abstract @NotNull Pointer<? extends NixSymbol> createPointer();

    @Override
    public @Nullable SearchScope getMaximalSearchScope() {
        // TODO: Implement
        return SearchTarget.super.getMaximalSearchScope();
    }

    @Override
    public @NotNull UsageHandler getUsageHandler() {
        return UsageHandler.createEmptyUsageHandler(presentation().getPresentableText());
    }
}
