// This is a generated file. Not intended for manual editing.
package org.nixos.idea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface NixExprSelect extends PsiElement {

  @Nullable
  NixAttrPath getAttrPath();

  @Nullable
  NixExprSelect getExprSelect();

  @NotNull
  NixExprSimple getExprSimple();

  @Nullable
  PsiElement getDot();

  @Nullable
  PsiElement getOrKw();

  @Nullable
  PsiElement getOwKw();

}
