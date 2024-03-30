package org.nixos.idea.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.interpretation.AttributeMap;
import org.nixos.idea.psi.NixBind;
import org.nixos.idea.psi.NixBindAttr;
import org.nixos.idea.psi.NixBindInherit;
import org.nixos.idea.psi.NixDeclarationElement;
import org.nixos.idea.psi.NixDeclarationHost;
import org.nixos.idea.psi.NixExprAttrs;
import org.nixos.idea.psi.NixExprLambda;
import org.nixos.idea.psi.NixExprLet;
import org.nixos.idea.psi.NixInheritedName;
import org.nixos.idea.psi.NixParameter;
import org.nixos.idea.psi.NixPsiUtil;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of the {@link NixDeclarationHost} interface.
 */
abstract class AbstractNixDeclarationHost extends AbstractNixPsiElement implements NixDeclarationHost {

    private static final Logger LOG = Logger.getInstance(AbstractNixDeclarationHost.class);

    private @Nullable AttributeMap<NixDeclarationElement> myDeclarations;

    AbstractNixDeclarationHost(@NotNull ASTNode node) {
        super(node);
        if (!(this instanceof NixExprLet) && !(this instanceof NixExprAttrs) && !(this instanceof NixExprLambda)) {
            LOG.error("Unknown subclass: " + getClass());
        }
    }

    @Override
    public boolean isExpandingScope() {
        if (this instanceof NixExprLet || this instanceof NixExprLambda) {
            return true;
        } else if (this instanceof NixExprAttrs set) {
            return NixPsiUtil.isRecursive(set);
        } else {
            LOG.error("Unknown subclass: " + getClass());
            return false;
        }
    }

    @Override
    public @NotNull AttributeMap<NixDeclarationElement> getDeclarations() {
        if (myDeclarations == null) {
            MY_DECLARATIONS.compareAndSet(this, null, findDeclarations());
        }
        return Objects.requireNonNull(myDeclarations);
    }

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        myDeclarations = null;
    }

    private @NotNull AttributeMap<NixDeclarationElement> findDeclarations() {
        if (this instanceof NixExprLet let) {
            return collectBindDeclarations(let.getBindList());
        } else if (this instanceof NixExprAttrs set) {
            return collectBindDeclarations(set.getBindList());
        } else if (this instanceof NixExprLambda lambda) {
            AttributeMap.Builder<NixDeclarationElement> builder = AttributeMap.builder();
            for (NixParameter parameter : NixPsiUtil.getParameters(lambda)) {
                checkDeclarationHost(parameter);
                builder.add(parameter.getAttributePath(), parameter);
            }
            return builder.build();
        } else {
            LOG.error("Unknown subclass: " + getClass());
            return AttributeMap.empty();
        }
    }

    private @NotNull AttributeMap<NixDeclarationElement> collectBindDeclarations(@NotNull List<NixBind> bindList) {
        AttributeMap.Builder<NixDeclarationElement> builder = AttributeMap.builder();
        for (NixBind bind : bindList) {
            if (bind instanceof NixBindAttr bindAttr) {
                checkDeclarationHost(bindAttr);
                builder.add(bindAttr.getAttributePath(), bindAttr);
            } else if (bind instanceof NixBindInherit bindInherit) {
                for (NixInheritedName inheritedName : bindInherit.getInheritedNames()) {
                    checkDeclarationHost(inheritedName);
                    builder.add(inheritedName.getAttributePath(), inheritedName);
                }
            } else {
                LOG.error("Unexpected NixBind implementation: " + bind.getClass());
            }
        }
        return builder.build();
    }

    private void checkDeclarationHost(NixDeclarationElement declarationElement) {
        if (declarationElement.getDeclarationHost() != this) {
            LOG.error(String.format(
                    "%s.getDeclarationHost() inconsistent with %s.getDeclarations()",
                    declarationElement.getClass().getName(),
                    this.getClass().getName()));
        }
    }

    // VarHandle mechanics
    private static final VarHandle MY_DECLARATIONS;
    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            MY_DECLARATIONS = l.findVarHandle(AbstractNixDeclarationHost.class, "myDeclarations", AttributeMap.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
