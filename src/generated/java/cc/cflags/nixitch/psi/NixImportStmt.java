// This is a generated file. Not intended for manual editing.
package cc.cflags.nixitch.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface NixImportStmt extends PsiElement {

  @Nullable
  NixCallArgs getCallArgs();

  @NotNull
  NixPathStmt getPathStmt();

  @NotNull
  PsiElement getImport();

}
