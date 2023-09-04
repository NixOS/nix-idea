package org.nixos.idea.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.interpretation.Attribute;
import org.nixos.idea.interpretation.AttributePath;
import org.nixos.idea.psi.NixAttr;
import org.nixos.idea.psi.NixBindAttr;
import org.nixos.idea.psi.NixInheritedName;
import org.nixos.idea.psi.NixNamedElement;
import org.nixos.idea.psi.NixParam;
import org.nixos.idea.psi.NixParamName;
import org.nixos.idea.psi.NixPsiElement;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Objects;

abstract class AbstractNixNamedElement extends AbstractNixPsiElement implements NixNamedElement {

    private @Nullable AttributePath myAttributePath;
    private @NotNull NixPsiElement @Nullable [] myAttributeElements;

    AbstractNixNamedElement(@NotNull ASTNode node) {
        super(node);
        assert this instanceof NixParam || this instanceof NixParamName ||
                this instanceof NixBindAttr || this instanceof NixInheritedName
                : "Unknown subclass: " + getClass();
    }

    @Override
    public @NotNull PsiElement getNameIdentifier() {
        if (this instanceof NixParam) {
            return ((NixParam) this).getParamName();
        } else if (this instanceof NixParamName) {
            return this;
        } else if (this instanceof NixBindAttr) {
            return ((NixBindAttr) this).getAttrPath();
        } else if (this instanceof NixInheritedName) {
            return ((NixInheritedName) this).getAttr();
        } else {
            throw new IllegalStateException("Unknown subclass: " + getClass());
        }
    }

    @Override
    public @NotNull String getName() {
        return getNameIdentifier().getText();
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        // TODO: Change name.
        return null;
    }

    @Override
    public int getTextOffset() {
        PsiElement identifier = getNameIdentifier();
        return identifier.getNode().getStartOffset();
    }

    @Override
    public @NotNull AttributePath getAttributePath() {
        if (myAttributePath == null) {
            processAttributes();
        }
        return Objects.requireNonNull(myAttributePath);
    }

    @Override
    public @NotNull NixPsiElement @NotNull [] getAttributeElements() {
        if (myAttributeElements == null) {
            processAttributes();
        }
        return Objects.requireNonNull(myAttributeElements).clone();
    }

    private void processAttributes() {
        AttributePath attributePath;
        NixPsiElement[] attributeElements;
        if (this instanceof NixParam) {
            NixParamName paramName = ((NixParam) this).getParamName();
            attributePath = AttributePath.of(Attribute.of(paramName));
            attributeElements = new NixPsiElement[]{paramName};
        } else if (this instanceof NixParamName) {
            NixParamName paramName = (NixParamName) this;
            attributePath = AttributePath.of(Attribute.of(paramName));
            attributeElements = new NixPsiElement[]{paramName};
        } else if (this instanceof NixBindAttr) {
            NixAttr[] elements = ((NixBindAttr) this).getAttrPath().getAttrList().toArray(NixAttr[]::new);
            attributeElements = elements;
            attributePath = AttributePath.of(Arrays.stream(elements).map(Attribute::of).toArray(Attribute[]::new));
        } else if (this instanceof NixInheritedName) {
            NixAttr attr = ((NixInheritedName) this).getAttr();
            attributePath = AttributePath.of(Attribute.of(attr));
            attributeElements = new NixPsiElement[]{attr};
        } else {
            throw new IllegalStateException("Unknown subclass: " + getClass());
        }
        assert attributeElements.length == attributePath.size();
        MY_ATTRIBUTE_PATH.compareAndSet(this, null, attributePath);
        MY_ATTRIBUTE_ELEMENTS.compareAndSet(this, null, attributeElements);
    }

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        myAttributePath = null;
        myAttributeElements = null;
    }

    // VarHandle mechanics
    private static final VarHandle MY_ATTRIBUTE_PATH;
    private static final VarHandle MY_ATTRIBUTE_ELEMENTS;
    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            MY_ATTRIBUTE_PATH = l.findVarHandle(AbstractNixNamedElement.class, "myAttributePath", AttributePath.class);
            MY_ATTRIBUTE_ELEMENTS = l.findVarHandle(AbstractNixNamedElement.class, "myAttributeElements", NixPsiElement[].class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

}
