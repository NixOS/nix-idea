package org.nixos.idea.lsp

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.util.text.Strings
import org.nixos.idea.settings.NixStoragePaths
import org.nixos.idea.settings.SimplePersistentStateComponentHelper.delegate
import java.util.ArrayDeque
import java.util.Collections
import java.util.Deque

@State(name = "NixLspSettings", storages = [Storage(value = NixStoragePaths.TOOLS, roamingType = RoamingType.LOCAL)])
class NixLspSettings : SimplePersistentStateComponent<NixLspSettings.State>(State()) {

    class State : BaseState() {
        var enabled by property(false)
        var command by string()
        var history: Deque<String> by property(ArrayDeque(), { it.isEmpty() })
    }

    var isEnabled: Boolean by delegate(State::enabled)
    var command: String
        get() = Strings.notNullize(state.command)
        set(command) {
            state.command = command
            addToHistory(command)
        }
    val commandHistory: Collection<String>
        get() = Collections.unmodifiableCollection(state.history)

    private fun addToHistory(command: String) {
        val history = state.history
        history.remove(command)
        history.addFirst(command)
        while (history.size > MAX_HISTORY_SIZE) {
            history.removeLast()
        }
    }

    companion object {
        private const val MAX_HISTORY_SIZE = 5

        @JvmStatic
        fun getInstance(): NixLspSettings {
            return ApplicationManager.getApplication().getService(NixLspSettings::class.java)
        }
    }
}
