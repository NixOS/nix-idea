package org.nixos.idea.psi;

import com.intellij.lang.Language;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.injection.general.Injection;
import com.intellij.lang.injection.general.LanguageInjectionPerformer;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
        for (Place place : collectPlaces(string, injection.getPrefix(), injection.getSuffix())) {
            LiteralTextEscaper<?> escaper = place.item().createLiteralTextEscaper();
            registrar.addPlace(
                    place.prefix().toString(), place.suffix().toString(),
                    place.item(), escaper.getRelevantTextRange()
            );
        }
        registrar.doneInjecting();
        return true;
    }

    private record Place(@NotNull NixStringText item, @NotNull CharSequence prefix, @NotNull CharSequence suffix) {}

    private static List<Place> collectPlaces(@NotNull NixString string, @NotNull String prefix, @NotNull String suffix) {
        int interpolations = 0;
        StringBuilder prevSuffix = null;
        List<Place> result = new ArrayList<>();
        for (NixStringPart stringPart : string.getStringParts()) {
            // Note: The first and last part is always of type NixStringText, the list is never empty
            if (stringPart instanceof NixStringText text) {
                prevSuffix = new StringBuilder();
                result.add(new Place(text, prefix, prevSuffix));
                prefix = "";
            } else {
                assert stringPart instanceof NixAntiquotation : stringPart.getClass();
                assert prevSuffix != null;
                prevSuffix.append(interpolationPlaceholder(interpolations++));
            }
        }
        assert prevSuffix != null;
        prevSuffix.append(suffix);
        return result;
    }

    /**
     * Arbitrary code used in the guest language in place for string interpolations.
     * This code is not visible for the user in the IDE as IDEA will visualize source code of the interpolations instead.
     * This method returns a different string for each interpolation to prevent tooling of the guess language
     * from assuming that all interpolations generate the same result.
     */
    private static String interpolationPlaceholder(int index) {
        // TODO What should I return here? Would an empty string work? What do other plugins use as placeholders?
        return "(__interpolation" + index + "__)";
    }
}
