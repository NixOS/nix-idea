package org.nixos.idea.lang

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.containers.toArray
import org.nixos.idea.psi.NixBindAttr
import org.nixos.idea.psi.NixElementType
import org.nixos.idea.psi.NixElementVisitor
import org.nixos.idea.psi.NixExprApp
import org.nixos.idea.psi.NixExprAssert
import org.nixos.idea.psi.NixExprAttrs
import org.nixos.idea.psi.NixExprIf
import org.nixos.idea.psi.NixExprLambda
import org.nixos.idea.psi.NixExprLet
import org.nixos.idea.psi.NixExprList
import org.nixos.idea.psi.NixExprOpConcat
import org.nixos.idea.psi.NixExprOpUpdate
import org.nixos.idea.psi.NixExprParens
import org.nixos.idea.psi.NixExprSelect
import org.nixos.idea.psi.NixExprWith
import org.nixos.idea.psi.NixFormal
import org.nixos.idea.psi.NixFormals
import org.nixos.idea.psi.NixIndString
import org.nixos.idea.psi.NixTypes

class NixFoldingBuilder : FoldingBuilderEx() {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()

        root.accept(object : NixElementVisitor<Unit>() {
            override fun visitExprAttrs(o: NixExprAttrs) {
                o.acceptChildren(this)
                descriptors.add(FoldingDescriptor(o, o.textRange))
                o.bindList.forEach { it.accept(this) }
            }

            override fun visitBindAttr(o: NixBindAttr) {
                o.expr?.accept(this)
            }

            override fun visitExprParens(o: NixExprParens) {
                descriptors.add(FoldingDescriptor(o, o.textRange))
                o.expr?.accept(this)
            }

            override fun visitExprWith(o: NixExprWith) {
                o.exprList.forEach { it.accept(this) }
            }

            override fun visitExprApp(o: NixExprApp) {
                o.exprList.forEach { it.accept(this) }
            }

            override fun visitExprIf(o: NixExprIf) {
                o.acceptChildren(this)
            }

            override fun visitExprAssert(o: NixExprAssert) {
                o.exprList.forEach { it.accept(this) }
            }

            override fun visitIndString(o: NixIndString) {
                descriptors.add(FoldingDescriptor(o, o.textRange))
            }

            override fun visitExprOpUpdate(o: NixExprOpUpdate) {
                o.acceptChildren(this)
            }

            override fun visitExprOpConcat(o: NixExprOpConcat) {
                o.acceptChildren(this)
            }

            override fun visitExprList(o: NixExprList) {
                descriptors.add(FoldingDescriptor(o, o.textRange))
                o.items.forEach { it.accept(this) }
            }

            override fun visitExprSelect(o: NixExprSelect) {
                o.acceptChildren(this)
            }

            override fun visitExprLet(o: NixExprLet) {
                descriptors.add(FoldingDescriptor(o, o.textRange))
                o.bindList.forEach { it.accept(this) }
                o.expr?.accept(this)
            }

            override fun visitFormals(o: NixFormals) {
                descriptors.add(FoldingDescriptor(o, o.textRange))
                o.formalList.forEach { it.accept(this) }
            }

            override fun visitComment(comment: PsiComment) {
                descriptors.add(FoldingDescriptor(comment, comment.textRange))
            }

            override fun visitFormal(o: NixFormal) {
                o.expr?.accept(this)
            }

            override fun visitExprLambda(o: NixExprLambda) {
                o.expr?.accept(this)
                o.formals?.accept(this)
            }

            override fun visitFile(file: PsiFile) {
                super.visitFile(file)
                file.acceptChildren(this)
            }
        })

        return descriptors.toArray(FoldingDescriptor.EMPTY_ARRAY)
    }

    override fun getPlaceholderText(node: ASTNode): String? =
        when (node.elementType) {
            NixTypes.EXPR_PARENS -> "(...)";
            NixTypes.MCOMMENT -> "/* ... */";
            NixTypes.EXPR_ATTRS -> "{ ... }";
            NixTypes.EXPR_LIST -> "[ ... ]";
            NixTypes.IND_STRING -> "'' ... ''";
            NixTypes.FORMALS -> "{ ... }";
            else -> null
        }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return false
    }
}
