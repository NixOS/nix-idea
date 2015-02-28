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

public class NixExprOpImpl extends ASTWrapperPsiElement implements NixExprOp {

  public NixExprOpImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NixVisitor) ((NixVisitor)visitor).visitExprOp(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public NixBoolExpr getBoolExpr() {
    return findChildByClass(NixBoolExpr.class);
  }

  @Override
  @Nullable
  public NixLogical getLogical() {
    return findChildByClass(NixLogical.class);
  }

  @Override
  @Nullable
  public NixUnaryOp getUnaryOp() {
    return findChildByClass(NixUnaryOp.class);
  }

}
