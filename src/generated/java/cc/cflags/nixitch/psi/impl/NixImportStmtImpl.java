// This is a generated file. Not intended for manual editing.
package cc.cflags.nixitch.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static cc.cflags.nixitch.psi.NixTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import cc.cflags.nixitch.psi.*;

public class NixImportStmtImpl extends ASTWrapperPsiElement implements NixImportStmt {

  public NixImportStmtImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NixVisitor) ((NixVisitor)visitor).visitImportStmt(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public NixCallArgs getCallArgs() {
    return findChildByClass(NixCallArgs.class);
  }

  @Override
  @NotNull
  public NixPathStmt getPathStmt() {
    return findNotNullChildByClass(NixPathStmt.class);
  }

  @Override
  @NotNull
  public PsiElement getImport() {
    return findNotNullChildByType(IMPORT);
  }

}
