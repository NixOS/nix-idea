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

public class NixLetInImpl extends ASTWrapperPsiElement implements NixLetIn {

  public NixLetInImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NixVisitor) ((NixVisitor)visitor).visitLetIn(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public NixPureBind getPureBind() {
    return findNotNullChildByClass(NixPureBind.class);
  }

  @Override
  @NotNull
  public PsiElement getIn() {
    return findNotNullChildByType(IN);
  }

  @Override
  @NotNull
  public PsiElement getLet() {
    return findNotNullChildByType(LET);
  }

}
