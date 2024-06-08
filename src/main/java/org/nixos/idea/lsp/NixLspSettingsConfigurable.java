package org.nixos.idea.lsp;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.platform.lsp.api.LspServerManager;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.lsp.ui.CommandSuggestionsPopup;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.util.List;

public class NixLspSettingsConfigurable implements SearchableConfigurable, Configurable.Beta {
    private static final List<CommandSuggestionsPopup.Suggestion> BUILTIN_SUGGESTIONS = List.of(
            CommandSuggestionsPopup.Suggestion.builtin("<html>Use <b>nil</b> from nixpkgs</html>",
                    "nix --extra-experimental-features \"nix-command flakes\" run nixpkgs#nil"),
            CommandSuggestionsPopup.Suggestion.builtin("<html>Use <b>nixd</b> from nixpkgs</html>",
                    "nix --extra-experimental-features \"nix-command flakes\" run nixpkgs#nixd")
    );

    private @Nullable JBCheckBox myEnabled;
    private @Nullable RawCommandLineEditor myCommand;

    @Override
    public @NotNull @NonNls String getId() {
        return "org.nixos.idea.lsp.NixLspSettingsConfigurable";
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Nix Language Server (LSP)";
    }

    @Override
    public @Nullable JComponent createComponent() {
        myEnabled = new JBCheckBox("Enable language server");
        myEnabled.addChangeListener(e -> updateUiState());

        myCommand = new RawCommandLineEditor();
        myCommand.getEditorField().getEmptyText().setText("Command to start Language Server");
        myCommand.getEditorField().getAccessibleContext().setAccessibleName("Command to start Language Server");
        myCommand.getEditorField().setMargin(myEnabled.getMargin());
        new CommandSuggestionsPopup(myCommand, NixLspSettings.getInstance().getCommandHistory(), BUILTIN_SUGGESTIONS).install();

        return FormBuilder.createFormBuilder()
                .addComponent(myEnabled)
                .addComponent(new TitledSeparator("Language Server Configuration"))
                .addLabeledComponent("Command: ", myCommand)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    @Override
    public void reset() {
        assert myEnabled != null;
        assert myCommand != null;

        NixLspSettings settings = NixLspSettings.getInstance();
        myEnabled.setSelected(settings.isEnabled());
        myCommand.setText(settings.getCommand());

        updateUiState();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void apply() throws ConfigurationException {
        assert myEnabled != null;
        assert myCommand != null;

        NixLspSettings settings = NixLspSettings.getInstance();
        settings.setEnabled(myEnabled.isSelected());
        settings.setCommand(myCommand.getText());

        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
            LspServerManager.getInstance(project).stopAndRestartIfNeeded(NixLspServerSupportProvider.class);
        }
    }

    @Override
    public boolean isModified() {
        assert myEnabled != null;
        assert myCommand != null;

        NixLspSettings settings = NixLspSettings.getInstance();
        return Configurable.isCheckboxModified(myEnabled, settings.isEnabled()) ||
                Configurable.isFieldModified(myCommand.getTextField(), settings.getCommand());
    }

    private void updateUiState() {
        assert myEnabled != null;
        assert myCommand != null;

        myCommand.setEnabled(myEnabled.isSelected());
    }
}
