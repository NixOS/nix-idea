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

public class NixMulExprImpl extends ASTWrapperPsiElement implements NixMulExpr {

  public NixMulExprImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NixVisitor) ((NixVisitor)visitor).visitMulExpr(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public NixMulExpr getMulExpr() {
    return findChildByClass(NixMulExpr.class);
  }

  @Override
  @NotNull
  public List<NixPrimary> getPrimaryList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, NixPrimary.class);
  }

  @Override
  @Nullable
  public PsiElement getDivide() {
    return findChildByType(DIVIDE);
  }

  @Override
  @Nullable
  public PsiElement getImpl() {
    return findChildByType(IMPL);
  }

  @Override
  @Nullable
  public PsiElement getTimes() {
    return findChildByType(TIMES);
  }

}
