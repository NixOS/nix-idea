package org.nixos.idea.lang.references.symbol;

import com.intellij.model.Pointer;
import com.intellij.navigation.NavigatableSymbol;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.platform.backend.navigation.NavigationTarget;
import com.intellij.platform.backend.presentation.TargetPresentation;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.lang.highlighter.NixTextAttributes;
import org.nixos.idea.lang.references.NixSymbolDeclaration;
import org.nixos.idea.psi.NixDeclarationHost;
import org.nixos.idea.settings.NixSymbolSettings;

import javax.swing.Icon;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public final class NixUserSymbol extends NixSymbol
        implements NavigatableSymbol {

    private final @NotNull NixDeclarationHost myHost;
    private final @NotNull List<String> myPath;
    private final @NotNull Type myType;
    @SuppressWarnings("FieldMayBeFinal") // Modified via MY_POINTER (VarHandle)
    private @Nullable Pointer<NixUserSymbol> myPointer = null;

    public NixUserSymbol(@NotNull NixDeclarationHost host, @NotNull List<String> path, @NotNull Type type) {
        assert !path.isEmpty();
        myHost = host;
        myPath = List.copyOf(path);
        myType = type;
    }

    @Override
    public @NotNull String getName() {
        return myPath.get(myPath.size() - 1);
    }

    public @NotNull Collection<NixSymbolDeclaration> getDeclarations() {
        return myHost.getDeclarations(myPath);
    }

    @Override
    public @NotNull Collection<NixSymbol> resolve(@NotNull String attributeName) {
        List<NixSymbol> symbols = Stream.concat(
                ContainerUtil.createMaybeSingletonList(myHost.getSymbol(ContainerUtil.append(myPath, attributeName))).stream(),
                myHost.getFullDeclarations(myPath).stream()
                        .flatMap(resolver -> resolver.resolve(attributeName).stream())
        ).toList();
        return symbols.isEmpty() ? List.of(new NixAdHocSymbol(this, attributeName)) : symbols;
    }

    @Override
    public @NotNull Pointer<NixUserSymbol> createPointer() {
        if (myPointer == null) {
            MY_POINTER.compareAndSet(this, null, Pointer.<NixUserSymbol, NixDeclarationHost>uroborosPointer(
                    SmartPointerManager.createPointer(myHost),
                    (host, pointer) -> dereference(host, myPath, pointer)));
            Objects.requireNonNull(myPointer, "Pointer.uroborosPointer(...) must not return null");
        }
        return myPointer;
    }

    private static @Nullable NixUserSymbol dereference(@NotNull NixDeclarationHost host, @NotNull List<String> path,
                                                       @NotNull Pointer<NixUserSymbol> pointer) {
        NixUserSymbol symbol = host.getSymbol(path);
        if (symbol != null) {
            MY_POINTER.compareAndSet(symbol, null, pointer);
        }
        return symbol;
    }

    @Override
    public @NotNull TargetPresentation presentation() {
        // TODO: TargetPresentationBuilder.locationText should specify the module (e.g. <nixpkgs>).
        //  See also PsiElementNavigationTarget.
        @Nullable PsiFile file = myHost.getContainingFile();
        return Commons.buildPresentation(getName(), myType.icon, myType.nameAttributes)
                .locationText(file == null ? null : file.getName(), file == null ? null : file.getIcon(0))
                .presentation();
    }

    @Override
    public @Nullable SearchScope getMaximalSearchScope() {
        if (myType.localScope == null) {
            return super.getMaximalSearchScope();
        } else {
            assert myPath.size() == 1;
            return new LocalSearchScope(myHost, myType.localScope);
        }
    }

    @Override
    public @NotNull Collection<? extends NavigationTarget> getNavigationTargets(@NotNull Project project) {
        assert myHost.getProject().equals(project);
        Stream<NavigationTarget> targets = myHost.getDeclarations(myPath).stream().map(NixSymbolDeclaration::navigationTarget);
        if (NixSymbolSettings.getInstance().getJumpToFirstDeclaration()) {
            return targets.findFirst().map(List::of).orElse(List.of());
        } else {
            return targets.toList();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NixUserSymbol symbol = (NixUserSymbol) o;
        return Objects.equals(myHost, symbol.myHost) && Objects.equals(myPath, symbol.myPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myHost, myPath);
    }

    public enum Type {
        ATTRIBUTE(Commons.ICON_ATTRIBUTE, NixTextAttributes.IDENTIFIER, null),
        PARAMETER(Commons.ICON_PARAMETER, NixTextAttributes.PARAMETER, "Scope of Parameter"),
        VARIABLE(Commons.ICON_VARIABLE, NixTextAttributes.LOCAL_VARIABLE, "Scope of Variable"),

        ;

        private final @NotNull Icon icon;
        private final @NotNull TextAttributesKey nameAttributes;
        private final @Nullable String localScope;

        Type(@NotNull Icon icon, @NotNull TextAttributesKey nameAttributes, @Nullable String localScope) {
            this.icon = icon;
            this.nameAttributes = nameAttributes;
            this.localScope = localScope;
        }
    }

    // VarHandle mechanics
    private static final VarHandle MY_POINTER;
    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            MY_POINTER = l.findVarHandle(NixUserSymbol.class, "myPointer", Pointer.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
