// This is a generated file. Not intended for manual editing.
package cc.cflags.nixitch.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface NixRequireExpr extends PsiElement {

  @NotNull
  NixPathsAssign getPathsAssign();

  @Nullable
  PsiElement getImports();

  @Nullable
  PsiElement getRequire();

  @Nullable
  PsiElement getRequires();

}
