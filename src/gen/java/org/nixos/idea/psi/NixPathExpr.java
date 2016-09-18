// This is a generated file. Not intended for manual editing.
package org.nixos.idea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface NixPathExpr extends PsiElement {

  @Nullable
  NixListExpr getListExpr();

  @Nullable
  PsiElement getHpath();

  @Nullable
  PsiElement getPath();

  @Nullable
  PsiElement getSpath();

}
