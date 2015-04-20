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

public class NixPathsExprImpl extends ASTWrapperPsiElement implements NixPathsExpr {

  public NixPathsExprImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NixVisitor) ((NixVisitor)visitor).visitPathsExpr(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<NixPathStmt> getPathStmtList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, NixPathStmt.class);
  }

  @Override
  @NotNull
  public PsiElement getLbrac() {
    return findNotNullChildByType(LBRAC);
  }

  @Override
  @NotNull
  public PsiElement getRbrac() {
    return findNotNullChildByType(RBRAC);
  }

}
