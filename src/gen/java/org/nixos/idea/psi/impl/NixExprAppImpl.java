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

public class NixExprAppImpl extends NixExprImpl implements NixExprApp {

  public NixExprAppImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull NixVisitor visitor) {
    visitor.visitExprApp(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NixVisitor) accept((NixVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<NixExpr> getExprList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, NixExpr.class);
  }

}
