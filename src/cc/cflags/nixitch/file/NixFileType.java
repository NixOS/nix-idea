package cc.cflags.nixitch.file;

import cc.cflags.nixitch.icon.NixIcons;
import cc.cflags.nixitch.lang.NixLanguage;
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
