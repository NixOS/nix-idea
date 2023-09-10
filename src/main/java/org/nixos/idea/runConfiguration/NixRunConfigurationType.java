package org.nixos.idea.runConfiguration;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.icon.NixIcons;

import javax.swing.*;

public final class NixRunConfigurationType implements ConfigurationType, DumbAware {
    private static final String ID = "NixRunConfiguration";

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Nix";
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) String getConfigurationTypeDescription() {
        return "Evaluates a nix file";
    }

    @Override
    public Icon getIcon() {
        return NixIcons.FILE;
    }

    @Override
    public @NotNull @NonNls String getId() {
        return ID;
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[] {new NixRunConfigurationFactory(this)};
    }
}
