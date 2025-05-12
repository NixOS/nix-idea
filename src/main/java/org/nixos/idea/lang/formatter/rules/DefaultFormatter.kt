package org.nixos.idea.lang.formatter.rules

import com.intellij.formatting.Indent
import com.intellij.formatting.Wrap
import com.intellij.formatting.WrapType
import org.nixos.idea.lang.formatter.dsl.FormatterDsl
import org.nixos.idea.lang.formatter.dsl.FormatterRule
import org.nixos.idea.psi.NixPsiElement

internal object DefaultFormatter : FormatterRule<NixPsiElement>() {
    override fun FormatterDsl<NixPsiElement>.apply() {
        val commonWrap = Wrap.createWrap(WrapType.NORMAL, false)
        children {
            wrap = commonWrap
            indent = Indent.getNoneIndent()
        }
    }
}
