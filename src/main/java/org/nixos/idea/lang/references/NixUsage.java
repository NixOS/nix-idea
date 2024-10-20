package org.nixos.idea.lang.references;

import com.intellij.find.usages.api.PsiUsage;
import com.intellij.find.usages.api.ReadWriteUsage;
import com.intellij.find.usages.api.UsageAccess;
import com.intellij.model.Pointer;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.psi.NixPsiElement;
import org.nixos.idea.settings.NixSymbolSettings;

@SuppressWarnings("UnstableApiUsage")
final class NixUsage implements PsiUsage, ReadWriteUsage {

    private final @NotNull NixPsiElement myIdentifier;
    private final boolean myIsDeclaration;
    private @Nullable Pointer<NixUsage> myPointer;

    NixUsage(@NotNull NixSymbolDeclaration declaration) {
        myIdentifier = declaration.getIdentifier();
        myIsDeclaration = true;
    }

    NixUsage(@NotNull NixSymbolReference reference) {
        myIdentifier = reference.getIdentifier();
        myIsDeclaration = false;
    }

    private NixUsage(@NotNull Pointer<NixUsage> pointer, @NotNull NixPsiElement identifier, boolean isDeclaration) {
        myIdentifier = identifier;
        myIsDeclaration = isDeclaration;
        myPointer = pointer;
    }

    @Override
    public @NotNull Pointer<NixUsage> createPointer() {
        if (myPointer == null) {
            boolean isDeclaration = myIsDeclaration;
            myPointer = Pointer.uroborosPointer(
                    SmartPointerManager.createPointer(myIdentifier),
                    (identifier, pointer) -> new NixUsage(pointer, identifier, isDeclaration));
        }
        return myPointer;
    }

    @Override
    public @NotNull PsiFile getFile() {
        return myIdentifier.getContainingFile();
    }

    @Override
    public @NotNull TextRange getRange() {
        return myIdentifier.getTextRange();
    }

    @Override
    public boolean getDeclaration() {
        // IDEA removes all instances which return true from the result of the usage search
        return !NixSymbolSettings.getInstance().getShowDeclarationsAsUsages() && myIsDeclaration;
    }

    @Override
    public @Nullable UsageAccess computeAccess() {
        return myIsDeclaration ? UsageAccess.Write : UsageAccess.Read;
    }
}
