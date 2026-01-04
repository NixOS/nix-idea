package org.nixos.idea.lsp;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor;
import com.intellij.util.execution.ParametersListUtil;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.ConfigurationItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.file.NixFileType;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
final class NixLspServerDescriptor extends ProjectWideLspServerDescriptor {

    NixLspServerDescriptor(@NotNull Project project) {
        super(project, "Nix");
    }

    @Override
    public @NotNull ClientCapabilities getClientCapabilities() {
        ClientCapabilities clientCapabilities = super.getClientCapabilities();
        clientCapabilities.getWorkspace().setConfiguration(true);
        return clientCapabilities;
    }

    @Override
    public @NotNull GeneralCommandLine createCommandLine() throws ExecutionException {
        List<String> argv = ParametersListUtil.parse(NixLspSettings.getInstance(getProject()).getCommand(), false, true);
        return new GeneralCommandLine(argv);
    }

    @Override
    public @Nullable Object getWorkspaceConfiguration(@NotNull ConfigurationItem item) {
        return Optional.ofNullable(NixLspSettings.getInstance(getProject()).getConfiguration())
                .map(JsonParser::parseString)
                .map(JsonElement::getAsJsonObject)
                .map(jsonObject -> jsonObject.get(item.getSection()))
                .orElse(null);
    }

    @Override
    public boolean isSupportedFile(@NotNull VirtualFile virtualFile) {
        return virtualFile.getFileType() == NixFileType.INSTANCE;
    }
}
