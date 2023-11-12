package org.nixos.idea.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.model.psi.PsiSymbolDeclaration;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.interpretation.Attribute;
import org.nixos.idea.interpretation.AttributePath;
import org.nixos.idea.lang.navigation.psi.NixSymbolDeclaration;
import org.nixos.idea.lang.navigation.symbol.NixSymbol;
import org.nixos.idea.psi.NixAttr;
import org.nixos.idea.psi.NixBindAttr;
import org.nixos.idea.psi.NixDeclarationElement;
import org.nixos.idea.psi.NixDeclarationHost;
import org.nixos.idea.psi.NixExpr;
import org.nixos.idea.psi.NixExprLambda;
import org.nixos.idea.psi.NixInheritedName;
import org.nixos.idea.psi.NixParamName;
import org.nixos.idea.psi.NixPsiElement;
import org.nixos.idea.util.TextRangeFactory;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of the {@link NixDeclarationElement} interface.
 */
abstract class AbstractNixDeclarationElement extends AbstractNixPsiElement implements NixDeclarationElement {

    private static final Logger LOG = Logger.getInstance(AbstractNixDeclarationElement.class);
    private static final Key<CachedValue<NixDeclarationHost>> KEY_DECLARATION_HOST = Key.create("AbstractNixDeclarationElement.declarationHost");

    private @Nullable AttributePath myAttributePath;
    private @NotNull NixPsiElement @Nullable [] myAttributeElements;

    AbstractNixDeclarationElement(@NotNull ASTNode node) {
        super(node);
        if (!(this instanceof NixParamName) && !(this instanceof NixBindAttr) && !(this instanceof NixInheritedName)) {
            LOG.error("Unknown subclass: " + getClass());
        }
    }

    @Override
    public @NotNull NixDeclarationHost getDeclarationHost() {
        return CachedValuesManager.getCachedValue(this, KEY_DECLARATION_HOST, () -> {
            for (PsiElement parent = getParent(); parent != null; parent = parent.getParent()) {
                if (parent instanceof NixDeclarationHost declarationHost) {
                    return CachedValueProvider.Result.create(declarationHost, declarationHost);
                }
            }
            throw new IllegalStateException("NixDeclarationElement outside NixDeclarationHost");
        });
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

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        myAttributePath = null;
        myAttributeElements = null;
    }

    private void processAttributes() {
        AttributePath attributePath;
        NixPsiElement[] attributeElements;
        if (this instanceof NixParamName paramName) {
            attributePath = AttributePath.of(Attribute.of(paramName));
            attributeElements = new NixPsiElement[]{paramName};
        } else if (this instanceof NixBindAttr bindAttr) {
            NixAttr[] elements = bindAttr.getAttrPath().getAttrList().toArray(NixAttr[]::new);
            attributeElements = elements;
            attributePath = AttributePath.of(Arrays.stream(elements).map(Attribute::of).toArray(Attribute[]::new));
        } else if (this instanceof NixInheritedName inheritedName) {
            NixAttr attr = inheritedName.getAttr();
            attributePath = AttributePath.of(Attribute.of(attr));
            attributeElements = new NixPsiElement[]{attr};
        } else {
            throw new IllegalStateException("Unknown subclass: " + getClass());
        }
        assert attributeElements.length == attributePath.size();
        MY_ATTRIBUTE_PATH.compareAndSet(this, null, attributePath);
        MY_ATTRIBUTE_ELEMENTS.compareAndSet(this, null, attributeElements);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull Collection<? extends @NotNull PsiSymbolDeclaration> getOwnDeclarations() {
        List<PsiSymbolDeclaration> result = new ArrayList<>();
        for (int i = 0; i < getAttributePath().size(); i++) {
            AttributePath path = getAttributePath().prefix(i);
            NixPsiElement identifier = getAttributeElements()[i];

            // TODO: 12.11.2023 Review symbol creation process!
            NixSymbol symbol;
            if (this instanceof NixParamName) {
                assert path.size() == 1;
                symbol = NixSymbol.parameter(findParent(NixExprLambda.class), Objects.requireNonNull(path.get(0).getName()));
            } else {
                symbol = NixSymbol.attribute(findParent(NixExpr.class), path);
            }

            result.add(new NixSymbolDeclaration(this, TextRangeFactory.relative(identifier, this), symbol));
        }
        return result;
    }

    private <T extends PsiElement> T findParent(Class<T> type) {
        return PsiTreeUtil.getParentOfType(AbstractNixDeclarationElement.this, type, false);
    }

    // VarHandle mechanics
    private static final VarHandle MY_ATTRIBUTE_PATH;
    private static final VarHandle MY_ATTRIBUTE_ELEMENTS;
    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            MY_ATTRIBUTE_PATH = l.findVarHandle(AbstractNixDeclarationElement.class, "myAttributePath", AttributePath.class);
            MY_ATTRIBUTE_ELEMENTS = l.findVarHandle(AbstractNixDeclarationElement.class, "myAttributeElements", NixPsiElement[].class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

}
