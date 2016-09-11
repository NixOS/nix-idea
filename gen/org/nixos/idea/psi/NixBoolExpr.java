// This is a generated file. Not intended for manual editing.
package org.nixos.idea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface NixBoolExpr extends PsiElement {

  @Nullable
  NixBoolExpr getBoolExpr();

  @NotNull
  List<NixLogical> getLogicalList();

  @Nullable
  PsiElement getAnd();

  @Nullable
  PsiElement getImpl();

  @Nullable
  PsiElement getOr();

  @Nullable
  PsiElement getOrKw();

}
