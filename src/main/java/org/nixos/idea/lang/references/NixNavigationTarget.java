package org.nixos.idea.lang.references;

import com.intellij.model.Pointer;
import com.intellij.openapi.util.TextRange;
import com.intellij.platform.backend.navigation.NavigationRequest;
import com.intellij.platform.backend.navigation.NavigationTarget;
import com.intellij.platform.backend.presentation.TargetPresentation;
import com.intellij.psi.SmartPointerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.nixos.idea.psi.NixPsiElement;

@SuppressWarnings("UnstableApiUsage")
public final class NixNavigationTarget implements NavigationTarget {

    private final @NotNull NixPsiElement myIdentifier;
    private final @NotNull TargetPresentation myTargetPresentation;
    private @Nullable Pointer<NavigationTarget> myPointer;

    public NixNavigationTarget(@NotNull NixPsiElement identifier, @NotNull TargetPresentation targetPresentation) {
        myIdentifier = identifier;
        myTargetPresentation = targetPresentation;
    }

    private NixNavigationTarget(@NotNull Pointer<NavigationTarget> pointer,
                                @NotNull NixPsiElement identifier,
                                @NotNull TargetPresentation targetPresentation) {
        myIdentifier = identifier;
        myTargetPresentation = targetPresentation;
        myPointer = pointer;
    }

    @TestOnly
    TextRange getRangeInFile() {
        return myIdentifier.getTextRange();
    }

    @Override
    public @NotNull Pointer<NavigationTarget> createPointer() {
        if (myPointer == null) {
            TargetPresentation targetPresentation = myTargetPresentation;
            myPointer = Pointer.uroborosPointer(
                    SmartPointerManager.createPointer(myIdentifier),
                    (identifier, pointer) -> new NixNavigationTarget(pointer, identifier, targetPresentation));
        }
        return myPointer;
    }

    @Override
    public @NotNull TargetPresentation computePresentation() {
        return myTargetPresentation;
    }

    @Override
    public @Nullable NavigationRequest navigationRequest() {
        return NavigationRequest.sourceNavigationRequest(myIdentifier.getContainingFile(), myIdentifier.getTextRange());
    }
}
