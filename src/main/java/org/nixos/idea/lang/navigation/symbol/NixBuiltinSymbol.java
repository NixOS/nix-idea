package org.nixos.idea.lang.navigation.symbol;

import com.intellij.lang.documentation.DocumentationResult;
import com.intellij.lang.documentation.DocumentationTarget;
import com.intellij.model.Pointer;
import com.intellij.navigation.TargetPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.interpretation.Attribute;
import org.nixos.idea.lang.builtins.NixBuiltin;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
final class NixBuiltinSymbol extends NixSymbol
        implements DocumentationTarget, Pointer<NixBuiltinSymbol> {

    private final @NotNull NixBuiltin myBuiltin;

    NixBuiltinSymbol(@NotNull NixBuiltin builtin) {
        myBuiltin = builtin;
    }

    @Override
    public @NotNull Attribute getName() {
        return Attribute.of(myBuiltin.name());
    }

    @Override
    public @NotNull Pointer<NixBuiltinSymbol> createPointer() {
        return this;
    }

    @Override
    public @NotNull NixBuiltinSymbol dereference() {
        return this;
    }

    @Override
    public @NotNull TargetPresentation presentation() {
        // TODO: Implement
        return TargetPresentation.builder(myBuiltin.name()).presentation();
    }

    @Override
    public @Nullable String computeDocumentationHint() {
        return myBuiltin.name();
    }

    @Override
    public @Nullable DocumentationResult computeDocumentation() {
        // TODO: Implement
        return DocumentationResult.documentation(myBuiltin.name());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NixBuiltinSymbol builtin = (NixBuiltinSymbol) o;
        return Objects.equals(myBuiltin, builtin.myBuiltin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myBuiltin);
    }
}
