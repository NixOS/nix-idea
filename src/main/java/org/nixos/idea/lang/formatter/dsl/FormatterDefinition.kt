package org.nixos.idea.lang.formatter.dsl

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.tree.IElementType
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import org.nixos.idea.lang.formatter.dsl.FormatterDsl.ChildAction

internal class FormatterDefinition(rules: Array<FormatterRule<*>>) {

    private val myDefaultRules: List<FormatterRule<*>>
    private val mySpecificRules: Map<IElementType, List<FormatterRule<*>>>

    init {
        val genericRules = mutableListOf<FormatterRule<*>>()
        val specificRules = mutableMapOf<IElementType, MutableList<FormatterRule<*>>>()
        for (rule in rules) {
            if (rule.elementTypes.isEmpty()) {
                genericRules.add(rule)
                specificRules.values.forEach { it.add(rule) }
            } else {
                rule.elementTypes.forEach { elementType ->
                    specificRules.computeIfAbsent(elementType, { genericRules.toMutableList() })
                        .add(rule)
                }
            }
        }
        myDefaultRules = genericRules.toImmutableList()
        mySpecificRules = specificRules.mapValues { it.value.toImmutableList() }.toImmutableMap()
    }

    fun process(
        node: ASTNode,
        settings: CodeStyleSettings,
    ): Result {
        return Processor(
            mySettings = FormatterDsl.Settings(settings),
            myChildActions = listOf(),
            myStates = mapOf(),
        ).process(node)
    }

    inner class Processor(
        private val mySettings: FormatterDsl.Settings,
        private val myChildActions: List<ChildAction<*>>,
        private val myStates: Map<FormatterDsl.State<*>, Any?>,
    ) {
        fun process(node: ASTNode): Result {
            val element = node.psi
            val elementType = node.elementType
            val result = FormatterDsl.Result()
            val context = FormatterDsl.Context(
                node = node,
                element = element,
                elementType = elementType,
                settings = mySettings,
                states = myStates,
            )

            for (rule in mySpecificRules.getOrDefault(elementType, myDefaultRules)) {
                if (!rule.acceptedClass.isInstance(element)) {
                    assert(
                        rule in myDefaultRules,
                        { "Invalid rule ${rule.name}: $elementType not instance of ${rule.acceptedClass}" })
                    continue
                }
                val dsl = FormatterDsl(context, result, rule.name)
                applyRule(rule, dsl)
            }

            for (action in myChildActions) {
                val dsl = FormatterDsl(context, result, action.debugName)
                action.process(dsl)
            }

            return Result(result, Processor(mySettings, result.childActions, result.states))
        }

        @Suppress("UNCHECKED_CAST")
        private fun <T : PsiElement> applyRule(action: FormatterRule<T>, dsl: FormatterDsl<*>) {
            action.run { (dsl as FormatterDsl<T>).apply() }
        }
    }

    class Result(dslResult: FormatterDsl.Result, val childProcessor: Processor) {
        val alignment = dslResult.alignment
        val wrap = dslResult.wrap
        val indent = dslResult.indent
        val debugName = dslResult.debugSources
    }
}
