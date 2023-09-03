package org.nixos.idea.interpretation;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.psi.NixBind;
import org.nixos.idea.psi.NixExprLet;
import org.nixos.idea.psi.NixPsiElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable index of accessible symbols.
 */
public final class Scope {
    public static final Scope EMPTY = new Scope(Map.of());

    private final @NotNull Map<String, NixPsiElement> mySources;

    private Scope(@NotNull Map<String, NixPsiElement> sources) {
        this.mySources = Map.copyOf(sources);
    }

    /**
     * Returns the element which introduces the variable from the given attribute path.
     * This method returns {@code null} if the variable cannot be found.
     *
     * @param path The attribute path containing the variable name as the first attribute.
     * @return Element which introduces the variable with the given name, or {@code null}.
     * @see #getOrigin(String)
     * @see Declaration#scope()
     */
    @Contract(pure = true)
    public @Nullable NixPsiElement getOrigin(@NotNull AttributePath path) {
        Attribute first = path.first();
        String variableName = first.getName();
        return variableName == null ? null : getOrigin(variableName);
    }

    /**
     * Returns the element which introduces the given variable into the scope.
     * Note that the element which introduces the variable is not the same as the element which defines the variable.
     * For example a {@linkplain NixExprLet <code>let</code>-expression} may introduce several variables,
     * but the variables would be defined by {@link NixBind}-elements within that expression.
     * If this scope belongs to an element, the returned value is either the element itself,
     * a (potentially indirect) parent element, or {@code null}.
     *
     * @param variableName The name of the variable.
     * @return Element which introduces the variable with the given name, or {@code null}.
     * @see Declaration#scope()
     */
    @Contract(pure = true)
    public @Nullable NixPsiElement getOrigin(@NotNull String variableName) {
        return mySources.get(variableName);
    }

    @Contract(pure = true)
    public @NotNull Collection<Declaration> resolveDeclarations(@NotNull AttributePath path) {
        NixPsiElement source = getOrigin(path);
        if (source == null) {
            return List.of();
        }
        List<Declaration> result = new ArrayList<>();
        source.getDeclarations().walk(path, (__, declarations) -> result.addAll(declarations));
        return Collections.unmodifiableCollection(result);
    }

    @Contract(pure = true)
    public @NotNull Scope subScope(@NotNull NixPsiElement origin, @NotNull AttributeMap<Collection<Declaration>> declarations) {
        assert declarations.values().stream().flatMap(Collection::stream).allMatch(declaration -> declaration.scope() == origin);
        if (declarations.isEmpty()) {
            return this;
        } else {
            Map<String, NixPsiElement> newSources = new HashMap<>(mySources);
            for (Attribute attribute : declarations.attributes()) {
                String variableName = attribute.getName();
                if (variableName != null) {
                    newSources.put(variableName, origin);
                }
            }
            return new Scope(newSources);
        }
    }
}
