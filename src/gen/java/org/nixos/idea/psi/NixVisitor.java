// This is a generated file. Not intended for manual editing.
package org.nixos.idea.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class NixVisitor extends PsiElementVisitor {

  public void visitAntiquotation(@NotNull NixAntiquotation o) {
    visitStringPart(o);
  }

  public void visitAttr(@NotNull NixAttr o) {
    visitPsiElement(o);
  }

  public void visitAttrPath(@NotNull NixAttrPath o) {
    visitPsiElement(o);
  }

  public void visitBind(@NotNull NixBind o) {
    visitPsiElement(o);
  }

  public void visitBindAttr(@NotNull NixBindAttr o) {
    visitBind(o);
  }

  public void visitBindInherit(@NotNull NixBindInherit o) {
    visitBind(o);
  }

  public void visitExpr(@NotNull NixExpr o) {
    visitPsiElement(o);
  }

  public void visitExprApp(@NotNull NixExprApp o) {
    visitExpr(o);
  }

  public void visitExprAssert(@NotNull NixExprAssert o) {
    visitExpr(o);
  }

  public void visitExprIf(@NotNull NixExprIf o) {
    visitExpr(o);
  }

  public void visitExprLambda(@NotNull NixExprLambda o) {
    visitExpr(o);
  }

  public void visitExprLet(@NotNull NixExprLet o) {
    visitExpr(o);
  }

  public void visitExprOp(@NotNull NixExprOp o) {
    visitExpr(o);
  }

  public void visitExprOpAnd(@NotNull NixExprOpAnd o) {
    visitExprOp(o);
  }

  public void visitExprOpBase(@NotNull NixExprOpBase o) {
    visitExprOp(o);
  }

  public void visitExprOpConcat(@NotNull NixExprOpConcat o) {
    visitExprOp(o);
  }

  public void visitExprOpDiv(@NotNull NixExprOpDiv o) {
    visitExprOp(o);
  }

  public void visitExprOpEq(@NotNull NixExprOpEq o) {
    visitExprOp(o);
  }

  public void visitExprOpGe(@NotNull NixExprOpGe o) {
    visitExprOp(o);
  }

  public void visitExprOpGt(@NotNull NixExprOpGt o) {
    visitExprOp(o);
  }

  public void visitExprOpHas(@NotNull NixExprOpHas o) {
    visitExprOp(o);
  }

  public void visitExprOpImplication(@NotNull NixExprOpImplication o) {
    visitExprOp(o);
  }

  public void visitExprOpLe(@NotNull NixExprOpLe o) {
    visitExprOp(o);
  }

  public void visitExprOpLt(@NotNull NixExprOpLt o) {
    visitExprOp(o);
  }

  public void visitExprOpMinus(@NotNull NixExprOpMinus o) {
    visitExprOp(o);
  }

  public void visitExprOpMul(@NotNull NixExprOpMul o) {
    visitExprOp(o);
  }

  public void visitExprOpNe(@NotNull NixExprOpNe o) {
    visitExprOp(o);
  }

  public void visitExprOpNeg(@NotNull NixExprOpNeg o) {
    visitExprOp(o);
  }

  public void visitExprOpNot(@NotNull NixExprOpNot o) {
    visitExprOp(o);
  }

  public void visitExprOpOr(@NotNull NixExprOpOr o) {
    visitExprOp(o);
  }

  public void visitExprOpPlus(@NotNull NixExprOpPlus o) {
    visitExprOp(o);
  }

  public void visitExprOpUpdate(@NotNull NixExprOpUpdate o) {
    visitExprOp(o);
  }

  public void visitExprSelect(@NotNull NixExprSelect o) {
    visitExpr(o);
  }

  public void visitExprSimple(@NotNull NixExprSimple o) {
    visitExpr(o);
  }

  public void visitExprWith(@NotNull NixExprWith o) {
    visitExpr(o);
  }

  public void visitIdentifier(@NotNull NixIdentifier o) {
    visitExprSimple(o);
  }

  public void visitIndString(@NotNull NixIndString o) {
    visitString(o);
  }

  public void visitLegacyAppOr(@NotNull NixLegacyAppOr o) {
    visitExprApp(o);
  }

  public void visitLegacyLet(@NotNull NixLegacyLet o) {
    visitExprSimple(o);
  }

  public void visitList(@NotNull NixList o) {
    visitExprSimple(o);
  }

  public void visitLiteral(@NotNull NixLiteral o) {
    visitExprSimple(o);
  }

  public void visitParam(@NotNull NixParam o) {
    visitPsiElement(o);
  }

  public void visitParamSet(@NotNull NixParamSet o) {
    visitPsiElement(o);
  }

  public void visitParens(@NotNull NixParens o) {
    visitExprSimple(o);
  }

  public void visitPath(@NotNull NixPath o) {
    visitLiteral(o);
  }

  public void visitSet(@NotNull NixSet o) {
    visitExprSimple(o);
  }

  public void visitStdAttr(@NotNull NixStdAttr o) {
    visitAttr(o);
  }

  public void visitStdString(@NotNull NixStdString o) {
    visitString(o);
  }

  public void visitString(@NotNull NixString o) {
    visitExprSimple(o);
  }

  public void visitStringAttr(@NotNull NixStringAttr o) {
    visitAttr(o);
  }

  public void visitStringPart(@NotNull NixStringPart o) {
    visitPsiElement(o);
  }

  public void visitStringText(@NotNull NixStringText o) {
    visitStringPart(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
