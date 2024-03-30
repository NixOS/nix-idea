package org.nixos.idea.lang.highlighter;

import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.file.NixFile;
import org.nixos.idea.interpretation.Attribute;
import org.nixos.idea.interpretation.AttributePath;
import org.nixos.idea.interpretation.VariableUsage;
import org.nixos.idea.lang.builtins.NixBuiltin;
import org.nixos.idea.lang.navigation.scope.Scope;
import org.nixos.idea.psi.NixDeclarationElement;
import org.nixos.idea.psi.NixDeclarationHost;
import org.nixos.idea.psi.NixExprAttrs;
import org.nixos.idea.psi.NixExprLambda;
import org.nixos.idea.psi.NixExprLet;
import org.nixos.idea.psi.NixPsiElement;

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
        if (element instanceof NixDeclarationElement declarationElement) {
            highlight(declarationElement.getAttributePath(), declarationElement.getAttributeElements(), declarationElement.getDeclarationHost());
        } else if (element instanceof NixPsiElement nixElement) {
            VariableUsage usage = VariableUsage.by(nixElement);
            if (usage != null) {
                String variableName = usage.path().first().getName();
                Scope.Source source = nixElement.getScope().getSource(variableName == null ? "" : variableName);
                if (source instanceof Scope.Source.Psi) {
                    // TODO: 17.11.2023 ...
                    highlight(usage.path(), usage.attributeElements(), source);
                } else if (source instanceof Scope.Source.Builtin) {
                    //
                }
            }
        }
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

    private void highlight(@NotNull AttributePath path, @NotNull PsiElement[] attributeElements, @Nullable NixDeclarationHost source) {
        StringBuilder pathStrBuilder = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            Attribute attribute = path.get(i);
            if (!attribute.isIdentifier()) {
                return;
            }

            pathStrBuilder.append(attribute);
            highlight(attributeElements[i], source, pathStrBuilder.toString());
            pathStrBuilder.append('.');
        }
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
