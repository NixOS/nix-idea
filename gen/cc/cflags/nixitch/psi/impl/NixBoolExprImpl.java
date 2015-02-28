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

public class NixBoolExprImpl extends ASTWrapperPsiElement implements NixBoolExpr {

  public NixBoolExprImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NixVisitor) ((NixVisitor)visitor).visitBoolExpr(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public NixBoolExpr getBoolExpr() {
    return findChildByClass(NixBoolExpr.class);
  }

  @Override
  @NotNull
  public List<NixLogical> getLogicalList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, NixLogical.class);
  }

  @Override
  @Nullable
  public PsiElement getAnd() {
    return findChildByType(AND);
  }

  @Override
  @Nullable
  public PsiElement getOr() {
    return findChildByType(OR);
  }

}
