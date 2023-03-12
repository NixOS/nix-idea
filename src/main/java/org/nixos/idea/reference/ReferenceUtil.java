package org.nixos.idea.reference;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceService;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

public class ReferenceUtil {
    public static PsiReference @NotNull [] getReferences(@NotNull PsiElement element) {
        return ReferenceProvidersRegistry.getReferencesFromProviders(element, PsiReferenceService.Hints.NO_HINTS);
    }

    public static PsiReference getReference(@NotNull PsiElement element) {
        return ArrayUtil.getFirstElement(getReferences(element));
    }
}
