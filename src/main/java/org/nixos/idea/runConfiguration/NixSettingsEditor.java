package org.nixos.idea.runConfiguration;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class NixSettingsEditor extends SettingsEditor<NixRunConfiguration> {
    private TextFieldWithBrowseButton myScriptName;
    private LabeledComponent labeledComponent1;
    private JPanel myRootPanel;

    @Override
    protected void resetEditorFrom(@NotNull NixRunConfiguration s) {

    }

    @Override
    protected void applyEditorTo(@NotNull NixRunConfiguration s) throws ConfigurationException {

    }

    @Override
    protected @NotNull JComponent createEditor() {
        return myRootPanel;
    }
}
