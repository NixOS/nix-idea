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

public class NixBindInheritImpl extends NixBindImpl implements NixBindInherit {

  public NixBindInheritImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull NixVisitor visitor) {
    visitor.visitBindInherit(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NixVisitor) accept((NixVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<NixAttr> getAttrList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, NixAttr.class);
  }

  @Override
  @Nullable
  public NixExpr getExpr() {
    return findChildByClass(NixExpr.class);
  }

}
