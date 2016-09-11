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

public class NixListExprImpl extends ASTWrapperPsiElement implements NixListExpr {

  public NixListExprImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull NixVisitor visitor) {
    visitor.visitListExpr(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NixVisitor) accept((NixVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public NixAttrPath getAttrPath() {
    return findChildByClass(NixAttrPath.class);
  }

  @Override
  @Nullable
  public NixBindOrSelect getBindOrSelect() {
    return findChildByClass(NixBindOrSelect.class);
  }

  @Override
  @Nullable
  public NixEvalOrSelect getEvalOrSelect() {
    return findChildByClass(NixEvalOrSelect.class);
  }

  @Override
  @Nullable
  public NixStringParts getStringParts() {
    return findChildByClass(NixStringParts.class);
  }

}
