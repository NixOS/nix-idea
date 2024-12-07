package org.nixos.idea.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.lang.references.NixSymbolDeclaration;
import org.nixos.idea.lang.references.symbol.NixUserSymbol;
import org.nixos.idea.psi.NixAttr;
import org.nixos.idea.psi.NixAttrPath;
import org.nixos.idea.psi.NixBind;
import org.nixos.idea.psi.NixBindAttr;
import org.nixos.idea.psi.NixBindInherit;
import org.nixos.idea.psi.NixDeclarationHost;
import org.nixos.idea.psi.NixExprAttrs;
import org.nixos.idea.psi.NixExprLambda;
import org.nixos.idea.psi.NixExprLet;
import org.nixos.idea.psi.NixIdentifier;
import org.nixos.idea.psi.NixParameter;
import org.nixos.idea.psi.NixPsiElement;
import org.nixos.idea.psi.NixPsiUtil;
import org.nixos.idea.settings.NixSymbolSettings;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Implementation of all instances of {@link NixDeclarationHost}.
 */
abstract class AbstractNixDeclarationHost extends AbstractNixPsiElement implements NixDeclarationHost {

    private static final Logger LOG = Logger.getInstance(AbstractNixDeclarationHost.class);

    private @Nullable Symbols mySymbols;

    AbstractNixDeclarationHost(@NotNull ASTNode node) {
        super(node);
        if (!(this instanceof NixExprLet) && !(this instanceof NixExprAttrs) && !(this instanceof NixExprLambda)) {
            LOG.error("Unknown subclass: " + getClass());
        }
    }

    @Override
    public final boolean isDeclaringVariables() {
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
    public @Nullable NixUserSymbol getSymbolForScope(@NotNull String variableName) {
        // Note: When we want to support dynamic attributes in the future, they must be ignored by this method.
        assert isDeclaringVariables() : "getSymbolForScope(...) must not be called when isDeclaringVariables() returns false";
        return getSymbols().getSymbolForScope(variableName);
    }

    @Override
    public final @Nullable NixUserSymbol getSymbol(@NotNull List<String> attributePath) {
        return getSymbols().getSymbol(attributePath);
    }

    @Override
    public final @NotNull Collection<NixSymbolDeclaration> getDeclarations(@NotNull List<String> attributePath) {
        return getSymbols().getDeclarations(attributePath);
    }

    static @NotNull Collection<NixSymbolDeclaration> getDeclarations(@NotNull AbstractNixPsiElement element) {
        AbstractNixDeclarationHost declarationHost = element.getDeclarationHost();
        if (declarationHost == null) {
            return List.of();
        }
        return declarationHost.getSymbols().getDeclarations(element);
    }

    private @NotNull Symbols getSymbols() {
        NixSymbolSettings settings = NixSymbolSettings.getInstance();
        Symbols symbols = mySymbols;
        if (symbols == null || symbols.isOutdated(settings)) {
            MY_SYMBOLS.compareAndSet(this, symbols, initSymbols(settings));
            Objects.requireNonNull(mySymbols, "initSymbols() must not return null");
        }
        return mySymbols;
    }

    private @NotNull Symbols initSymbols(@NotNull NixSymbolSettings settings) {
        Symbols symbols = new Symbols(settings);
        if (!NixSymbolSettings.getInstance().getEnabled()) {
            return symbols;
        } else if (this instanceof NixExprLet let) {
            collectBindDeclarations(symbols, let.getBindList(), true);
        } else if (this instanceof NixExprAttrs attrs) {
            collectBindDeclarations(symbols, attrs.getBindList(), NixPsiUtil.isLegacyLet(attrs));
        } else if (this instanceof NixExprLambda lambda) {
            for (NixParameter parameter : NixPsiUtil.getParameters(lambda)) {
                NixIdentifier identifier = parameter.getIdentifier();
                symbols.addParameter(parameter, identifier);
            }
        } else {
            LOG.error("Unknown subclass: " + getClass());
        }
        return symbols;
    }

    private void collectBindDeclarations(@NotNull Symbols result, @NotNull List<NixBind> bindList, boolean isVariable) {
        NixUserSymbol.Type type = isVariable ? NixUserSymbol.Type.VARIABLE : NixUserSymbol.Type.ATTRIBUTE;
        for (NixBind bind : bindList) {
            if (bind instanceof NixBindAttr bindAttr) {
                result.addBindAttr(bindAttr, bindAttr.getAttrPath(), type);
            } else if (bind instanceof NixBindInherit bindInherit) {
                for (NixAttr inheritedAttribute : bindInherit.getAttrList()) {
                    result.addInherit(bindInherit, inheritedAttribute, type, bindInherit.getExpr() != null);
                }
            } else {
                LOG.error("Unexpected NixBind implementation: " + bind.getClass());
            }
        }
    }

    private boolean checkDeclarationHost(@NotNull NixPsiElement element) {
        if (element instanceof AbstractNixPsiElement el) {
            if (el.getDeclarationHost() == this) {
                return true;
            }
            LOG.error("Element must belong to this declaration host");
        } else {
            LOG.error("Unexpected NixPsiElement implementation: " + element.getClass());
        }
        return false;
    }

    private final class Symbols {
        private final @NotNull Map<List<String>, NixUserSymbol> mySymbols = new HashMap<>();
        private final @NotNull Map<List<String>, List<NixSymbolDeclaration>> myDeclarationsBySymbol = new HashMap<>();
        private final @NotNull Map<NixPsiElement, List<NixSymbolDeclaration>> myDeclarationsByElement = new HashMap<>();
        private final @NotNull Set<String> myVariables = new HashSet<>();
        private final long mySettingsModificationCount;

        private Symbols(@NotNull NixSymbolSettings settings) {
            mySettingsModificationCount = settings.getStateModificationCount();
        }

        private boolean isOutdated(@NotNull NixSymbolSettings settings) {
            return mySettingsModificationCount != settings.getStateModificationCount();
        }

        private void addBindAttr(@NotNull NixPsiElement element, @NotNull NixAttrPath attrPath, @NotNull NixUserSymbol.Type type) {
            if (!checkDeclarationHost(element)) {
                return;
            }

            String elementName = attrPath.getText();
            List<String> path = new ArrayList<>();
            for (NixAttr attr : attrPath.getAttrList()) {
                String name = NixPsiUtil.getAttributeName(attr);
                if (name == null) {
                    return;
                }
                path.add(name);
                add(element, attr, path, type, true, elementName, null);
                type = NixUserSymbol.Type.ATTRIBUTE;
            }
        }

        private void addInherit(@NotNull NixPsiElement element, @NotNull NixAttr attr,
                                @NotNull NixUserSymbol.Type type, boolean exposeAsVariable) {
            String name = NixPsiUtil.getAttributeName(attr);
            if (checkDeclarationHost(element) && name != null) {
                add(element, attr, List.of(name),
                        type, exposeAsVariable, attr.getText(), "inherit");
            }
        }

        private void addParameter(@NotNull NixPsiElement element, @NotNull NixIdentifier identifier) {
            if (checkDeclarationHost(element)) {
                add(element, identifier,
                        List.of(identifier.getText()),
                        NixUserSymbol.Type.PARAMETER, true,
                        identifier.getText(), "lambda");
            }
        }

        private void add(@NotNull NixPsiElement element,
                         @NotNull NixPsiElement identifier,
                         @NotNull List<String> attributePath,
                         @NotNull NixUserSymbol.Type type,
                         boolean exposeAsVariable,
                         @NotNull String elementName,
                         @Nullable String elementType) {
            assert checkDeclarationHost(element);
            List<String> attributePathCopy = List.copyOf(attributePath);

            NixUserSymbol symbol = mySymbols.computeIfAbsent(attributePathCopy,
                    path -> new NixUserSymbol(AbstractNixDeclarationHost.this, path, type));
            if (exposeAsVariable && attributePath.size() == 1) {
                myVariables.add(attributePath.get(0));
            }

            NixSymbolDeclaration declaration = new NixSymbolDeclaration(element, identifier, symbol, elementName, elementType);
            myDeclarationsBySymbol.computeIfAbsent(attributePathCopy, __ -> new ArrayList<>())
                    .add(declaration);
            myDeclarationsByElement.computeIfAbsent(element, __ -> new ArrayList<>())
                    .add(declaration);
        }

        private @Nullable NixUserSymbol getSymbolForScope(@NotNull String variableName) {
            return myVariables.contains(variableName) ? mySymbols.get(List.of(variableName)) : null;
        }

        private @Nullable NixUserSymbol getSymbol(@NotNull List<String> attributePath) {
            return mySymbols.get(attributePath);
        }

        private @NotNull List<NixSymbolDeclaration> getDeclarations(@NotNull List<String> attributePath) {
            return myDeclarationsBySymbol.getOrDefault(attributePath, List.of());
        }

        private @NotNull List<NixSymbolDeclaration> getDeclarations(@NotNull NixPsiElement element) {
            return myDeclarationsByElement.getOrDefault(element, List.of());
        }
    }

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        mySymbols = null;
    }

    // VarHandle mechanics
    private static final VarHandle MY_SYMBOLS;
    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            MY_SYMBOLS = l.findVarHandle(AbstractNixDeclarationHost.class, "mySymbols", Symbols.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
