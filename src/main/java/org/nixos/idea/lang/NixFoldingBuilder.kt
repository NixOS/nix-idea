package org.nixos.idea.lang

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.util.containers.toArray
import org.nixos.idea.psi.NixBindInherit
import org.nixos.idea.psi.NixElementVisitor
import org.nixos.idea.psi.NixExprAttrs
import org.nixos.idea.psi.NixExprLet
import org.nixos.idea.psi.NixExprList
import org.nixos.idea.psi.NixExprParens
import org.nixos.idea.psi.NixFormals
import org.nixos.idea.psi.NixString
import org.nixos.idea.psi.NixTypes

class NixFoldingBuilder : FoldingBuilderEx() {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()

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
                super.visitComment(o)
            }

        })

        return descriptors.toArray(FoldingDescriptor.EMPTY_ARRAY)
    }

    override fun getPlaceholderText(node: ASTNode): String? {
        return null
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return false
    }
}
