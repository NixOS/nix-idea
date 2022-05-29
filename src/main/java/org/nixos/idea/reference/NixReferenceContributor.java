package org.nixos.idea.reference;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.psi.NixLiteral;
import org.nixos.idea.psi.NixStringText;

import java.util.Arrays;

import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.StandardPatterns.or;
import static com.intellij.patterns.StandardPatterns.string;

public class NixReferenceContributor extends PsiReferenceContributor {

    // Same as the pattern in Nix.flex.
    private static final String NIX_PATH_REGEX = "[a-zA-Z0-9._+-]*(\\/[a-zA-Z0-9._+-]+)+\\/?";

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar psiReferenceRegistrar) {
        psiReferenceRegistrar.registerReferenceProvider(
                or(
                        psiElement(NixLiteral.class),
                        psiElement(NixStringText.class)
                                .withText(string().matches(NIX_PATH_REGEX))
                ),
                new NixReferenceProvider()
        );
    }

    private static class NixReferenceProvider extends PsiReferenceProvider {

        @Override
        public boolean acceptsTarget(@NotNull PsiElement target) {
            return target instanceof PsiFileSystemItem;
        }

        @Override
        public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement psiElement,
                                                               @NotNull ProcessingContext processingContext) {
            FileReferenceSet fileReferenceSet = new FileReferenceSet(psiElement);
            FileReference[] references = fileReferenceSet.getAllReferences();
            FileReference lastReference = fileReferenceSet.getLastReference();
            FileReference defaultNixReference = fileReferenceSet.createFileReference(lastReference.getRangeInElement(), lastReference.getIndex() + 1, "default.nix");
            PsiReference[] allReferences = Arrays.copyOf(references, references.length + 1);
            allReferences[allReferences.length - 1] = defaultNixReference;
            return allReferences;
        }
    }
}
