package cc.cflags.nixitch.file;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import cc.cflags.nixitch.lang.NixLanguage;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class NixFile extends PsiFileBase {
    public NixFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, NixLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return NixFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Nix File";
    }

    @Override
    public Icon getIcon(int flags) {
        return super.getIcon(flags);
    }
}

