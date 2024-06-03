package org.nixos.idea.format;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessAdapter;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.formatting.service.AsyncDocumentFormattingService;
import com.intellij.formatting.service.AsyncFormattingRequest;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiFile;
import com.intellij.util.execution.ParametersListUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.file.NixFile;
import org.nixos.idea.lang.NixLanguage;
import org.nixos.idea.settings.NixExternalFormatterSettings;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class NixExternalFormatter extends AsyncDocumentFormattingService {

    @Override
    protected @NotNull String getNotificationGroupId() {
        return NixLanguage.NOTIFICATION_GROUP_ID;
    }

    @Override
    protected @NotNull @NlsSafe String getName() {
        return "NixIDEA";
    }

    @Override
    public @NotNull Set<Feature> getFeatures() {
        return EnumSet.noneOf(Feature.class);
    }

    @Override
    public boolean canFormat(@NotNull PsiFile psiFile) {
        return psiFile instanceof NixFile;
    }


    @Override
    protected @Nullable FormattingTask createFormattingTask(@NotNull AsyncFormattingRequest request) {
        NixExternalFormatterSettings nixSettings = NixExternalFormatterSettings.getInstance();
        if (!nixSettings.isFormatEnabled()) {
            return null;
        }

        var ioFile = request.getIOFile();
        if (ioFile == null) return null;

        @NonNls
        var command = nixSettings.getFormatCommand();
        List<String> argv = ParametersListUtil.parse(command, false, true);

        var commandLine = new GeneralCommandLine(argv);

        try {
            var handler = new OSProcessHandler(commandLine.withCharset(StandardCharsets.UTF_8));
            OutputStream processInput = handler.getProcessInput();
            return new FormattingTask() {
                @Override
                public void run() {
                    handler.addProcessListener(new CapturingProcessAdapter() {

                        @Override
                        public void processTerminated(@NotNull ProcessEvent event) {
                            int exitCode = event.getExitCode();
                            if (exitCode == 0) {
                                request.onTextReady(getOutput().getStdout());
                            } else {
                                request.onError("NixIDEA", getOutput().getStderr());
                            }
                        }
                    });
                    handler.startNotify();
                    try {
                        Files.copy(ioFile.toPath(), processInput);
                        processInput.flush();
                        processInput.close();
                    } catch (IOException e) {
                        handler.destroyProcess();
                        request.onError("NixIDEA", e.getMessage());
                    }
                }

                @Override
                public boolean cancel() {
                    handler.destroyProcess();
                    return true;
                }

                @Override
                public boolean isRunUnderProgress() {
                    return true;
                }
            };
        } catch (ExecutionException e) {
            request.onError("NixIDEA", e.getMessage());
            return null;
        }
    }
}
