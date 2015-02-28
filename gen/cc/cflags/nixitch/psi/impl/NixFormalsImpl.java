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

public class NixFormalsImpl extends ASTWrapperPsiElement implements NixFormals {

  public NixFormalsImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NixVisitor) ((NixVisitor)visitor).visitFormals(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<NixFormal> getFormalList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, NixFormal.class);
  }

}
