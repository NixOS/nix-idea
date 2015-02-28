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

public class NixExprAppImpl extends ASTWrapperPsiElement implements NixExprApp {

  public NixExprAppImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NixVisitor) ((NixVisitor)visitor).visitExprApp(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<NixExprSelect> getExprSelectList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, NixExprSelect.class);
  }

}
