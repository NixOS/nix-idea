// This is a generated file. Not intended for manual editing.
package cc.cflags.nixitch.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface NixRelExpr extends PsiElement {

  @Nullable
  NixRelExpr getRelExpr();

  @NotNull
  List<NixRelative> getRelativeList();

  @Nullable
  PsiElement getEq();

  @Nullable
  PsiElement getGeq();

  @Nullable
  PsiElement getGt();

  @Nullable
  PsiElement getLeq();

  @Nullable
  PsiElement getLt();

  @Nullable
  PsiElement getNeq();

}
