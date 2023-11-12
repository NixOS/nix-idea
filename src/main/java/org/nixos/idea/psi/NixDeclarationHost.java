package org.nixos.idea.psi;

import org.jetbrains.annotations.NotNull;
import org.nixos.idea.interpretation.AttributeMap;

/**
 * An element which may contain declarations.
 * While {@linkplain NixExprLet <code>let</code>-expressions} and {@linkplain NixExprLambda functions} may only declare variables,
 * {@linkplain NixSet sets} may declare attributes which are not added to the scope as variables.
 */
public interface NixDeclarationHost extends NixPsiElement {
    /**
     * Whether this element may introduce new variables into the scope.
     *
     * @return {@code true} if the declarations returned by {@link #getDeclarations()} shall be added to the scope.
     */
    boolean isExpandingScope();

    /**
     * Returns declarations owned by this construct.
     * This element is the {@linkplain NixDeclarationElement#getDeclarationHost() declaration host} of all returned declarations.
     *
     * @return Declarations owned by this construct.
     */
    @NotNull AttributeMap<NixDeclarationElement> getDeclarations();
}
