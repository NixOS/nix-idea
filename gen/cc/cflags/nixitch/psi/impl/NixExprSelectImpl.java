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

public class NixExprSelectImpl extends ASTWrapperPsiElement implements NixExprSelect {

  public NixExprSelectImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NixVisitor) ((NixVisitor)visitor).visitExprSelect(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public NixAttrPath getAttrPath() {
    return findChildByClass(NixAttrPath.class);
  }

  @Override
  @Nullable
  public NixExprSelect getExprSelect() {
    return findChildByClass(NixExprSelect.class);
  }

  @Override
  @NotNull
  public NixExprSimple getExprSimple() {
    return findNotNullChildByClass(NixExprSimple.class);
  }

  @Override
  @Nullable
  public PsiElement getDot() {
    return findChildByType(DOT);
  }

  @Override
  @Nullable
  public PsiElement getOrKw() {
    return findChildByType(OR_KW);
  }

  @Override
  @Nullable
  public PsiElement getOwKw() {
    return findChildByType(OW_KW);
  }

}
