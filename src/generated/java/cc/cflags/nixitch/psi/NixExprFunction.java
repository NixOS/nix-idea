// This is a generated file. Not intended for manual editing.
package cc.cflags.nixitch.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface NixExprFunction extends PsiElement {

  @Nullable
  NixExpr getExpr();

  @Nullable
  NixExprFunction getExprFunction();

  @Nullable
  NixExprIf getExprIf();

  @Nullable
  NixLambda getLambda();

  @Nullable
  NixLetIn getLetIn();

  @Nullable
  PsiElement getAssert();

  @Nullable
  PsiElement getColon();

  @Nullable
  PsiElement getId();

  @Nullable
  PsiElement getSemi();

  @Nullable
  PsiElement getWith();

}
