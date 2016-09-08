// This is a generated file. Not intended for manual editing.
package org.nixos.idea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface NixExprSimple extends PsiElement {

  @Nullable
  NixBindSet getBindSet();

  @Nullable
  NixEvalExpr getEvalExpr();

  @Nullable
  NixExprList getExprList();

  @Nullable
  NixImportStmt getImportStmt();

  @Nullable
  NixIndStringParts getIndStringParts();

  @Nullable
  NixLiteral getLiteral();

  @Nullable
  NixStringParts getStringParts();

  @Nullable
  PsiElement getIndStringClose();

  @Nullable
  PsiElement getIndStringOpen();

  @Nullable
  PsiElement getLbrac();

  @Nullable
  PsiElement getLet();

  @Nullable
  PsiElement getRbrac();

  @Nullable
  PsiElement getRec();

}
