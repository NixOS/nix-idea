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

public class NixAddExprImpl extends ASTWrapperPsiElement implements NixAddExpr {

  public NixAddExprImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull NixVisitor visitor) {
    visitor.visitAddExpr(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NixVisitor) accept((NixVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public NixAddExpr getAddExpr() {
    return findChildByClass(NixAddExpr.class);
  }

  @Override
  @NotNull
  public List<NixFactor> getFactorList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, NixFactor.class);
  }

  @Override
  @Nullable
  public PsiElement getConcat() {
    return findChildByType(CONCAT);
  }

  @Override
  @Nullable
  public PsiElement getMinus() {
    return findChildByType(MINUS);
  }

  @Override
  @Nullable
  public PsiElement getPlus() {
    return findChildByType(PLUS);
  }

  @Override
  @Nullable
  public PsiElement getUpdate() {
    return findChildByType(UPDATE);
  }

}
