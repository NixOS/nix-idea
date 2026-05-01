package org.nixos.idea.settings

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.util.text.Strings
import java.util.Deque
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

internal object SimplePersistentStateComponentHelper {

    private const val MAX_HISTORY_SIZE = 5

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
    fun <S : BaseState, V> delegate(prop: KMutableProperty1<S, V>) =
        object : ReadWriteProperty<SimplePersistentStateComponent<S>, V> {
            override fun getValue(thisRef: SimplePersistentStateComponent<S>, property: KProperty<*>): V {
                return prop.get(thisRef.state)
            }

            override fun setValue(thisRef: SimplePersistentStateComponent<S>, property: KProperty<*>, value: V) {
                prop.set(thisRef.state, value)
            }
        }

    /**
     * Creates string property with history which delegates every access to the given properties of the state.
     *
     * ```kotlin
     * @State(name = "SomeSettings", storages = [Storage(...)])
     * class SomeSettings : SimplePersistentStateComponent<SomeSettings.State>(State()) {
     *     class State : BaseState() {
     *         // The internal storage of the configured values
     *         var command by string()
     *         var history: Deque<String> by property(ArrayDeque(), { it.isEmpty() })
     *     }
     *     // Makes the property publicly accessible
     *     var command: String by delegate(State::command, State::history)
     *     val commandHistory: Collection<String>
     *         get() = Collections.unmodifiableCollection(state.history)
     * }
     */
    fun <S : BaseState> delegate(prop: KMutableProperty1<S, String?>, historyProp: KProperty1<S, Deque<String>>) =
        object : ReadWriteProperty<SimplePersistentStateComponent<S>, String> {
            override fun getValue(thisRef: SimplePersistentStateComponent<S>, property: KProperty<*>): String {
                return Strings.notNullize(prop.get(thisRef.state))
            }

            override fun setValue(thisRef: SimplePersistentStateComponent<S>, property: KProperty<*>, value: String) {
                val normalized = Strings.nullize(value, true)
                prop.set(thisRef.state, normalized)
                if (normalized != null) {
                    val history = historyProp.get(thisRef.state)
                    history.remove(normalized)
                    history.addFirst(normalized)
                    while (history.size > MAX_HISTORY_SIZE) {
                        history.removeLast()
                    }
                }
            }
        }
}
