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
import org.nixos.idea.lang.navigation.NixNavigationTarget;
import org.nixos.idea.psi.NixExprLambda;
import org.nixos.idea.psi.NixParam;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
final class NixParameterSymbol extends NixSymbol
        implements DocumentationTarget, NavigatableSymbol {

    private final @NotNull NixExprLambda myOwner;
    private final @NotNull String myName;
    private final @NotNull Pointer<NixParameterSymbol> myPointer;

    NixParameterSymbol(@NotNull NixExprLambda owner, @NotNull String name) {
        this(owner, name, Pointer.uroborosPointer(
                SmartPointerManager.createPointer(owner),
                (owner1, pointer) -> new NixParameterSymbol(owner1, name, pointer)));
    }

    private NixParameterSymbol(@NotNull NixExprLambda owner, @NotNull String name, @NotNull Pointer<NixParameterSymbol> pointer) {
        myOwner = owner;
        myName = name;
        myPointer = pointer;
    }

    @Override
    public @NotNull Attribute getName() {
        return Attribute.of(myName);
    }

    @Override
    public @NotNull Pointer<NixParameterSymbol> createPointer() {
        assert this.equals(myPointer.dereference());
        return myPointer;
    }

    @Override
    public @NotNull TargetPresentation presentation() {
        // TODO: Implement
        return TargetPresentation.builder(myName)
                .presentation();
    }

    @Override
    public @Nullable DocumentationResult computeDocumentation() {
        // TODO: Implement
        return DocumentationTarget.super.computeDocumentation();
    }

    @Override
    public @NotNull SearchScope getMaximalSearchScope() {
        return new LocalSearchScope(myOwner);
    }

    @Override
    public @NotNull Collection<SearchRequest> getTextSearchRequests() {
        // TODO: Implement
        return super.getTextSearchRequests();
    }

    @Override
    public @NotNull Collection<? extends NavigationTarget> getNavigationTargets(@NotNull Project project) {
        assert myOwner.getProject().equals(project);
        return Stream.concat(
                        Stream.ofNullable(myOwner.getParamName()),
                        Stream.ofNullable(myOwner.getParamsInSet())
                                .flatMap(Collection::stream)
                                .map(NixParam::getParamName))
                .filter(paramName -> myName.equals(paramName.getAttributePath().get(0).getName()))
                .map(NixNavigationTarget::of)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NixParameterSymbol parameter = (NixParameterSymbol) o;
        return Objects.equals(myOwner, parameter.myOwner) && Objects.equals(myName, parameter.myName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myOwner, myName);
    }

}
