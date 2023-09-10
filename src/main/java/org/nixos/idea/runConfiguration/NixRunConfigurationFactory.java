package org.nixos.idea.runConfiguration;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NixRunConfigurationFactory extends ConfigurationFactory {
    public NixRunConfigurationFactory(@NotNull NixRunConfigurationType type) {
        super(type);
    }

    @Override
    public @NotNull @NonNls String getId() {
        return getType().getId();
    }

    @Override
    public boolean isApplicable(@NotNull Project project) {
        // TODO: Hide if there are no nix-files in the project
        return super.isApplicable(project);
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new NixRunConfiguration(project, this, "nix");
    }

    @Override
    public @NotNull Class<? extends BaseState> getOptionsClass() {
        return NixRunConfigurationOptions.class;
    }

    @Override
    public boolean isEditableInDumbMode() {
        return true;
    }
}
