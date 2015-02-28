// This is a generated file. Not intended for manual editing.
package cc.cflags.nixitch.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface NixInheritAttrs extends PsiElement {

  @NotNull
  NixAttrs getAttrs();

  @Nullable
  NixExpr getExpr();

  @NotNull
  PsiElement getInherit();

  @Nullable
  PsiElement getLparen();

  @Nullable
  PsiElement getRparen();

}
