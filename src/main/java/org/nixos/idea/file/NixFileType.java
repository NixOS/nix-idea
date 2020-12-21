package org.nixos.idea.file;

import org.nixos.idea.icon.NixIcons;
import org.nixos.idea.lang.NixLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class NixFileType extends LanguageFileType {

    public static final NixFileType INSTANCE = new NixFileType();

    private NixFileType() {
        super(NixLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Nix file";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Nix language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "nix";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return NixIcons.FILE;
    }

}
