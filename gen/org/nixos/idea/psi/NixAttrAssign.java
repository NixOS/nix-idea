// This is a generated file. Not intended for manual editing.
package org.nixos.idea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface NixAttrAssign extends PsiElement {

  @NotNull
  NixAttrPath getAttrPath();

  @Nullable
  NixExpr getExpr();

  @NotNull
  PsiElement getAssign();

  String getAssignedAttr();

}
