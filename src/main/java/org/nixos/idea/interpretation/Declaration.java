package org.nixos.idea.interpretation;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.psi.NixAttr;
import org.nixos.idea.psi.NixBind;
import org.nixos.idea.psi.NixBindAttr;
import org.nixos.idea.psi.NixBindInheritAttr;
import org.nixos.idea.psi.NixBindInheritVar;
import org.nixos.idea.psi.NixExprLambda;
import org.nixos.idea.psi.NixExprLet;
import org.nixos.idea.psi.NixIdentifier;
import org.nixos.idea.psi.NixLegacyLet;
import org.nixos.idea.psi.NixParam;
import org.nixos.idea.psi.NixParamName;
import org.nixos.idea.psi.NixParamSet;
import org.nixos.idea.psi.NixPsiElement;
import org.nixos.idea.psi.NixSet;
import org.nixos.idea.psi.NixTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Variable declaration. This class is shallowly immutable.
 *
 * @see NixPsiElement#getDeclarations()
 */
public final class Declaration {
    private final @NotNull NixPsiElement myElement;
    private final @NotNull NixPsiElement myScope;
    private final @NotNull AttributePath myUsedPath;
    private final @NotNull NixPsiElement[] myAttributeElements;

    private Declaration(@NotNull Builder builder) {
        assert builder.myAttributeElements.size() == builder.myAttributes.size();
        myElement = builder.myElement;
        myScope = builder.myScope;
        myUsedPath = AttributePath.of(builder.myAttributes);
        myAttributeElements = builder.myAttributeElements.toArray(NixPsiElement[]::new);
    }

    /**
     * The element which declares or defines the variable.
     * The following element types might be returned by this method:
     * <ul>
     *     <li>{@link NixBindAttr} for assignments in {@code let}-expressions or recursive sets.
     *     <li>{@link NixAttr} for the attributes behind the {@code inherit} keyword.
     *     <li>{@link NixParam} for function parameters in set notation.
     *     <li>{@link NixParamName} for function parameters outside the set notation.
     * </ul>
     *
     * @return Element which declares or defines the variable.
     */
    @Contract(pure = true)
    public @NotNull NixPsiElement element() {
        assert myElement instanceof NixBindAttr || myElement instanceof NixAttr ||
                myElement instanceof NixParam || myElement instanceof NixParamName
                : "element type does not match Javadoc: " + myElement.getClass();
        return myElement;
    }

    /**
     * The element which introduces the variable into the scope.
     * All elements below the returned element are able to access the variable.
     * The returned element might be one of the following types:
     * <ul>
     *     <li>{@link NixExprLet}
     *     <li>{@link NixLegacyLet}
     *     <li>{@link NixSet} (if the set is recursive)
     *     <li>{@link NixExprLambda}
     * </ul>
     *
     * @return The element which introduces the variable into the scope.
     */
    @Contract(pure = true)
    public @NotNull NixPsiElement scope() {
        assert myScope instanceof NixExprLet || myScope instanceof NixLegacyLet ||
                myScope instanceof NixSet || myScope instanceof NixExprLambda
                : "element type does not match Javadoc: " + myScope.getClass();
        return myScope;
    }

    /**
     * The attribute path which is declared.
     * For function parameters, the path may only contain one attribute, the parameter name.
     * For sets and {@code let}-expressions, the path may contain multiple attributes.
     *
     * @return The attribute path which is declared.
     */
    @Contract(pure = true)
    public @NotNull AttributePath path() {
        return myUsedPath;
    }

    /**
     * The elements defining the individual attributes ot the {@linkplain #path() attribute path}.
     * For function parameters, the array contains exactly one element of type {@link NixParamName}.
     * For {@code let}-expressions and recursive sets, the elements are of type {@link NixAttr}.
     *
     * @return The elements defining the individual attributes.
     */
    @Contract(pure = true)
    public @NotNull NixPsiElement @NotNull [] attributeElements() {
        assert Arrays.stream(myAttributeElements).allMatch(el -> el instanceof NixParamName || el instanceof NixAttr)
                : "element type does not match Javadoc";
        return myAttributeElements.clone();
    }

    /**
     * Returns all declarations introduced by the given element.
     * The {@linkplain #scope() scope} of all returned elements is the same as the element given to this method.
     * The method may return {@code null} when there is no element introduced into the scope.
     * You should usually use {@link NixPsiElement#getDeclarations()} instead of using this method directly.
     *
     * @param element The root of the scope for which all declarations shall be returned.
     * @return All declarations introduced to the scope by the given element, or {@code null}.
     */
    @Contract(pure = true)
    public static @Nullable Collection<Declaration> allOf(@NotNull NixPsiElement element) {
        if (element instanceof NixExprLet) {
            NixExprLet let = (NixExprLet) element;
            return collectDeclarations(let, let.getBindList());
        } else if (element instanceof NixLegacyLet) {
            NixLegacyLet let = (NixLegacyLet) element;
            return collectDeclarations(let, let.getBindList());
        } else if (element instanceof NixSet && element.getNode().findChildByType(NixTypes.REC) != null) {
            NixSet set = (NixSet) element;
            return collectDeclarations(set, set.getBindList());
        } else if (element instanceof NixExprLambda) {
            NixExprLambda lambda = (NixExprLambda) element;
            List<Declaration> declarations = new ArrayList<>();
            NixParamName mainParam = lambda.getParamName();
            if (mainParam != null) {
                declarations.add(new Builder(mainParam, lambda)
                        .addAttribute(mainParam)
                        .build());
            }
            NixParamSet paramSet = lambda.getParamSet();
            if (paramSet != null) {
                for (NixParam param : paramSet.getParamList()) {
                    declarations.add(new Builder(param, lambda)
                            .addAttribute(param.getParamName())
                            .build());
                }
            }
            return declarations;
        } else {
            return null;
        }
    }

    private static @NotNull Collection<Declaration> collectDeclarations(@NotNull NixPsiElement source, @NotNull List<NixBind> bindList) {
        List<Declaration> declarations = new ArrayList<>();
        for (NixBind bind : bindList) {
            if (bind instanceof NixBindAttr) {
                NixBindAttr bindAttr = (NixBindAttr) bind;
                Builder builder = new Builder(bindAttr, source);
                for (NixAttr attr : bindAttr.getAttrPath().getAttrList()) {
                    builder.addAttribute(attr);
                }
                declarations.add(builder.build());
            } else if (bind instanceof NixBindInheritAttr) {
                for (NixAttr attr : ((NixBindInheritAttr) bind).getAttrList()) {
                    declarations.add(new Builder(attr, source)
                            .addAttribute(attr)
                            .build());
                }
            } else {
                assert bind instanceof NixBindInheritVar : "Unexpected NixBind implementation: " + bind.getClass();
            }
        }
        return declarations;
    }

    private static final class Builder {
        private final @NotNull NixPsiElement myElement;
        private final @NotNull NixPsiElement myScope;
        private final @NotNull List<Attribute> myAttributes = new ArrayList<>();
        private final @NotNull List<NixPsiElement> myAttributeElements = new ArrayList<>();

        private Builder(@NotNull NixPsiElement element, @NotNull NixPsiElement scope) {
            myElement = element;
            myScope = scope;
        }

        @Contract("_ -> this")
        private @NotNull Builder addAttribute(@NotNull NixIdentifier element) {
            return addAttribute(Attribute.of(element), element);
        }

        @Contract("_ -> this")
        private @NotNull Builder addAttribute(@NotNull NixAttr element) {
            return addAttribute(Attribute.of(element), element);
        }

        @Contract("_, _ -> this")
        private @NotNull Builder addAttribute(@NotNull Attribute attribute, @NotNull NixPsiElement element) {
            myAttributes.add(attribute);
            myAttributeElements.add(element);
            return this;
        }

        @Contract(pure = true)
        private @NotNull Declaration build() {
            return new Declaration(this);
        }
    }
}
