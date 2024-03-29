package org.nixos.idea.lang.highlighter;

import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.lang.ASTNode;
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
import org.nixos.idea.psi.NixExprLambda;
import org.nixos.idea.psi.NixExprLet;
import org.nixos.idea.psi.NixExprSelect;
import org.nixos.idea.psi.NixIdentifier;
import org.nixos.idea.psi.NixLegacyLet;
import org.nixos.idea.psi.NixParam;
import org.nixos.idea.psi.NixParamSet;
import org.nixos.idea.psi.NixSet;
import org.nixos.idea.psi.NixStdAttr;
import org.nixos.idea.psi.NixTypes;

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
        if (element instanceof NixIdentifier) {
            NixExpr value = (NixIdentifier) element;
            String identifier = value.getText();
            PsiElement source = findSource(element, identifier);
            highlight(value, source, identifier);
        } else if (element instanceof NixExprSelect) {
            NixExprSelect expr = (NixExprSelect) element;
            NixExpr value = expr.getValue();
            NixAttrPath attrPath = expr.getAttrPath();
            if (attrPath != null && value instanceof NixIdentifier) {
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
        if (element instanceof NixExprLet) {
            NixExprLet let = (NixExprLet) element;
            return iterateVariables(let.getBindList(), fullPath, action);
        } else if (element instanceof NixLegacyLet) {
            NixLegacyLet let = (NixLegacyLet) element;
            return iterateVariables(let.getBindList(), fullPath, action);
        } else if (element instanceof NixSet) {
            NixSet set = (NixSet) element;
            return set.getNode().findChildByType(NixTypes.REC) != null &&
                    iterateVariables(set.getBindList(), fullPath, action);
        } else if (element instanceof NixExprLambda) {
            NixExprLambda lambda = (NixExprLambda) element;
            ASTNode mainParam = lambda.getNode().findChildByType(NixTypes.ID);
            if (mainParam != null && action.test(mainParam.getPsi(), fullPath ? mainParam.getText() : null)) {
                return true;
            }
            NixParamSet paramSet = lambda.getParamSet();
            if (paramSet != null) {
                for (NixParam param : paramSet.getParamList()) {
                    ASTNode paramName = param.getNode().findChildByType(NixTypes.ID);
                    if (paramName != null && action.test(paramName.getPsi(), fullPath ? paramName.getText() : null)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean iterateVariables(@NotNull List<NixBind> bindList, boolean fullPath, @NotNull BiPredicate<PsiElement, String> action) {
        for (NixBind bind : bindList) {
            if (bind instanceof NixBindAttr) {
                NixBindAttr bindAttr = (NixBindAttr) bind;
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
            } else if (bind instanceof NixBindInherit) {
                for (NixAttr attr : ((NixBindInherit) bind).getAttrList()) {
                    if (attr instanceof NixStdAttr && action.test(attr, fullPath ? attr.getText() : null)) {
                        return true;
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
                source instanceof NixLegacyLet ||
                source instanceof NixSet) {
            return LOCAL_VARIABLE;
        } else if (source instanceof NixExprLambda) {
            return PARAMETER;
        } else {
            throw new IllegalArgumentException("Invalid source: " + source);
        }
    }

    private static @NotNull HighlightInfoType getHighlightingByBuiltin(@NotNull NixBuiltin builtin) {
        switch (builtin.highlightingType()) {
            case IMPORT: return IMPORT;
            case LITERAL: return LITERAL;
            case OTHER: return BUILTIN;
            default: throw new IllegalStateException("unknown type: " + builtin.highlightingType());
        }
    }

    private void highlight(@NotNull PsiElement element, @Nullable PsiElement source, @NotNull String attrPath) {
        HighlightInfoType type = null;
        if (!attrPath.contains(".")) {
            if (source != null) {
                type = getHighlightingBySource(source);
            }
            else {
                NixBuiltin builtin = NixBuiltin.resolveGlobal(attrPath);
                if (builtin != null) {
                    type = getHighlightingByBuiltin(builtin);
                }
            }
        }
        else if (attrPath.startsWith(BUILTINS_PREFIX)) {
            NixBuiltin builtin = NixBuiltin.resolveBuiltin(attrPath.substring(BUILTINS_PREFIX.length()));
            if (builtin != null) {
                type = getHighlightingByBuiltin(builtin);
            }
        }
        highlight(element, source, attrPath, type);
    }
}
