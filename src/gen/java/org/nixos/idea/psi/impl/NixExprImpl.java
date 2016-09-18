// This is a generated file. Not intended for manual editing.
package org.nixos.idea.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.nixos.idea.psi.NixTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import org.nixos.idea.psi.*;

public class NixExprImpl extends ASTWrapperPsiElement implements NixExpr {

  public NixExprImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull NixVisitor visitor) {
    visitor.visitExpr(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NixVisitor) accept((NixVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public NixBinds getBinds() {
    return findChildByClass(NixBinds.class);
  }

  @Override
  @NotNull
  public List<NixExpr> getExprList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, NixExpr.class);
  }

  @Override
  @Nullable
  public NixExprOp getExprOp() {
    return findChildByClass(NixExprOp.class);
  }

  @Override
  @Nullable
  public NixLambda getLambda() {
    return findChildByClass(NixLambda.class);
  }

  @Override
  @Nullable
  public PsiElement getAssert() {
    return findChildByType(ASSERT);
  }

  @Override
  @Nullable
  public PsiElement getElse() {
    return findChildByType(ELSE);
  }

  @Override
  @Nullable
  public PsiElement getIf() {
    return findChildByType(IF);
  }

  @Override
  @Nullable
  public PsiElement getIn() {
    return findChildByType(IN);
  }

  @Override
  @Nullable
  public PsiElement getLet() {
    return findChildByType(LET);
  }

  @Override
  @Nullable
  public PsiElement getSemi() {
    return findChildByType(SEMI);
  }

  @Override
  @Nullable
  public PsiElement getThen() {
    return findChildByType(THEN);
  }

  @Override
  @Nullable
  public PsiElement getWith() {
    return findChildByType(WITH);
  }

}
