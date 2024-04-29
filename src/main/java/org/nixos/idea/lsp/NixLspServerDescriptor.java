package org.nixos.idea.lsp;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor;
import com.intellij.util.execution.ParametersListUtil;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.file.NixFileType;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
final class NixLspServerDescriptor extends ProjectWideLspServerDescriptor {

    private final String myCommand;

    NixLspServerDescriptor(@NotNull Project project, NixLspSettings settings) {
        super(project, "Nix");
        myCommand = settings.getCommand();
    }

    @Override
    public @NotNull GeneralCommandLine createCommandLine() throws ExecutionException {
        List<String> argv = ParametersListUtil.parse(myCommand, false, true);
        return new GeneralCommandLine(argv);
    }

    @Override
    public boolean isSupportedFile(@NotNull VirtualFile virtualFile) {
        return virtualFile.getFileType() == NixFileType.INSTANCE;
    }
}
