package org.nixos.idea.lsp;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.lsp.api.LspServer;
import com.intellij.platform.lsp.api.LspServerSupportProvider;
import com.intellij.platform.lsp.api.lsWidget.LspServerWidgetItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.file.NixFileType;
import org.nixos.idea.icon.NixIcons;

@SuppressWarnings("UnstableApiUsage")
public final class NixLspServerSupportProvider implements LspServerSupportProvider {
    @Override
    public void fileOpened(@NotNull Project project, @NotNull VirtualFile virtualFile, @NotNull LspServerStarter lspServerStarter) {
        if (virtualFile.getFileType() == NixFileType.INSTANCE) {
            NixLspSettings settings = NixLspSettings.getInstance(project);
            if (settings.isEnabled()) {
                lspServerStarter.ensureServerStarted(new NixLspServerDescriptor(project, settings));
            }
        }
    }

    @Override
    public @NotNull LspServerWidgetItem createLspServerWidgetItem(@NotNull LspServer lspServer, @Nullable VirtualFile currentFile) {
        return new LspServerWidgetItem(lspServer, currentFile, NixIcons.FILE, NixLspSettingsConfigurable.class);
    }
}
