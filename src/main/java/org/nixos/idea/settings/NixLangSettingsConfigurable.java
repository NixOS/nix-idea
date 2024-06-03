package org.nixos.idea.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.lsp.ui.CommandSuggestionsPopup;

import javax.swing.*;
import java.util.List;

public class NixLangSettingsConfigurable implements SearchableConfigurable, Configurable.Beta {
    private static final List<CommandSuggestionsPopup.Suggestion> BUILTIN_SUGGESTIONS = List.of(
            CommandSuggestionsPopup.Suggestion.builtin("<html>Use <b>nixpkgs-fmt</b> from nixpkgs</html>",
                    "nix --extra-experimental-features \"nix-command flakes\" run nixpkgs#nixpkgs-fmt")
    );

    private @Nullable JBCheckBox myEnabled;
    private @Nullable RawCommandLineEditor myCommand;
    private @Nullable JBLabel myTextArea;

    @Override
    public @NotNull @NonNls String getId() {
        return "org.nixos.idea.settings.NixLangSettingsConfigurable";
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Nix";
    }

    @Override
    public @Nullable JComponent createComponent() {
        myEnabled = new JBCheckBox("Enable external formatter");
        myEnabled.addChangeListener(e -> updateUiState());

        myCommand = new RawCommandLineEditor();
        myCommand.getEditorField().getEmptyText().setText("Command to execute for formatting");
        myCommand.getEditorField().getAccessibleContext().setAccessibleName("Command to execute for formatting");

        myTextArea = new JBLabel();

        myTextArea.setText("Format Nix files via an external formatter. Source of focused file will be passed as standard input.");
        new CommandSuggestionsPopup(
                myCommand,
                NixLangSettings.getInstance().getCommandHistory(),
                BUILTIN_SUGGESTIONS
        ).install();


        return FormBuilder.createFormBuilder()
                .addComponent(new TitledSeparator("External Formatter Configuration"))
                .addComponent(myTextArea)
                .addComponent(myEnabled)
                .addLabeledComponent("Command: ", myCommand)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    @Override
    public void reset() {
        assert myEnabled != null;
        assert myCommand != null;

        NixLangSettings settings = NixLangSettings.getInstance();
        myEnabled.setSelected(settings.isFormatEnabled());
        myCommand.setText(settings.getFormatCommand());

        updateUiState();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void apply() throws ConfigurationException {
        assert myEnabled != null;
        assert myCommand != null;

        NixLangSettings settings = NixLangSettings.getInstance();
        settings.setFormatEnabled(myEnabled.isSelected());
        settings.setFormatCommand(myCommand.getText());
    }

    @Override
    public boolean isModified() {
        assert myEnabled != null;
        assert myCommand != null;

        NixLangSettings settings = NixLangSettings.getInstance();
        return Configurable.isCheckboxModified(myEnabled, settings.isFormatEnabled()) ||
                Configurable.isFieldModified(myCommand.getTextField(), settings.getFormatCommand());
    }

    private void updateUiState() {
        assert myEnabled != null;
        assert myCommand != null;

        myCommand.setEnabled(myEnabled.isSelected());
    }
}
