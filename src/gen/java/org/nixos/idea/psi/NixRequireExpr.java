// This is a generated file. Not intended for manual editing.
package org.nixos.idea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface NixRequireExpr extends PsiElement {

  @NotNull
  List<NixPathExpr> getPathExprList();

  @NotNull
  PsiElement getAssign();

  @Nullable
  PsiElement getImports();

  @NotNull
  PsiElement getLbrac();

  @NotNull
  PsiElement getRbrac();

  @Nullable
  PsiElement getRequire();

  @Nullable
  PsiElement getRequires();

}
