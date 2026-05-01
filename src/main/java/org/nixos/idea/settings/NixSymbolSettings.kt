package org.nixos.idea.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import org.nixos.idea.settings.SimplePersistentStateComponentHelper.delegate

@State(name = "NixSymbolSettings", storages = [Storage(NixStoragePaths.DEFAULT)])
class NixSymbolSettings : SimplePersistentStateComponent<NixSymbolSettings.State>(State()) {

    class State : BaseState() {
        var enabledPreview by property(false)
        var jumpToFirstDeclaration by property(false)
        var showDeclarationsAsUsages by property(false)
    }

    companion object {
        @JvmStatic
        fun getInstance(): NixSymbolSettings {
            return ApplicationManager.getApplication().getService(NixSymbolSettings::class.java)
        }
    }

    var enabled: Boolean by delegate(State::enabledPreview)
    var jumpToFirstDeclaration by delegate(State::jumpToFirstDeclaration)
    var showDeclarationsAsUsages: Boolean by delegate(State::showDeclarationsAsUsages)

}
