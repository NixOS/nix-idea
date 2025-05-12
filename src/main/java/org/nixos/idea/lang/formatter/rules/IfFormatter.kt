package org.nixos.idea.lang.formatter.rules

import com.intellij.formatting.Indent
import com.intellij.formatting.Wrap
import com.intellij.formatting.WrapType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtilBase
import org.nixos.idea.lang.formatter.dsl.FormatterDsl
import org.nixos.idea.lang.formatter.dsl.FormatterRule
import org.nixos.idea.psi.NixExprIf
import org.nixos.idea.psi.NixTokenType
import org.nixos.idea.psi.NixTypes

internal object IfFormatter : FormatterRule<NixExprIf>(NixTypes.EXPR_IF) {
    override fun FormatterDsl<NixExprIf>.apply() {
        val caseWrap = Wrap.createWrap(WrapType.CHOP_DOWN_IF_LONG, true)
        val conditionWrap = Wrap.createChildWrap(caseWrap, WrapType.CHOP_DOWN_IF_LONG, true)
        children {
            val elementType = node.getElementType()
            if (elementType is NixTokenType) {
                wrap = null
                indent = Indent.getNoneIndent()
            } else {
                indent = Indent.getNormalIndent()
                val prev = PsiTreeUtil.skipWhitespacesAndCommentsBackward(node.getPsi())
                val prevType = PsiUtilBase.getElementType(prev)
                if (prevType === NixTypes.ELSE && elementType === NixTypes.EXPR_IF) {
                    // TODO Does this work?
                    wrap = conditionWrap
                } else if (prevType === NixTypes.IF) {
                    wrap = conditionWrap
                } else {
                    wrap = caseWrap
                }
            }
        }
    }
}
