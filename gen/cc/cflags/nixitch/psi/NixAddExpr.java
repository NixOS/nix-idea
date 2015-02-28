// This is a generated file. Not intended for manual editing.
package cc.cflags.nixitch.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface NixAddExpr extends PsiElement {

  @Nullable
  NixAddExpr getAddExpr();

  @NotNull
  List<NixFactor> getFactorList();

  @Nullable
  PsiElement getConcat();

  @Nullable
  PsiElement getMinus();

  @Nullable
  PsiElement getPlus();

  @Nullable
  PsiElement getUpdate();

}
