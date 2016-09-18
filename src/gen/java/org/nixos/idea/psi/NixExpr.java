// This is a generated file. Not intended for manual editing.
package org.nixos.idea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface NixExpr extends PsiElement {

  @Nullable
  NixBinds getBinds();

  @NotNull
  List<NixExpr> getExprList();

  @Nullable
  NixExprOp getExprOp();

  @Nullable
  NixLambda getLambda();

  @Nullable
  PsiElement getAssert();

  @Nullable
  PsiElement getElse();

  @Nullable
  PsiElement getIf();

  @Nullable
  PsiElement getIn();

  @Nullable
  PsiElement getLet();

  @Nullable
  PsiElement getSemi();

  @Nullable
  PsiElement getThen();

  @Nullable
  PsiElement getWith();

}
