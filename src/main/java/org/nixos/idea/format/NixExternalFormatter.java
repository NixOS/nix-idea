package org.nixos.idea.format;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessAdapter;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.formatting.service.AsyncDocumentFormattingService;
import com.intellij.formatting.service.AsyncFormattingRequest;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.execution.ParametersListUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.file.NixFile;
import org.nixos.idea.lang.NixLanguage;
import org.nixos.idea.settings.NixExternalFormatterSettings;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

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
        // When using `nix fmt` with `treefmt`,
        // we may ideally support detecting which files are supported by the formatter,
        // but currently we are only formatting `*.nix` files.
        return psiFile instanceof NixFile;
    }

    @Override
    protected @Nullable FormattingTask createFormattingTask(@NotNull AsyncFormattingRequest request) {
        NixExternalFormatterSettings formatterSettings = NixExternalFormatterSettings.getInstance();
        if (!formatterSettings.isEnabled()) {
            return null;
        }

        File ioFile = request.getIOFile();
        if (ioFile == null) return null;
        Path ioPath = ioFile.toPath();

        // `getIOFile()` returns the file backing the document when it is a local file,
        // and a temporary copy otherwise (this check mirrors the platform's logic).
        VirtualFile virtualFile = request.getContext().getVirtualFile();
        boolean formatsBackingFile = virtualFile != null
                && virtualFile.isInLocalFileSystem()
                && ioPath.equals(virtualFile.getFileSystem().getNioPath(virtualFile));

        Strategy strategy = switch (formatterSettings.getStrategy()) {
            case NIX_FMT -> new Strategy.NixFmtStrategy();
            case CUSTOM_COMMAND -> new Strategy.CustomCommandStrategy(formatterSettings);
        };

        boolean useStdIo = strategy.useStdIo();
        var commandLine = new GeneralCommandLine(strategy.command(ioPath));
        // Not sure if using UTF-8 is technically correct.
        // Should we use the encoding of the file, the shell, or neither?
        commandLine.withCharset(StandardCharsets.UTF_8);
        // If we don't use stdout, treat it the same as stderr.
        commandLine.withRedirectErrorStream(!useStdIo);

        if (strategy.runInFileDirectory()) {
            // Might not be appropriate if `ioPath` is a temporary file,
            // but not sure what to use instead in such cases.
            commandLine.withWorkingDirectory(ioPath.getParent());
        }

        return new FormattingTask() {
            private @Nullable OSProcessHandler handler;
            private boolean canceled;

            @Override
            public void run() {
                // No need to call FileDocumentManager.getInstance().saveDocument(...):
                // The platform has already saved it before this task was created.

                // Start formatter process
                synchronized (this) {
                    if (canceled) {
                        return;
                    }
                    try {
                        handler = new OSProcessHandler(commandLine);
                    } catch (ExecutionException e) {
                        request.onError("NixIDEA", e.getMessage());
                        return;
                    }
                }
                // Configure output handling
                handler.addProcessListener(new CapturingProcessAdapter() {
                    @Override
                    public void processTerminated(@NotNull ProcessEvent event) {
                        int exitCode = event.getExitCode();
                        if (useStdIo) {
                            if (exitCode == 0) {
                                request.onTextReady(getOutput().getStdout());
                            } else {
                                request.onError("NixIDEA", getOutput().getStderr());
                            }
                        } else {
                            if (formatsBackingFile) {
                                // The formatter may have modified the file backing the document in place,
                                // even if it failed or the request was canceled and the process got killed.
                                // Let the VFS pick up any external change.
                                // Passing the new text to onTextReady() instead would modify
                                // the document in parallel to the file and could trigger the
                                // "File Cache Conflict" dialog on the next VFS refresh.
                                VfsUtil.markDirtyAndRefresh(false, false, false, virtualFile);
                            }
                            if (exitCode == 0) {
                                if (formatsBackingFile) {
                                    request.onTextReady(null);
                                } else {
                                    // The formatter modified a temporary copy.
                                    // Read the result back and apply it to the document.
                                    // The platform wrote the copy with the file's charset,
                                    // and Document.setText() rejects CRLF line separators, so normalize accordingly.
                                    try {
                                        String text = Files.readString(ioPath,
                                                virtualFile != null ? virtualFile.getCharset() : StandardCharsets.UTF_8);
                                        request.onTextReady(StringUtil.convertLineSeparators(text));
                                    } catch (IOException e) {
                                        request.onError("NixIDEA", e.getMessage());
                                    }
                                }
                            } else {
                                // Use `getStdout()` as we enabled `withRedirectErrorStream`.
                                request.onError("NixIDEA", getOutput().getStdout());
                            }
                        }
                    }
                });
                handler.startNotify();
                // Write formatter input
                try {
                    OutputStream processInput = handler.getProcessInput();
                    if (useStdIo) {
                        processInput.write(request.getDocumentText().getBytes(StandardCharsets.UTF_8));
                    }
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
                    // Do not wait for the process to die: cancel() may be called on the EDT.
                    // While this introduces the potential of a race condition,
                    // there seems to be no good (not overly complicated) way to handle it.
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

    private interface Strategy {

        List<String> command(Path path);

        boolean runInFileDirectory();

        boolean useStdIo();

        private static String escapePath(Strategy strategy, Path path) {
            if (strategy.runInFileDirectory()) {
                // Prefix with `./` to escape files starting with a hyphen (`-`).
                return "./" + path.getFileName();
            } else if (path.isAbsolute()) {
                return path.toString();
            } else {
                return "./" + path;
            }
        }

        final class NixFmtStrategy implements Strategy {

            // In the future, we may try to detect the formatter implementation.
            // If the executable is named `treefmt`, we may instead use the following command:
            //
            //   nix fmt -- --stdin -- {file-name}
            //
            // The current implementation restrains itself to the
            // following convention documented by `nix fmt --help`:
            //
            // > Any arguments will be forwarded to the formatter.
            // > Typically these are the files to format.

            @Override
            public List<String> command(Path path) {
                return List.of(
                        "nix",
                        "--extra-experimental-features", "nix-command flakes",
                        "fmt",
                        // We may alternatively use two End of Options Indicators (bare `--`)
                        // for escaping the file name,
                        // but we don't know if the formatter complies to this convention.
                        // (The first indicator would be for `nix fmt`, the second one for the formatter)
                        escapePath(this, path));
            }

            @Override
            public boolean runInFileDirectory() {
                return true;
            }

            @Override
            public boolean useStdIo() {
                return false;
            }
        }

        final class CustomCommandStrategy implements Strategy {
            private final NixExternalFormatterSettings mySettings;

            CustomCommandStrategy(NixExternalFormatterSettings settings) {
                mySettings = settings;
            }

            @Override
            public List<String> command(Path path) {
                var command = mySettings.getFormatCommand();
                List<String> argv = ParametersListUtil.parse(command, false, true);
                return switch (mySettings.getFormatCommandInputMode()) {
                    case STDIO -> argv;
                    case CLI_ARGUMENT -> Stream.concat(
                            argv.stream(),
                            Stream.of(escapePath(this, path))
                    ).toList();
                };
            }

            @Override
            public boolean runInFileDirectory() {
                return mySettings.isFormatCommandRunInFileDirectory();
            }

            @Override
            public boolean useStdIo() {
                return switch (mySettings.getFormatCommandInputMode()) {
                    case STDIO -> true;
                    case CLI_ARGUMENT -> false;
                };
            }
        }
    }
}
