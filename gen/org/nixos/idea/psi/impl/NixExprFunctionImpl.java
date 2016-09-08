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

public class NixExprFunctionImpl extends ASTWrapperPsiElement implements NixExprFunction {

  public NixExprFunctionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull NixVisitor visitor) {
    visitor.visitExprFunction(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NixVisitor) accept((NixVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public NixExpr getExpr() {
    return findChildByClass(NixExpr.class);
  }

  @Override
  @Nullable
  public NixExprFunction getExprFunction() {
    return findChildByClass(NixExprFunction.class);
  }

  @Override
  @Nullable
  public NixExprIf getExprIf() {
    return findChildByClass(NixExprIf.class);
  }

  @Override
  @Nullable
  public NixLambda getLambda() {
    return findChildByClass(NixLambda.class);
  }

  @Override
  @Nullable
  public NixLetIn getLetIn() {
    return findChildByClass(NixLetIn.class);
  }

  @Override
  @Nullable
  public PsiElement getAssert() {
    return findChildByType(ASSERT);
  }

  @Override
  @Nullable
  public PsiElement getColon() {
    return findChildByType(COLON);
  }

  @Override
  @Nullable
  public PsiElement getId() {
    return findChildByType(ID);
  }

  @Override
  @Nullable
  public PsiElement getSemi() {
    return findChildByType(SEMI);
  }

  @Override
  @Nullable
  public PsiElement getWith() {
    return findChildByType(WITH);
  }

}
