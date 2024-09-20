package org.nixos.idea.lsp;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.RoamingType;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.settings.NixStoragePaths;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;

@State(name = "NixLspSettings", storages = @Storage(value = NixStoragePaths.TOOLS, roamingType = RoamingType.LOCAL))
public final class NixLspSettings implements PersistentStateComponent<NixLspSettings.State> {

    // Documentation:
    // https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html

    private static final int MAX_HISTORY_SIZE = 5;

    private @NotNull State myState = new State();

    public static @NotNull NixLspSettings getInstance() {
        return ApplicationManager.getApplication().getService(NixLspSettings.class);
    }

    public boolean isEnabled() {
        return myState.enabled;
    }

    public void setEnabled(boolean enabled) {
        myState.enabled = enabled;
    }

    public @NotNull String getCommand() {
        return myState.command;
    }

    public void setCommand(@NotNull String command) {
        myState.command = command;
        addToHistory(command);
    }

    public @NotNull Collection<String> getCommandHistory() {
        return Collections.unmodifiableCollection(myState.history);
    }

    private void addToHistory(@NotNull String command) {
        Deque<String> history = myState.history;
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
        public boolean enabled = false;
        public @NotNull String command = "";
        public Deque<String> history = new ArrayDeque<>();
    }
}
