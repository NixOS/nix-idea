package org.nixos.idea.settings

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.SimplePersistentStateComponent
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

internal object SimplePersistentStateComponentHelper {

    /**
     * Creates property which delegates every access to the given property of the state.
     *
     * ```kotlin
     * @State(name = "SomeSettings", storages = [Storage(...)])
     * class SomeSettings : SimplePersistentStateComponent<SomeSettings.State>(State()) {
     *     class State : BaseState() {
     *         // The internal storage of the configured values
     *         var enabled by property(true)
     *     }
     *
     *     // Makes the property publicly accessible
     *     var enabled: Boolean by delegate(State::enabled)
     * }
     * ```
     */
    fun <S : BaseState, V> delegate(prop: KMutableProperty1<S, V>): ReadWriteProperty<SimplePersistentStateComponent<S>, V> {
        return object : ReadWriteProperty<SimplePersistentStateComponent<S>, V> {
            override fun getValue(thisRef: SimplePersistentStateComponent<S>, property: KProperty<*>): V {
                return prop.get(thisRef.state)
            }

            override fun setValue(thisRef: SimplePersistentStateComponent<S>, property: KProperty<*>, value: V) {
                prop.set(thisRef.state, value)
            }
        }
    }
}
