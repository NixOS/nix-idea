package org.nixos.idea.interpretation;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.psi.NixAttr;
import org.nixos.idea.psi.NixBind;
import org.nixos.idea.psi.NixBindAttr;
import org.nixos.idea.psi.NixBindInheritAttr;
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
import java.util.Collection;
import java.util.List;

public final class Declaration {
    private final @NotNull NixPsiElement myElement;
    private final @NotNull NixPsiElement mySource;
    private final @NotNull AttributePath myUsedPath;
    private final @NotNull NixPsiElement[] myAttributeElements;

    private Declaration(@NotNull Builder builder) {
        assert !builder.myAttributes.isEmpty();
        assert builder.myAttributeElements.size() == builder.myAttributes.size();
        myElement = builder.myElement;
        mySource = builder.mySource;
        myUsedPath = AttributePath.of(builder.myAttributes);
        myAttributeElements = builder.myAttributeElements.toArray(NixPsiElement[]::new);
    }

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
        }
        else {
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
                throw new IllegalStateException("Unexpected NixBind implementation: " + bind.getClass());
            }
        }
        return declarations;
    }

    @Contract(pure = true)
    public @NotNull NixPsiElement element() {
        return myElement;
    }

    @Contract(pure = true)
    public @NotNull NixPsiElement source() {
        return mySource;
    }

    @Contract(pure = true)
    public @NotNull AttributePath path() {
        return myUsedPath;
    }

    @Contract(pure = true)
    public @NotNull NixPsiElement @NotNull [] attributeElements() {
        return myAttributeElements.clone();
    }

    private static final class Builder {
        private final @NotNull NixPsiElement myElement;
        private final @NotNull NixPsiElement mySource;
        private final @NotNull List<Attribute> myAttributes = new ArrayList<>();
        private final @NotNull List<NixPsiElement> myAttributeElements = new ArrayList<>();

        private Builder(@NotNull NixPsiElement element, @NotNull NixPsiElement source) {
            myElement = element;
            mySource = source;
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
