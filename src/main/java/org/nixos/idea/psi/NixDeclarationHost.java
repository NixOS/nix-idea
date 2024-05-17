package org.nixos.idea.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.lang.references.NixSymbolDeclaration;
import org.nixos.idea.lang.references.NixSymbolResolver;
import org.nixos.idea.lang.references.symbol.NixUserSymbol;

import java.util.Collection;
import java.util.List;

/**
 * An element which may contain declarations.
 * There are two types of declaration hosts:
 * <ol>
 *     <li>Elements which declare variables.
 *         The declared variables are accessible in the subtree of this element.
 *         <ul>
 *             <li>{@link NixExprLet}
 *             <li>{@link NixExprLambda}
 *             <li>{@link NixExprAttrs} if {@linkplain NixPsiUtil#isRecursive(NixExprAttrs) recursive}
 *         </ul>
 *     <li>Elements which declare attributes.
 *         The attributes are accessible via the result of this expression.
 *         <ul>
 *             <li>{@link NixExprAttrs} if not a {@linkplain NixPsiUtil#isLegacyLet(NixExprAttrs) legacy let expression}
 *         </ul>
 * </ol>
 * These two cases are only implemented as one interface because
 * the implementation is effectively the same in case of {@link NixExprLet} and {@link NixExprAttrs}.
 */
public interface NixDeclarationHost extends NixPsiElement {
    /**
     * Whether declarations of this element may be accessible as variables.
     * If this method returns {@code true}, {@link #getSymbolForScope(String)} may be called to resolve a variable.
     *
     * @return {@code true} if the declarations shall be added to the scope.
     */
    boolean isDeclaringVariables();

    /**
     * Returns the symbol for the given variable name.
     * Must not be called when {@link #isDeclaringVariables()} returns {@code false}.
     * Symbols exposed via this method become available from {@link #getScope()} in all children.
     * The method returns {@code null} if no variable with the given name is declared from this element.
     *
     * @param variableName The name of the variable.
     * @return The symbol representing the variable, or {@code null}.
     */
    @Nullable NixUserSymbol getSymbolForScope(@NotNull String variableName);

    @Nullable NixUserSymbol getSymbol(@NotNull List<String> attributePath);

    @NotNull Collection<NixSymbolDeclaration> getDeclarations(@NotNull List<String> attributePath);

    @NotNull Collection<NixSymbolResolver> getFullDeclarations(@NotNull List<String> attributePath);
}
