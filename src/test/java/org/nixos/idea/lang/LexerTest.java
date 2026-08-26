package org.nixos.idea.lang;

import com.intellij.lexer.Lexer;
import com.intellij.testFramework.LexerTestCase;

public final class LexerTest extends LexerTestCase {
  public void testRestartabilityWithStringInterpolation() {
    // Checks that the lexer is restartable. See
    // https://intellij-support.jetbrains.com/hc/en-us/community/posts/360010305800
    checkCorrectRestart("""
            ''
              ${
                [
                  "pure string",
                  "string with ${interpolation}",
                  ''ind string with ${multiple} ${interpolations}''
                ]
              }
            ''
            """);
  }

  @Override
  protected Lexer createLexer() {
    return new NixLexer();
  }

  @Override
  protected String getDirPath() {
    // We do not use loadTestDataFile(...)
    throw new UnsupportedOperationException();
  }
}
