// This is a generated file. Not intended for manual editing.
package cc.cflags.nixitch.psi;

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
  PsiElement getImpl();

  @Nullable
  PsiElement getTimes();

}
