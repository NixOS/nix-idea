package org.nixos.idea.lang

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.CustomFoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import org.nixos.idea.psi.NixAntiquotation
import org.nixos.idea.psi.NixBindInherit
import org.nixos.idea.psi.NixElementVisitor
import org.nixos.idea.psi.NixExprAttrs
import org.nixos.idea.psi.NixExprLet
import org.nixos.idea.psi.NixExprList
import org.nixos.idea.psi.NixExprParens
import org.nixos.idea.psi.NixFormals
import org.nixos.idea.psi.NixString
import org.nixos.idea.psi.NixTypes

class NixFoldingBuilder : CustomFoldingBuilder() {
    override fun buildLanguageFoldRegions(
        descriptors: MutableList<FoldingDescriptor>,
        root: PsiElement,
        document: Document,
        quick: Boolean
    ) {
        root.accept(object : NixElementVisitor<Unit>() {
            override fun visitElement(o: PsiElement) {
                super.visitElement(o)
                return o.acceptChildren(this)
            }

            override fun visitExprAttrs(o: NixExprAttrs) {
                descriptors.add(FoldingDescriptor(o.node, o.textRange, null, "{ ... }"))
                super.visitExprAttrs(o)
            }

            override fun visitExprParens(o: NixExprParens) {
                descriptors.add(FoldingDescriptor(o.node, o.textRange, null, "( ... )"))
                super.visitExprParens(o)
            }

            override fun visitString(o: NixString) {
                descriptors.add(FoldingDescriptor(o.node, o.textRange, null, "\" ... \""))
                super.visitString(o)
            }

            override fun visitAntiquotation(o: NixAntiquotation) {
                descriptors.add(FoldingDescriptor(o.node, o.textRange, null, "\${ ... }"))
                return super.visitAntiquotation(o)
            }

            override fun visitExprList(o: NixExprList) {
                descriptors.add(FoldingDescriptor(o.node, o.textRange, null, "[ ... ]"))
                super.visitExprList(o)
            }

            override fun visitExprLet(o: NixExprLet) {
                if (o.bindList.isNotEmpty()) {
                    val last = o.bindList.last()
                    descriptors.add(
                        FoldingDescriptor(
                            o.node,
                            TextRange(
                                o.node.firstChildNode.textRange.endOffset, /* Start folding after the keyword */
                                last.textRange.endOffset /* Stop folding before the in */
                            ),
                            null,
                            " ... ",
                        )
                    )
                }
                super.visitExprLet(o)
            }

            override fun visitBindInherit(o: NixBindInherit) {
                if (o.attributes.isNotEmpty()) {
                    val placeholderText = if (o.source == null) {" ... "} else {" (${o.source!!.node.text}) ... "}
                    descriptors.add(
                        FoldingDescriptor(
                            o.node,
                            TextRange(
                                o.node.firstChildNode.textRange.endOffset,  /* Start folding after the keyword */
                                o.textRange.endOffset - 1 /* don't fold the semicolon */
                            ),
                            null,
                            placeholderText
                        )
                    )
                }
                super.visitBindInherit(o)
            }

            override fun visitFormals(o: NixFormals) {
                descriptors.add(FoldingDescriptor(o.node, o.textRange, null, "{ ... }"))
                super.visitFormals(o)
            }

            override fun visitComment(o: PsiComment) {
                if (o.tokenType == NixTypes.MCOMMENT) {
                    descriptors.add(FoldingDescriptor(o.node, o.textRange, null, "/* ... */"))
                }

                if (o.tokenType == NixTypes.SCOMMENT && findNextAdjacentSComment( o, { it.prevSibling }) == null ) {
                    val end = findLastSComment(o, { it.nextSibling })
                    if (end != o) {
                        descriptors.add(
                            FoldingDescriptor(
                                o.node,
                                TextRange(o.textRange.startOffset, end.textRange.endOffset),
                                null,
                                "# ...",
                            )
                        )
                    }
                }

                super.visitComment(o)
            }

            private fun findNextAdjacentSComment(o: PsiComment, nextFn: (PsiElement) -> PsiElement?): PsiComment? {
                val ws = nextFn(o) ?: return null
                if (ws is PsiWhiteSpace && ws.text.count { it == '\n' } > 1) {
                    return null
                }

                val next = nextFn(ws)
                return if (next is PsiComment && next.tokenType == NixTypes.SCOMMENT) {
                    next
                } else {
                    null
                }
            }

            private fun findLastSComment(o: PsiComment, nextFn: (PsiElement) -> PsiElement?): PsiComment {
                var result = o;
                while (true) {
                    result = findNextAdjacentSComment(result, nextFn) ?: return result
                }
            }

        })
    }

    override fun getLanguagePlaceholderText(node: ASTNode, textRange: TextRange): String? {
        return null
    }

    override fun isRegionCollapsedByDefault(node: ASTNode): Boolean {
        return false
    }
}
