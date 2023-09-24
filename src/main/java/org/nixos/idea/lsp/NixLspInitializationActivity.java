package org.nixos.idea.lsp;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.wso2.lsp4intellij.IntellijLanguageClient;
import org.wso2.lsp4intellij.client.languageserver.serverdefinition.RawCommandServerDefinition;
import org.wso2.lsp4intellij.requests.Timeouts;

public final class NixLspInitializationActivity implements ProjectActivity {
    @Override
    public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        IntellijLanguageClient.setTimeout(Timeouts.INIT, 100_000);
        IntellijLanguageClient.setTimeout(Timeouts.SHUTDOWN, 10_000);
        IntellijLanguageClient.addServerDefinition(
                new RawCommandServerDefinition("nix", new String[]{
                        "nix", "--extra-experimental-features", "nix-command", "--extra-experimental-features", "flakes", "run", "nixpkgs#nil"}),
//                        "nix", "--extra-experimental-features", "nix-command", "--extra-experimental-features", "flakes", "run", "nixpkgs#nixd"}),
                project);
        return null;
    }
}
