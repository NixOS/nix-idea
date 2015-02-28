// This is a generated file. Not intended for manual editing.
package cc.cflags.nixitch.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface NixBinds extends PsiElement {

  @NotNull
  List<NixAttrAssign> getAttrAssignList();

  @NotNull
  List<NixInheritAttrs> getInheritAttrsList();

  @NotNull
  List<NixRequireExpr> getRequireExprList();

}
