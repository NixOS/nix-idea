package org.nixos.idea.settings.ui

import com.intellij.openapi.ui.validation.DialogValidationRequestor
import com.intellij.openapi.ui.validation.WHEN_TEXT_CHANGED
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsContexts.DialogMessage
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.TextAccessor
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.MutableProperty
import com.intellij.ui.dsl.builder.toMutableProperty
import org.nixos.idea.settings.ui.CommandSuggestionsPopup.Suggestion
import javax.swing.JComponent
import kotlin.reflect.KMutableProperty0

object UiDslExtensions {
    fun <T : JComponent> Cell<T>.validateOnReset(): Cell<T> {
        return validationRequestor { onReset { it() } }
    }

    fun <T : JComponent> Cell<T>.warnOnInput(@DialogMessage message: String, condition: (T) -> Boolean): Cell<T> {
        return validationOnInput { if (condition(it)) warning(message) else null }
    }

    fun <T : RawCommandLineEditor> Cell<T>.bindText(prop: MutableProperty<String>): Cell<T> {
        return bind(TextAccessor::getText, TextAccessor::setText, prop)
    }

    fun <T : RawCommandLineEditor> Cell<T>.bindText(prop: KMutableProperty0<String>): Cell<T> {
        return bindText(prop.toMutableProperty())
    }

    fun <T : RawCommandLineEditor> Cell<T>.validateWhenTextChanged(): Cell<T> {
        return validationRequestor(DialogValidationRequestor.WithParameter {
            WHEN_TEXT_CHANGED(it.textField)
        })
    }

    fun <T : RawCommandLineEditor> Cell<T>.placeholderText(@NlsContexts.StatusText text: String): Cell<T> {
        component.editorField.emptyText.setText(text)
        component.editorField.accessibleContext.accessibleName = text
        return this
    }

    fun <T : RawCommandLineEditor> Cell<T>.suggestionsPopup(
        history: Collection<String>,
        suggestions: List<Suggestion>
    ): Cell<T> {
        CommandSuggestionsPopup(
            component,
            history,
            suggestions
        ).install()
        return this
    }
}
