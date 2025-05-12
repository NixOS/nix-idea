package org.nixos.idea.lang.formatter.dsl

import com.intellij.formatting.Alignment
import com.intellij.formatting.Indent
import com.intellij.formatting.Wrap
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import org.nixos.idea.lang.NixLanguage
import org.nixos.idea.settings.NixCodeStyleSettings
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

@DslMarker
private annotation class FormatterDslMarker()

@FormatterDslMarker
internal class FormatterDsl<out T : PsiElement>(
    private val context: Context<T>,
    private val result: Result,
    private val debugName: String,
) {

    private var nextChildBlock = 0

    val node by context::node
    val element by context::element
    val elementType by context::elementType
    val settings by context::settings

    var alignment by result::alignment
    var wrap by result::wrap
    var indent by result::indent

    fun children(
        vararg types: IElementType,
        configure: FormatterDsl<*>.() -> Unit
    ) {
        children(PsiElement::class, *types, configure = configure)
    }

    fun <T : PsiElement> children(
        clazz: KClass<T>,
        vararg types: IElementType,
        configure: FormatterDsl<T>.() -> Unit
    ) {
        result.childActions.add(
            ChildAction(
                debugName = "${debugName}[${nextChildBlock++}]",
                clazz = clazz,
                types = if (types.isEmpty()) TokenSet.ANY else TokenSet.create(*types),
                configure = configure,
            )
        )
    }

    fun <T> setState(state: State<T>, value: T): T {
        assert(!result.states.contains(state))
        result.states.put(state, value)
        return value
    }

    fun <T> getState(state: State<T>): T? {
        assert(!result.states.contains(state))
        @Suppress("UNCHECKED_CAST")
        return context.states[state] as T
    }

    fun <T> preserveState(state: State<T>): T? {
        return getState(state)?.let { setState(state, it) }
    }

    class Context<out T : PsiElement>(
        val node: ASTNode,
        val element: T,
        val elementType: IElementType,
        val settings: Settings,
        val states: Map<State<*>, Any?>,
    )

    class Result {
        private val myDebugSources = linkedMapOf<KProperty<*>, String?>()

        var alignment: Alignment? = null
        var wrap: Wrap? = null
        var indent: Indent? = null

        val childActions = mutableListOf<ChildAction<*>>()
        val states = mutableMapOf<State<*>, Any?>()

        val debugSources get() = myDebugSources.values.distinct().joinToString()

        fun registerSource(property: KProperty<*>, source: String?) {
            myDebugSources.put(property, source)
        }
    }

    class Settings(globalSettings: CodeStyleSettings) {
        val common = globalSettings.getCommonSettings(NixLanguage.INSTANCE)
        val nix = globalSettings.getCustomSettings(NixCodeStyleSettings::class.java)
    }

    data class ChildAction<T : PsiElement>(
        val debugName: String,
        val clazz: KClass<T>,
        val types: TokenSet,
        val configure: FormatterDsl<T>.() -> Unit,
    ) {
        @Suppress("UNCHECKED_CAST") // Checked via isInstance
        fun <U : PsiElement> process(context: FormatterDsl<U>) {
            if (clazz.isInstance(context.element) && types.contains(context.elementType)) {
                (context as FormatterDsl<T>).configure()
            }
        }
    }

    interface State<T> {
    }

    companion object {
        @Suppress("unused") // false positive, used by FormatterDsl::alignment, ...
        private operator fun <V> KMutableProperty0<V>.setValue(
            thisRef: FormatterDsl<*>,
            property: KProperty<*>,
            value: V
        ) {
            thisRef.result.registerSource(property, thisRef.debugName)
            set(value)
        }
    }
}
