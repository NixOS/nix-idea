// This is a generated file. Not intended for manual editing.
package org.nixos.idea.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.nixos.idea.psi.NixTypes.*;
import org.nixos.idea.psi.*;

public class NixExprOpOrImpl extends NixExprOpImpl implements NixExprOpOr {

  public NixExprOpOrImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull NixVisitor visitor) {
    visitor.visitExprOpOr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NixVisitor) accept((NixVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<NixExprOp> getExprOpList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, NixExprOp.class);
  }

}
