package org.nixos.idea.lang.formatter.rules

import com.intellij.formatting.Indent
import com.intellij.formatting.Wrap
import com.intellij.formatting.WrapType
import org.nixos.idea.lang.formatter.dsl.FormatterDsl
import org.nixos.idea.lang.formatter.dsl.FormatterRule
import org.nixos.idea.psi.NixPsiElement
import org.nixos.idea.psi.NixTokenType
import org.nixos.idea.psi.NixTypes

internal object CollectionFormatter : FormatterRule<NixPsiElement>(NixTypes.EXPR_ATTRS, NixTypes.EXPR_LIST, NixTypes.FORMALS) {
    override fun FormatterDsl<NixPsiElement>.apply() {
        val itemWrap = Wrap.createWrap(WrapType.CHOP_DOWN_IF_LONG, true)
        children {
            val elementType = node.getElementType()
            if (elementType is NixTokenType) {
                wrap = null
                indent = Indent.getNoneIndent()
            } else {
                wrap = itemWrap
                indent = Indent.getNormalIndent()
            }
        }
    }
}
