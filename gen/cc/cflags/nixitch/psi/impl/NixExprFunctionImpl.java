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

public class NixExprFunctionImpl extends ASTWrapperPsiElement implements NixExprFunction {

  public NixExprFunctionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NixVisitor) ((NixVisitor)visitor).visitExprFunction(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public NixExpr getExpr() {
    return findChildByClass(NixExpr.class);
  }

  @Override
  @Nullable
  public NixExprFunction getExprFunction() {
    return findChildByClass(NixExprFunction.class);
  }

  @Override
  @Nullable
  public NixExprIf getExprIf() {
    return findChildByClass(NixExprIf.class);
  }

  @Override
  @Nullable
  public NixLambda getLambda() {
    return findChildByClass(NixLambda.class);
  }

  @Override
  @Nullable
  public NixLetIn getLetIn() {
    return findChildByClass(NixLetIn.class);
  }

  @Override
  @Nullable
  public PsiElement getAssert() {
    return findChildByType(ASSERT);
  }

  @Override
  @Nullable
  public PsiElement getColon() {
    return findChildByType(COLON);
  }

  @Override
  @Nullable
  public PsiElement getId() {
    return findChildByType(ID);
  }

  @Override
  @Nullable
  public PsiElement getSemi() {
    return findChildByType(SEMI);
  }

  @Override
  @Nullable
  public PsiElement getWith() {
    return findChildByType(WITH);
  }

}
