package org.nixos.idea.lang.formatter.rules

import com.intellij.formatting.Alignment
import com.intellij.psi.util.PsiTreeUtil
import org.nixos.idea.lang.formatter.dsl.FormatterDsl
import org.nixos.idea.lang.formatter.dsl.FormatterRule
import org.nixos.idea.psi.NixBindAttr
import org.nixos.idea.psi.NixExprAttrs
import org.nixos.idea.psi.NixPsiElement
import org.nixos.idea.psi.NixTypes
import org.nixos.idea.settings.NixCodeStyleSettings.AttributeAlignment

internal object AttributeAlignmentFormatter : FormatterRule<NixExprAttrs>(NixTypes.EXPR_ATTRS) {
    object STATE_ALIGNMENT : FormatterDsl.State<Alignment>

    override fun FormatterDsl<NixExprAttrs>.apply() {
        val strategy = Strategy.of(settings.nix.ALIGN_ATTRIBUTES)
        var attrAlignment = strategy.createAlignment(getState(STATE_ALIGNMENT))
        if (attrAlignment != null) {
            setState(STATE_ALIGNMENT, attrAlignment)
            var lastAttributeBinding: NixBindAttr? = null
            children(NixBindAttr::class, NixTypes.BIND_ATTR) {
                val currentBinding = element
                preserveState(STATE_ALIGNMENT)
                children(NixTypes.ASSIGN) {
                    attrAlignment = strategy.updateAlignment(
                        attrAlignment!!,
                        lastAttributeBinding,
                        currentBinding
                    )
                    lastAttributeBinding = currentBinding
                    alignment = attrAlignment
                }
            }
        }
    }

    private enum class Strategy(@field:AttributeAlignment @param:AttributeAlignment private val myId: Int) {
        DO_NOT_ALIGN(AttributeAlignment.DO_NOT_ALIGN),
        ALIGN_CONSECUTIVE(AttributeAlignment.ALIGN_CONSECUTIVE),
        ALIGN_SIBLINGS(AttributeAlignment.ALIGN_SIBLINGS),
        ALIGN_NESTED(AttributeAlignment.ALIGN_NESTED),
        ;

        fun createAlignment(parent: Alignment?): Alignment? {
            when (this) {
                DO_NOT_ALIGN -> return null
                ALIGN_NESTED -> {
                    if (parent != null) {
                        return parent
                    }
                    return Alignment.createAlignment(true)
                }

                else -> return Alignment.createAlignment(true)
            }
        }

        fun updateAlignment(
            previousAlignment: Alignment,
            previousBinding: NixBindAttr?,
            currentBinding: NixBindAttr
        ): Alignment {
            when (this) {
                ALIGN_CONSECUTIVE -> {
                    if (previousBinding != null && isSeparationBetween(previousBinding, currentBinding)) {
                        return Alignment.createAlignment(true)
                    }
                    return previousAlignment
                }

                else -> return previousAlignment
            }
        }

        companion object {
            fun of(@AttributeAlignment id: Int): Strategy {
                for (strategy in Strategy.entries) {
                    if (strategy.myId == id) {
                        return strategy
                    }
                }
                throw NoSuchElementException("Unknown alignment strategy: " + id)
            }

            private fun isSeparationBetween(before: NixPsiElement, after: NixPsiElement): Boolean {
                if (PsiTreeUtil.skipWhitespacesAndCommentsForward(before) === after) {
                    var lineFeedCount = 0
                    var c = before.getNextSibling()
                    while (c !== after) {
                        if (c.textContains('\n') && ++lineFeedCount > 1) {
                            return true
                        }
                        c = c.getNextSibling()
                    }
                }
                return false
            }
        }
    }
}
