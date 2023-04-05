package org.nixos.idea.psi.impl;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.openapi.util.Key;
import com.intellij.psi.TokenType;
import org.jetbrains.annotations.NotNull;

public final class NixParserUtil extends GeneratedParserUtilBase {
  private static final Key<Boolean> IS_IN_BINDING = Key.create("IS_IN_BINDING");

  private NixParserUtil() {} // Cannot be instantiated.

  public static boolean parseBindValue(@NotNull PsiBuilder builder, int level, @NotNull Parser expr) {
    Boolean oldValue = builder.getUserData(IS_IN_BINDING);
    try {
      builder.putUserData(IS_IN_BINDING, true);
      return expr.parse(builder, level);
    }
    finally {
      builder.putUserData(IS_IN_BINDING, oldValue);
    }
  }

  public static boolean parseNonBindValue(@NotNull PsiBuilder builder, int level, @NotNull Parser expr) {
    Boolean oldValue = builder.getUserData(IS_IN_BINDING);
    try {
      builder.putUserData(IS_IN_BINDING, null);
      return expr.parse(builder, level);
    }
    finally {
      builder.putUserData(IS_IN_BINDING, oldValue);
    }
  }

  public static boolean parseIsBindValue(@NotNull PsiBuilder builder, int level) {
    Boolean isInBinding = builder.getUserData(IS_IN_BINDING);
    return isInBinding != null && isInBinding;
  }

  /**
   * Calls "next" only if it's not occurring after a whitespace token.
   * Whitespace is automatically skipped by {@link PsiBuilder}, therefore we're checking the previous raw token
   * and the current raw token.
   */
  public static boolean not_after_whitespace(@NotNull PsiBuilder builder, int level, @NotNull Parser next) {
    return builder.rawLookup(-1) != TokenType.WHITE_SPACE
            && builder.rawLookup(0) != TokenType.WHITE_SPACE
            && next.parse(builder, level);
  }
}
