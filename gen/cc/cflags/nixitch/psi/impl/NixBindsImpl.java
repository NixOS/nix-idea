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

public class NixBindsImpl extends ASTWrapperPsiElement implements NixBinds {

  public NixBindsImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NixVisitor) ((NixVisitor)visitor).visitBinds(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<NixAttrAssign> getAttrAssignList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, NixAttrAssign.class);
  }

  @Override
  @NotNull
  public List<NixInheritAttrs> getInheritAttrsList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, NixInheritAttrs.class);
  }

  @Override
  @NotNull
  public List<NixRequireExpr> getRequireExprList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, NixRequireExpr.class);
  }

}
