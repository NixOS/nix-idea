package org.nixos.idea.lang.formatter.dsl

import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes

internal abstract class FormatterRule<T : PsiElement>(vararg types: IElementType) {

    val elementTypes = listOf(*types)

    val acceptedClass = this::class.allSupertypes
        .find { it.classifier == FormatterRule::class }!!
        .arguments[0].type!!.classifier as KClass<*>

    val name = this::class.simpleName?.removeSuffix("Formatter") ?: "<Anonymous>"

    abstract fun FormatterDsl<T>.apply()
}
