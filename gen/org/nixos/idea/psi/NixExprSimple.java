// This is a generated file. Not intended for manual editing.
package org.nixos.idea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface NixExprSimple extends PsiElement {

  @Nullable
  NixAttrPath getAttrPath();

  @Nullable
  NixBindOrSelect getBindOrSelect();

  @Nullable
  NixBindSet getBindSet();

  @Nullable
  NixDefval getDefval();

  @Nullable
  NixEvalOrSelect getEvalOrSelect();

  @Nullable
  NixExprOp getExprOp();

  @Nullable
  NixImportStmt getImportStmt();

  @Nullable
  NixIndStringParts getIndStringParts();

  @Nullable
  NixList getList();

  @Nullable
  NixLiteral getLiteral();

  @Nullable
  NixStringParts getStringParts();

  @Nullable
  NixUnaryOp getUnaryOp();

  @Nullable
  PsiElement getIndStringClose();

  @Nullable
  PsiElement getIndStringOpen();

  @Nullable
  PsiElement getRec();

}
