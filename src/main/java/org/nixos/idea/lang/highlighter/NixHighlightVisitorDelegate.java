package org.nixos.idea.lang.highlighter;

import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.file.NixFile;
import org.nixos.idea.lang.builtins.NixBuiltin;
import org.nixos.idea.psi.NixAttr;
import org.nixos.idea.psi.NixAttrPath;
import org.nixos.idea.psi.NixBind;
import org.nixos.idea.psi.NixBindAttr;
import org.nixos.idea.psi.NixBindInherit;
import org.nixos.idea.psi.NixExpr;
import org.nixos.idea.psi.NixExprAttrs;
import org.nixos.idea.psi.NixExprLambda;
import org.nixos.idea.psi.NixExprLet;
import org.nixos.idea.psi.NixExprSelect;
import org.nixos.idea.psi.NixExprVar;
import org.nixos.idea.psi.NixIdentifier;
import org.nixos.idea.psi.NixParameter;
import org.nixos.idea.psi.NixPsiUtil;
import org.nixos.idea.psi.NixStdAttr;

import java.util.List;
import java.util.function.BiPredicate;

/**
 * Delegate for the highlighting logic used by {@link NixHighlightVisitor} and {@link NixRainbowVisitor}.
 */
abstract class NixHighlightVisitorDelegate {

    private static final String BUILTINS_PREFIX = "builtins.";

    public static final HighlightInfoType LITERAL = new HighlightInfoType.HighlightInfoTypeImpl(HighlightInfoType.SYMBOL_TYPE_SEVERITY, NixTextAttributes.LITERAL, true);
    public static final HighlightInfoType IMPORT = new HighlightInfoType.HighlightInfoTypeImpl(HighlightInfoType.SYMBOL_TYPE_SEVERITY, NixTextAttributes.IMPORT, true);
    public static final HighlightInfoType BUILTIN = new HighlightInfoType.HighlightInfoTypeImpl(HighlightInfoType.SYMBOL_TYPE_SEVERITY, NixTextAttributes.BUILTIN, true);
    public static final HighlightInfoType LOCAL_VARIABLE = new HighlightInfoType.HighlightInfoTypeImpl(HighlightInfoType.SYMBOL_TYPE_SEVERITY, NixTextAttributes.LOCAL_VARIABLE, false);
    public static final HighlightInfoType PARAMETER = new HighlightInfoType.HighlightInfoTypeImpl(HighlightInfoType.SYMBOL_TYPE_SEVERITY, NixTextAttributes.PARAMETER, false);

    static boolean suitableForFile(@NotNull PsiFile file) {
        return file instanceof NixFile;
    }

    /**
     * Callback which gets called for usages of variables and their attributes in the given file.
     *
     * @param element  The element which refers to a variable or attribute.
     * @param source   The element which defines the corresponding variable. Is {@code null} if the source cannot be determined.
     * @param attrPath The used attribute path.
     * @param type     The type of variable, i.e. {@link #LOCAL_VARIABLE} or {@link #PARAMETER}.
     */
    abstract void highlight(@NotNull PsiElement element, @Nullable PsiElement source, @NotNull String attrPath, @Nullable HighlightInfoType type);

    void visit(@NotNull PsiElement element) {
        if (element instanceof NixExprVar value) {
            String identifier = value.getText();
            PsiElement source = findSource(element, identifier);
            highlight(value, source, identifier);
        } else if (element instanceof NixExprSelect expr) {
            NixExpr value = expr.getValue();
            NixAttrPath attrPath = expr.getAttrPath();
            if (attrPath != null && value instanceof NixExprVar) {
                String identifier = value.getText();
                PsiElement source = findSource(element, identifier);
                String pathStr = identifier;
                for (NixAttr nixAttr : expr.getAttrPath().getAttrList()) {
                    if (!(nixAttr instanceof NixStdAttr)) {
                        break;
                    }
                    pathStr = pathStr + '.' + nixAttr.getText();
                    highlight(nixAttr, source, pathStr);
                }
            }
        } else if (element instanceof NixStdAttr attr &&
                attr.getParent() instanceof NixBindInherit bindInherit &&
                bindInherit.getSource() == null) {
            String identifier = attr.getText();
            PsiElement source = findSource(attr, identifier);
            highlight(attr, source, identifier);
        } else {
            iterateVariables(element, true, (var, path) -> {
                highlight(var, element, path);
                return false;
            });
        }
    }

    private static @Nullable PsiElement findSource(@NotNull PsiElement context, @NotNull String identifier) {
        do {
            if (iterateVariables(context, false, (var, __) -> var.textMatches(identifier))) {
                return context;
            } else {
                context = context.getParent();
            }
        } while (context != null);
        return null;
    }

    private static boolean iterateVariables(@NotNull PsiElement element, boolean fullPath, @NotNull BiPredicate<PsiElement, String> action) {
        if (element instanceof NixExprLet let) {
            return iterateVariables(let.getBindList(), fullPath, action);
        } else if (element instanceof NixExprAttrs set) {
            return NixPsiUtil.isRecursive(set) &&
                    iterateVariables(set.getBindList(), fullPath, action);
        } else if (element instanceof NixExprLambda lambda) {
            for (NixParameter parameter : NixPsiUtil.getParameters(lambda)) {
                NixIdentifier identifier = parameter.getIdentifier();
                if (action.test(identifier, fullPath ? identifier.getText() : null)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean iterateVariables(@NotNull List<NixBind> bindList, boolean fullPath, @NotNull BiPredicate<PsiElement, String> action) {
        for (NixBind bind : bindList) {
            if (bind instanceof NixBindAttr bindAttr) {
                if (fullPath) {
                    List<NixAttr> attrs = bindAttr.getAttrPath().getAttrList();
                    NixAttr first = attrs.get(0);
                    if (!(first instanceof NixStdAttr)) {
                        continue;
                    }
                    String pathStr = first.getText();
                    if (action.test(first, pathStr)) {
                        return true;
                    }
                    for (NixAttr attr : attrs.subList(1, attrs.size())) {
                        if (!(attr instanceof NixStdAttr)) {
                            break;
                        }
                        pathStr = pathStr + '.' + attr.getText();
                        if (action.test(attr, pathStr)) {
                            return true;
                        }
                    }
                } else {
                    if (action.test(bindAttr.getAttrPath().getFirstAttr(), null)) {
                        return true;
                    }
                }
            } else if (bind instanceof NixBindInherit bindInherit) {
                // `let { inherit x; } in ...` does not actually introduce a new variable
                if (bindInherit.getSource() != null) {
                    for (NixAttr attr : bindInherit.getAttributes()) {
                        if (attr instanceof NixStdAttr && action.test(attr, fullPath ? attr.getText() : null)) {
                            return true;
                        }
                    }
                }
            } else {
                throw new IllegalStateException("Unexpected NixBind implementation: " + bind.getClass());
            }
        }
        return false;
    }

    private static @NotNull HighlightInfoType getHighlightingBySource(@NotNull PsiElement source) {
        if (source instanceof NixExprLet ||
                source instanceof NixExprAttrs) {
            return LOCAL_VARIABLE;
        } else if (source instanceof NixExprLambda) {
            return PARAMETER;
        } else {
            throw new IllegalArgumentException("Invalid source: " + source);
        }
    }

    private static @NotNull HighlightInfoType getHighlightingByBuiltin(@NotNull NixBuiltin builtin) {
        return switch (builtin.highlightingType()) {
            case IMPORT -> IMPORT;
            case LITERAL -> LITERAL;
            case OTHER -> BUILTIN;
        };
    }

    private void highlight(@NotNull PsiElement element, @Nullable PsiElement source, @NotNull String attrPath) {
        HighlightInfoType type = null;
        if (!attrPath.contains(".")) {
            if (source != null) {
                type = getHighlightingBySource(source);
            } else {
                NixBuiltin builtin = NixBuiltin.resolveGlobal(attrPath);
                if (builtin != null) {
                    type = getHighlightingByBuiltin(builtin);
                }
            }
        } else if (attrPath.startsWith(BUILTINS_PREFIX)) {
            NixBuiltin builtin = NixBuiltin.resolveBuiltin(attrPath.substring(BUILTINS_PREFIX.length()));
            if (builtin != null) {
                type = getHighlightingByBuiltin(builtin);
            }
        }
        highlight(element, source, attrPath, type);
    }
}
