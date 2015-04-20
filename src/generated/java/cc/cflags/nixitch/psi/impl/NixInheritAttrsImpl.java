// This is a generated file. Not intended for manual editing.
package cc.cflags.nixitch.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static cc.cflags.nixitch.psi.NixTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import cc.cflags.nixitch.psi.*;

public class NixInheritAttrsImpl extends ASTWrapperPsiElement implements NixInheritAttrs {

  public NixInheritAttrsImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NixVisitor) ((NixVisitor)visitor).visitInheritAttrs(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public NixAttrs getAttrs() {
    return findNotNullChildByClass(NixAttrs.class);
  }

  @Override
  @Nullable
  public NixExpr getExpr() {
    return findChildByClass(NixExpr.class);
  }

  @Override
  @NotNull
  public PsiElement getInherit() {
    return findNotNullChildByType(INHERIT);
  }

  @Override
  @Nullable
  public PsiElement getLparen() {
    return findChildByType(LPAREN);
  }

  @Override
  @Nullable
  public PsiElement getRparen() {
    return findChildByType(RPAREN);
  }

}
