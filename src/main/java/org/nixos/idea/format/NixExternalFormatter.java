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

        var command = nixSettings.getFormatCommand();
        List<String> argv = ParametersListUtil.parse(command, false, true);
        var commandLine = new GeneralCommandLine(argv);

        return new FormattingTask() {
            private @Nullable OSProcessHandler handler;
            private boolean canceled;

            @Override
            public void run() {
                synchronized (this) {
                    if (canceled) {
                        return;
                    }
                    try {
                        handler = new OSProcessHandler(commandLine.withCharset(StandardCharsets.UTF_8));
                    } catch (ExecutionException e) {
                        request.onError("NixIDEA", e.getMessage());
                        return;
                    }
                }
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
                    OutputStream processInput = handler.getProcessInput();
                    Files.copy(ioFile.toPath(), processInput);
                    processInput.close();
                } catch (IOException e) {
                    handler.destroyProcess();
                    request.onError("NixIDEA", e.getMessage());
                }
            }

            @Override
            public synchronized boolean cancel() {
                canceled = true;
                if (handler != null) {
                    handler.destroyProcess();
                }
                return true;
            }

            @Override
            public boolean isRunUnderProgress() {
                return true;
            }
        };
    }
}
