package org.nixos.idea.lang.navigation.scope;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.interpretation.Attribute;
import org.nixos.idea.interpretation.AttributeMap;
import org.nixos.idea.lang.builtins.NixBuiltin;
import org.nixos.idea.psi.NixBind;
import org.nixos.idea.psi.NixDeclarationElement;
import org.nixos.idea.psi.NixDeclarationHost;
import org.nixos.idea.psi.NixExprLet;
import org.nixos.idea.psi.NixExprWith;
import org.nixos.idea.psi.NixPsiElement;
import org.nixos.idea.util.ImmutableLists;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable index of accessible variables in a specific scope.
 */
public final class Scope {
    public static final Scope EMPTY = withBuiltinsOnly();

    private final @NotNull Map<String, Source> mySources;
    private final @NotNull Source.With myFallback;

    private Scope(@NotNull Map<String, Source> sources, @NotNull Source.With fallback) {
        this.mySources = Map.copyOf(sources);
        this.myFallback = fallback;
    }

    /**
     * Returns the {@linkplain Source source} of the given variable.
     * Note that the source of the variable is not the same as the element which defines the variable.
     * For example a {@linkplain NixExprLet <code>let</code>-expression} may be the source of several variables,
     * but the variables would be defined by {@link NixBind}-elements within the {@code let}-expression.
     * If this method returns a source of type {@link Source.Psi},
     * the psi element will either be the element this scope belongs to, or a (potentially indirect) parent of that element.
     *
     * @param variableName The name of the variable.
     * @return Source of the variable with the given name.
     */
    @Contract(pure = true)
    public @NotNull Source getSource(@NotNull String variableName) {
        return mySources.getOrDefault(variableName, myFallback);
    }

    @Contract(pure = true)
    public @NotNull Scope subScope(@NotNull NixPsiElement element) {
        if (element instanceof NixExprWith withExpression) {
            return new Scope(mySources, new Source.With(ImmutableLists.append(myFallback.elements(), withExpression)));
        } else if (element instanceof NixDeclarationHost declarationHost && declarationHost.isExpandingScope()) {
            AttributeMap<NixDeclarationElement> declarations = declarationHost.getDeclarations();
            if (declarations.isEmpty()) {
                return this;
            }
            Map<String, Source> newSources = new HashMap<>(mySources);
            for (Attribute attribute : declarations.attributes()) {
                String variableName = attribute.getName();
                if (variableName != null) {
                    newSources.put(variableName, new Source.Psi(declarationHost));
                }
            }
            return new Scope(newSources, myFallback);
        } else {
            return this;
        }
    }

    private static @NotNull Scope withBuiltinsOnly() {
        Collection<NixBuiltin> globals = NixBuiltin.getAllGlobals();
        Map<String, Source> sources = new HashMap<>(globals.size());
        for (NixBuiltin global : globals) {
            sources.put(global.name(), new Source.Builtin(global));
        }
        return new Scope(sources, new Source.With(List.of()));
    }

    /**
     * The source which may introduce a specific variable.
     */
    public sealed interface Source {
        /**
         * Represents the {@link NixPsiElement} which introduces the variable into the scope.
         *
         * @param element The element represented by this object.
         * @see NixDeclarationElement#getDeclarationHost()
         */
        record Psi(NixDeclarationHost element) implements Source {}

        /**
         * Represents the {@link NixBuiltin} which is referenced by the variable.
         *
         * @param builtin The builtin represented by this object.
         */
        record Builtin(NixBuiltin builtin) implements Source {}

        /**
         * All the {@code with}-expressions which may define the variable.
         * This means that the variable was not defined by any other source.
         *
         * @param elements List of {@code with}-expressions above the current scope.
         */
        record With(List<NixExprWith> elements) implements Source {}
    }
}
