package org.nixos.idea.lang.references.symbol;

import com.intellij.model.Pointer;
import com.intellij.platform.backend.presentation.TargetPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.lang.highlighter.NixTextAttributes;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
final class NixAdHocSymbol extends NixSymbol {

    private final @NotNull NixSymbol myBase;
    private final @NotNull String myAttributeName;
    private @Nullable Pointer<NixAdHocSymbol> myPointer = null;

    NixAdHocSymbol(@NotNull NixSymbol base, @NotNull String attributeName) {
        // TODO: Maybe we should get NixExpr as first parameter? What is with NixSymbolResolver.noop for example.
        this(base, attributeName, null);
    }

    private NixAdHocSymbol(@NotNull NixSymbol base, @NotNull String attributeName, @Nullable Pointer<NixAdHocSymbol> pointer) {
        myBase = base;
        myAttributeName = attributeName;
        myPointer = pointer;
    }

    @Override
    public @NotNull String getName() {
        return myAttributeName;
    }

    @Override
    public @NotNull Pointer<NixAdHocSymbol> createPointer() {
        if (myPointer == null) {
            String attributeName = myAttributeName;
            MY_POINTER.compareAndSet(this, null, Pointer.<NixAdHocSymbol, NixSymbol>uroborosPointer(
                    myBase.createPointer(),
                    (base, pointer) -> new NixAdHocSymbol(base, attributeName, pointer)));
            Objects.requireNonNull(myPointer, "Pointer.uroborosPointer(...) must not return null");
            assert Objects.equals(myPointer.dereference(), this);
        }
        return myPointer;
    }

    @Override
    public @NotNull Collection<NixSymbol> resolve(@NotNull String attributeName) {
        return List.of(new NixAdHocSymbol(this, attributeName));
    }

    @Override
    public @NotNull TargetPresentation presentation() {
        return Commons.buildPresentation(myAttributeName, Commons.ICON_ATTRIBUTE, NixTextAttributes.IDENTIFIER)
                .presentation();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NixAdHocSymbol that = (NixAdHocSymbol) o;
        return Objects.equals(myBase, that.myBase) && Objects.equals(myAttributeName, that.myAttributeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myBase, myAttributeName);
    }

    // VarHandle mechanics
    private static final VarHandle MY_POINTER;
    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            MY_POINTER = l.findVarHandle(NixAdHocSymbol.class, "myPointer", Pointer.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
