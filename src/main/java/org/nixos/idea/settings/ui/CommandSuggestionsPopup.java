package org.nixos.idea.settings.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.UiUtils;
import com.intellij.openapi.ui.popup.JBPopupListener;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.ListPopupStepEx;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.components.fields.ExpandableTextField;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.popup.list.ListPopupImpl;
import com.intellij.util.ui.StatusText;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public final class CommandSuggestionsPopup {

    // Implementation partially inspired by TextCompletionField

    private final @NotNull ExpandableTextField myEditor;
    private final @NotNull Collection<String> myHistory;
    private @Nullable ListPopup myPopup;
    private final @NotNull List<Suggestion> mySuggestions;

    public CommandSuggestionsPopup(@NotNull RawCommandLineEditor commandLineEditor,
                                   @NotNull Collection<String> history,
                                   @NotNull List<Suggestion> suggestions
    ) {
        mySuggestions = suggestions;
        myEditor = commandLineEditor.getEditorField();
        myHistory = history;
    }

    public void install() {
        MyEventListener listener = new MyEventListener();
        myEditor.addFocusListener(listener);
        myEditor.addCaretListener(listener);
        myEditor.getDocument().addDocumentListener(listener);

        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK);
        myEditor.addExtension(ExtendableTextComponent.Extension.create(
                AllIcons.General.InlineVariables,
                AllIcons.General.InlineVariablesHover,
                "Show suggestions... (" + KeymapUtil.getKeystrokeText(keyStroke) + ")",
                this::show));
        DumbAwareAction.create(__ -> show()).registerCustomShortcutSet(new CustomShortcutSet(keyStroke), myEditor);
    }

    public void show() {
        if (myPopup == null) {
            myPopup = new MyListPopup();
            myPopup.showUnderneathOf(myEditor);
        }
    }

    public void hide() {
        if (myPopup != null) {
            myPopup.cancel();
            assert myPopup == null;
        }
    }

    private final class MyEventListener extends DocumentAdapter implements CaretListener, FocusListener {

        @Override
        public void focusGained(FocusEvent e) {
            if (myEditor.getText().isEmpty()) {
                show();
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            hide();
        }

        @Override
        protected void textChanged(@NotNull DocumentEvent e) {
            hide();
        }

        @Override
        public void caretUpdate(CaretEvent e) {
            hide();
        }
    }

    private final class MyListPopup extends ListPopupImpl implements JBPopupListener {
        private MyListPopup() {
            super(null, new MyListPopupStep());
            // Disable focus in popup, so that the text field stays in focus.
            setRequestFocus(false);
            // Prevent the popup from overriding the paste-action.
            // Preventing users from pasting while the popup is open would be annoying,
            // as the popup may open automatically when you focus the text field.
            UiUtils.removeKeyboardAction(getList(), UiUtils.getKeyStrokes(IdeActions.ACTION_PASTE));
            // Register listener, which informs the outer class when the popup is closed
            addListener(this);
        }

        @Override
        protected void process(KeyEvent aEvent) {
            switch (aEvent.getKeyCode()) {
                // Do no handle left and right key,
                // as it would prevent their usage in the text field while the popup is open.
                case KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT -> {
                }
                default -> super.process(aEvent);
            }
        }

        @Override
        public void onClosed(@NotNull LightweightWindowEvent event) {
            myPopup = null;
        }
    }

    public record Suggestion(
            @NotNull Icon icon,
            @NotNull String primaryText,
            @Nullable String secondaryText,
            @NotNull String command
    ) {
        public static @NotNull Suggestion builtin(@NotNull String name, @NotNull String command) {
            return new Suggestion(AllIcons.Actions.Lightning, name, command, command);
        }

        static @NotNull Suggestion history(@NotNull String command) {
            return new Suggestion(AllIcons.Vcs.History, command, null, command);
        }

        @Override
        public String toString() {
            // This method is called by IntelliJ when the user presses Ctrl+C
            return command();
        }
    }

    private final class MyListPopupStep extends BaseListPopupStep<Suggestion> implements ListPopupStepEx<Suggestion> {

        public MyListPopupStep() {
            super(null, Stream.concat(
                    mySuggestions.stream(),
                    myHistory.stream().map(Suggestion::history)
            ).toList());
        }

        @Override
        public Icon getIconFor(Suggestion value) {
            return value.icon();
        }

        @Override
        public @NotNull String getTextFor(Suggestion value) {
            return value.primaryText();
        }

        @Override
        public @Nls @Nullable String getSecondaryTextFor(Suggestion suggestion) {
            return suggestion.secondaryText();
        }

        @Override
        public @NlsContexts.Tooltip @Nullable String getTooltipTextFor(Suggestion value) {
            return null;
        }

        @Override
        public void setEmptyText(@NotNull StatusText emptyText) {
        }

        @Override
        public @Nullable PopupStep<?> onChosen(Suggestion selectedValue, boolean finalChoice) {
            myEditor.setText(selectedValue.command());
            return FINAL_CHOICE;
        }
    }
}
