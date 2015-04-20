// This is a generated file. Not intended for manual editing.
package cc.cflags.nixitch.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface NixExprIf extends PsiElement {

  @NotNull
  List<NixExpr> getExprList();

  @Nullable
  NixExprOp getExprOp();

  @Nullable
  PsiElement getElse();

  @Nullable
  PsiElement getIf();

  @Nullable
  PsiElement getThen();

}
