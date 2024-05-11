package org.nixos.idea.lang.references.symbol;

import com.intellij.icons.AllIcons;
import com.intellij.model.Pointer;
import com.intellij.platform.backend.presentation.TargetPresentation;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.lang.builtins.NixBuiltin;
import org.nixos.idea.lang.highlighter.NixTextAttributes;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
final class NixBuiltinSymbol extends NixSymbol
        implements /*DocumentationTarget, */Pointer<NixBuiltinSymbol> {

    private final @NotNull NixBuiltin myBuiltin;

    NixBuiltinSymbol(@NotNull NixBuiltin builtin) {
        myBuiltin = builtin;
    }

    @Override
    public @NotNull String getName() {
        return myBuiltin.name();
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
        return Commons.buildPresentation(myBuiltin.name(), AllIcons.Nodes.Padlock, NixTextAttributes.BUILTIN)
                .presentation();
    }

//    @Override
//    public @NotNull TargetPresentation computePresentation() {
//        return presentation();
//    }
//
//    @Override
//    public @Nullable String computeDocumentationHint() {
//        return myBuiltin.name();
//    }
//
//    @Override
//    public @Nullable DocumentationResult computeDocumentation() {
//        // TODO: Implement
//        return DocumentationResult.documentation(myBuiltin.name());
//    }

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
