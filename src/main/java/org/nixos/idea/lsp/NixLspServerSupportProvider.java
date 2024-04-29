package org.nixos.idea.lsp;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.lsp.api.LspServerSupportProvider;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.file.NixFileType;

@SuppressWarnings("UnstableApiUsage")
public final class NixLspServerSupportProvider implements LspServerSupportProvider {
    @Override
    public void fileOpened(@NotNull Project project, @NotNull VirtualFile virtualFile, @NotNull LspServerStarter lspServerStarter) {
        if (virtualFile.getFileType() == NixFileType.INSTANCE) {
            NixLspSettings settings = NixLspSettings.getInstance();
            if (settings.isEnabled()) {
                lspServerStarter.ensureServerStarted(new NixLspServerDescriptor(project, settings));
            }
        }
    }

    // TODO: Uncomment with IDEA 2024.1
    //@Override
    //public @NotNull LspServerWidgetItem createLspServerWidgetItem(@NotNull LspServer lspServer, @Nullable VirtualFile currentFile) {
    //    return new LspServerWidgetItem(lspServer, currentFile, NixIcons.FILE, NixLspSettingsConfigurable.class);
    //}
}
