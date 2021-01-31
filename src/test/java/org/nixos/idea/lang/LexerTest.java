package org.nixos.idea.lang;

import com.intellij.lexer.Lexer;
import com.intellij.testFramework.LexerTestCase;

public final class LexerTest extends LexerTestCase {
  public void testRestartabilityWithAntiquotations() {
    // Checks that the lexer is restartable. See
    // https://intellij-support.jetbrains.com/hc/en-us/community/posts/360010305800
    checkCorrectRestartOnEveryToken(
        "''\n" +
        "  ${\n" +
        "    [\n" +
        "      \"pure string\",\n" +
        "      \"string with ${antiquotation}\",\n" +
        "      ''ind string with ${multiple} ${antiquotations}''\n" +
        "    ]\n" +
        "  }\n" +
        "''\n");
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
