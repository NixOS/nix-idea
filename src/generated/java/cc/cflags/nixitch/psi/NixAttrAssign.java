// This is a generated file. Not intended for manual editing.
package cc.cflags.nixitch.psi;

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

}
