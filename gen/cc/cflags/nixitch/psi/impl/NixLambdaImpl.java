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

public class NixLambdaImpl extends ASTWrapperPsiElement implements NixLambda {

  public NixLambdaImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NixVisitor) ((NixVisitor)visitor).visitLambda(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public NixBnLambda getBnLambda() {
    return findChildByClass(NixBnLambda.class);
  }

  @Override
  @Nullable
  public NixFnLambda getFnLambda() {
    return findChildByClass(NixFnLambda.class);
  }

  @Override
  @Nullable
  public NixFormalSet getFormalSet() {
    return findChildByClass(NixFormalSet.class);
  }

}
