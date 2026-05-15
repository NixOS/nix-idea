package org.nixos.idea.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.injection.general.Injection;
import com.intellij.lang.injection.general.LanguageInjectionPerformer;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.util.NixStringUtil;

public class NixInjectionPerformer implements LanguageInjectionPerformer {

    @Override
    public boolean isPrimary() {
        return true;
    }

    @Override
    public boolean performInjection(@NotNull MultiHostRegistrar registrar, @NotNull Injection injection, @NotNull PsiElement context) {

        // TODO Support injection on quotes?
        //  It is currently not possible to press Alt+Enter on the quotes to enable language injections.
        // TODO Add InjectedLanguageManager.FRANKENSTEIN_INJECTION when there are interpolations?
        //  This seems to be used by various languages to disable certain error checks as when part of the source code is unavailable.
        // TODO JetBrains implementations of LanguageInjectionPerformer do more stuff,
        //  like calling InjectorUtils.registerSupport(...). Not sure whether this is relevant.
        // TODO Adding new interpolations after enabling a language injection has strange effect.
        // TODO What about LanguageInjectionSupport? https://plugins.jetbrains.com/docs/intellij/language-injection.html

        NixString string = PsiTreeUtil.getParentOfType(context, NixString.class, false);
        if (string == null) {
            return false;
        }
        Language injectedLanguage = injection.getInjectedLanguage();
        if (injectedLanguage == null) {
            return false;
        }

        LanguageFileType injectedFileType = injectedLanguage.getAssociatedFileType();
        String injectedFileExtension = injectedFileType == null ? null : injectedFileType.getDefaultExtension();

        registrar.startInjecting(injectedLanguage, injectedFileExtension);
        String prefix = injection.getPrefix();
        int maxIndent = NixStringUtil.detectMaxIndent(string);
        int interpolations = 0;
        for (NixStringPart stringPart : string.getStringParts()) {
            // Note: The list is never empty, all interpolations are surrounded by nodes of type NixStringText
            if (stringPart instanceof NixStringText text) {
                String suffix = text.getNextSibling() instanceof NixStringPart ? null : injection.getSuffix();
                int startOffset = text.getNode().getStartOffset();
                ASTNode nextToken = text.getNode().getFirstChildNode();
                do {
                    int endOffset = startOffset;
                    boolean newLine = false;
                    while (nextToken != null && !newLine) {
                        IElementType tokenType = nextToken.getElementType();
                        if (tokenType == NixTypes.IND_STR_LF) {
                            newLine = true;
                        } else if (tokenType == NixTypes.IND_STR_INDENT) {
                            startOffset += Math.min(nextToken.getTextLength(), maxIndent);
                        }
                        endOffset += nextToken.getTextLength();
                        nextToken = nextToken.getTreeNext();
                    }
                    registrar.addPlace(
                            prefix, nextToken == null ? suffix : null,
                            string, TextRange.create(startOffset, endOffset)
                    );
                    prefix = null;
                    startOffset = endOffset;
                } while (nextToken != null);
            } else {
                assert stringPart instanceof NixAntiquotation : stringPart.getClass();
                assert prefix == null;
                prefix = interpolationPlaceholder(interpolations++);
            }
        }
        registrar.doneInjecting();
        return true;
    }

    /**
     * Arbitrary code used in the guest language in place for string interpolations.
     * This code is not visible for the user in the IDE as IDEA will visualize source code of the interpolations instead.
     * This method returns a different string for each interpolation to prevent tooling of the guess language
     * from assuming that all interpolations generate the same result.
     */
    private static String interpolationPlaceholder(int index) {
        // TODO What should I return here? Would an empty string work?
        //      What do other plugins use as placeholders?
        //      I assume it should ideally be a valid expression or symbol in the guest language?
        return "(__interpolation" + index + "__)";
    }
}
