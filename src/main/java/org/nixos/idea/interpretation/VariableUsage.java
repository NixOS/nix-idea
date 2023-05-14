package org.nixos.idea.interpretation;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.psi.NixAttr;
import org.nixos.idea.psi.NixAttrPath;
import org.nixos.idea.psi.NixExpr;
import org.nixos.idea.psi.NixExprSelect;
import org.nixos.idea.psi.NixPsiElement;
import org.nixos.idea.psi.NixVariableAccess;

import java.util.ArrayList;
import java.util.List;

public final class VariableUsage {
    private final @NotNull NixPsiElement myElement;
    private final @NotNull AttributePath myUsedPath;
    private final @NotNull NixPsiElement[] myAttributeElements;

    private VariableUsage(@NotNull Builder builder) {
        assert !builder.myAttributes.isEmpty();
        assert builder.myAttributeElements.size() == builder.myAttributes.size();
        myElement = builder.myElement;
        myUsedPath = AttributePath.of(builder.myAttributes);
        myAttributeElements = builder.myAttributeElements.toArray(NixPsiElement[]::new);
    }

    @Contract(pure = true)
    public static @Nullable VariableUsage by(@NotNull NixPsiElement element) {
        if (element instanceof NixVariableAccess && !(element.getParent() instanceof NixExprSelect)) {
            NixVariableAccess value = (NixVariableAccess) element;
            return new Builder(value)
                    .addAttribute(value)
                    .build();
        } else if (element instanceof NixExprSelect) {
            NixExprSelect expr = (NixExprSelect) element;
            NixExpr value = expr.getValue();
            NixAttrPath attrPath = expr.getAttrPath();
            if (attrPath != null && value instanceof NixVariableAccess) {
                Builder builder = new Builder(expr).addAttribute((NixVariableAccess) value);
                for (NixAttr attr : attrPath.getAttrList()) {
                    builder.addAttribute(attr);
                }
                return builder.build();
            }
            return null;
        } else {
            return null;
        }
    }

    @Contract(pure = true)
    public @NotNull NixPsiElement element() {
        return myElement;
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
        private final @NotNull List<Attribute> myAttributes = new ArrayList<>();
        private final @NotNull List<NixPsiElement> myAttributeElements = new ArrayList<>();

        private Builder(@NotNull NixPsiElement element) {
            myElement = element;
        }

        @Contract("_ -> this")
        private @NotNull Builder addAttribute(@NotNull NixVariableAccess element) {
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
        private @NotNull VariableUsage build() {
            return new VariableUsage(this);
        }
    }
}
