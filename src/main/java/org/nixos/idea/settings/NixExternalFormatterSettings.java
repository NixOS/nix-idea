package org.nixos.idea.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;

@State(name = "NixExternalFormatterSettings", storages = @Storage(value = "nix-idea-ext-fmt.xml", roamingType = RoamingType.DISABLED))
public final class NixExternalFormatterSettings implements PersistentStateComponent<NixExternalFormatterSettings.State> {

    // TODO: Use RoamingType.LOCAL with 2024.1

    // Documentation:
    // https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html

    private static final int MAX_HISTORY_SIZE = 5;

    private @NotNull State myState = new State();

    public static @NotNull NixExternalFormatterSettings getInstance() {
        return ApplicationManager.getApplication().getService(NixExternalFormatterSettings.class);
    }

    public boolean isFormatEnabled() {
        return myState.formatEnabled;
    }

    public void setFormatEnabled(boolean enabled) {
        myState.formatEnabled = enabled;
    }

    public @NotNull String getFormatCommand() {
        return myState.formatCommand;
    }

    public void setFormatCommand(@NotNull String command) {
        myState.formatCommand = command;
        addFormatCommandToHistory(command);
    }

    public @NotNull Collection<String> getCommandHistory() {
        return Collections.unmodifiableCollection(myState.formatCommandHistory);
    }

    private void addFormatCommandToHistory(@NotNull String command) {
        Deque<String> history = myState.formatCommandHistory;
        history.remove(command);
        history.addFirst(command);
        while (history.size() > MAX_HISTORY_SIZE) {
            history.removeLast();
        }
    }

    @SuppressWarnings("ClassEscapesDefinedScope")
    @Override
    public void loadState(@NotNull State state) {
        myState = state;
    }

    @SuppressWarnings("ClassEscapesDefinedScope")
    @Override
    public @NotNull State getState() {
        return myState;
    }

    static final class State {
        public boolean formatEnabled = false;
        public @NotNull String formatCommand = "";
        public Deque<String> formatCommandHistory = new ArrayDeque<>();
    }
}
