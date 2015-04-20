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

public class NixPathStmtImpl extends ASTWrapperPsiElement implements NixPathStmt {

  public NixPathStmtImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NixVisitor) ((NixVisitor)visitor).visitPathStmt(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public NixStringParts getStringParts() {
    return findChildByClass(NixStringParts.class);
  }

  @Override
  @Nullable
  public PsiElement getHpath() {
    return findChildByType(HPATH);
  }

  @Override
  @Nullable
  public PsiElement getPath() {
    return findChildByType(PATH);
  }

  @Override
  @Nullable
  public PsiElement getSpath() {
    return findChildByType(SPATH);
  }

}
