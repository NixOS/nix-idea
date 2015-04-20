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

public class NixLiteralImpl extends ASTWrapperPsiElement implements NixLiteral {

  public NixLiteralImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof NixVisitor) ((NixVisitor)visitor).visitLiteral(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PsiElement getBool() {
    return findChildByType(BOOL);
  }

  @Override
  @Nullable
  public PsiElement getHpath() {
    return findChildByType(HPATH);
  }

  @Override
  @Nullable
  public PsiElement getId() {
    return findChildByType(ID);
  }

  @Override
  @Nullable
  public PsiElement getInt() {
    return findChildByType(INT);
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

  @Override
  @Nullable
  public PsiElement getStr() {
    return findChildByType(STR);
  }

  @Override
  @Nullable
  public PsiElement getUri() {
    return findChildByType(URI);
  }

}
