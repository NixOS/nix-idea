package org.nixos.idea.lang.navigation.symbol;

import com.intellij.lang.documentation.DocumentationResult;
import com.intellij.lang.documentation.DocumentationTarget;
import com.intellij.model.Pointer;
import com.intellij.model.search.SearchRequest;
import com.intellij.navigation.NavigatableSymbol;
import com.intellij.navigation.NavigationTarget;
import com.intellij.navigation.TargetPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.interpretation.Attribute;
import org.nixos.idea.interpretation.AttributePath;
import org.nixos.idea.lang.navigation.NixNavigationTarget;
import org.nixos.idea.psi.NixDeclarationHost;
import org.nixos.idea.psi.NixExprAttrs;
import org.nixos.idea.psi.NixExprLet;
import org.nixos.idea.psi.NixTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
final class NixAttributeSymbol extends NixSymbol
        implements DocumentationTarget, NavigatableSymbol {

    private final @NotNull NixDeclarationHost myHost;
    private final @NotNull AttributePath myPath;
    private final @NotNull Pointer<NixAttributeSymbol> myPointer;

    NixAttributeSymbol(@NotNull NixDeclarationHost host, @NotNull AttributePath path) {
        this(host, path, Pointer.uroborosPointer(
                SmartPointerManager.createPointer(host),
                (owner1, pointer) -> new NixAttributeSymbol(host, path, pointer)));
    }

    private NixAttributeSymbol(@NotNull NixDeclarationHost host, @NotNull AttributePath path, @NotNull Pointer<NixAttributeSymbol> pointer) {
        myHost = host;
        myPath = path;
        myPointer = pointer;
        assert host instanceof NixExprAttrs || host instanceof NixExprLet;
    }

    @Override
    public @NotNull Attribute getName() {
        return myPath.last();
    }

    @Override
    public @NotNull Pointer<NixAttributeSymbol> createPointer() {
        assert this.equals(myPointer.dereference());
        return myPointer;
    }

    @Override
    public @NotNull TargetPresentation presentation() {
        // TODO: Implement
        return TargetPresentation.builder(myPath.last().toString())
                .presentation();
    }

    @Override
    public @Nullable DocumentationResult computeDocumentation() {
        // TODO: Implement
        return DocumentationTarget.super.computeDocumentation();
    }

    @Override
    public @Nullable SearchScope getMaximalSearchScope() {
        if ((myHost instanceof NixExprLet || myHost instanceof NixExprAttrs && myHost.getNode().findChildByType(NixTypes.LET) != null) && myPath.size() == 1) {
            return new LocalSearchScope(myHost);
        } else {
            return super.getMaximalSearchScope();
        }
    }

    @Override
    public @NotNull Collection<SearchRequest> getTextSearchRequests() {
        // TODO: Implement
        return super.getTextSearchRequests();
    }

    @Override
    public @NotNull Collection<? extends NavigationTarget> getNavigationTargets(@NotNull Project project) {
        List<NavigationTarget> result = new ArrayList<>();
        myHost.getDeclarations().walk(myPath, (attributePath, declaration) -> {
            result.add(NixNavigationTarget.of(declaration));
        });
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NixAttributeSymbol attribute = (NixAttributeSymbol) o;
        return Objects.equals(myHost, attribute.myHost) && Objects.equals(myPath, attribute.myPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myHost, myPath);
    }

}
