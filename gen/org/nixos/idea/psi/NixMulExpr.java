// This is a generated file. Not intended for manual editing.
package org.nixos.idea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface NixMulExpr extends PsiElement {

  @Nullable
  NixMulExpr getMulExpr();

  @NotNull
  List<NixPrimary> getPrimaryList();

  @Nullable
  PsiElement getDivide();

  @Nullable
  PsiElement getTimes();

}
