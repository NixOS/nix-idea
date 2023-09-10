package org.nixos.idea.runConfiguration;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NixRunConfiguration extends RunConfigurationBase<NixRunConfigurationOptions> {
    // TODO: 12.03.23 Evaluate, Built, Built and Run, Open Shell
    public NixRunConfiguration(@NotNull Project project, @Nullable NixRunConfigurationFactory factory, @Nullable String name) {
        super(project, factory, name);
    }

    @Override
    protected @NotNull NixRunConfigurationOptions getOptions() {
        return (NixRunConfigurationOptions) super.getOptions();
    }

    @Override
    public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new NixSettingsEditor();
    }

    @Override
    public @NotNull RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) throws ExecutionException {
        return new CommandLineState(environment) {
            @Override
            protected @NotNull ProcessHandler startProcess() throws ExecutionException {
                GeneralCommandLine commandLine = new GeneralCommandLine(getOptions().getScriptName());
                OSProcessHandler processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine);
                ProcessTerminatedListener.attach(processHandler, getProject());
                return processHandler;
            }
        };
    }
}
