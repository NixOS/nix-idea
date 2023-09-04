package org.nixos.idea.lsp;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.lsp.api.LspServerSupportProvider;
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.file.NixFileType;

@SuppressWarnings("UnstableApiUsage")
public class NixLspServerSupportProvider implements LspServerSupportProvider {
    @Override
    public void fileOpened(@NotNull Project project, @NotNull VirtualFile virtualFile, @NotNull LspServerStarter lspServerStarter) {
        if (virtualFile.getFileType() == NixFileType.INSTANCE) {
            lspServerStarter.ensureServerStarted(new NixLspServerDescriptor(project));
        }
    }

    private static final class NixLspServerDescriptor extends ProjectWideLspServerDescriptor {
        public NixLspServerDescriptor(@NotNull Project project) {
            super(project, "Nix");
        }

        @Override
        public @NotNull GeneralCommandLine createCommandLine() throws ExecutionException {
            return new GeneralCommandLine("nix", "--extra-experimental-features", "nix-command", "--extra-experimental-features", "flakes", "run", "nixpkgs#nil");
        }

        @Override
        public boolean isSupportedFile(@NotNull VirtualFile virtualFile) {
            return virtualFile.getFileType() == NixFileType.INSTANCE;
        }
    }
}
