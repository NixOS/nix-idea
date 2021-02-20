package org.nixos.idea.lang;

import com.intellij.testFramework.ParsingTestCase;
import com.intellij.testFramework.TestDataPath;
import org.nixos.idea.lang.NixParserDefinition;

@TestDataPath("$PROJECT_ROOT/src/test/testData/ParsingFailTest")
public final class ParsingFailTest extends ParsingTestCase {
  public ParsingFailTest() {
    super("ParsingFailTest", "nix", new NixParserDefinition());
  }

  // References for the syntax of the Nix Expression Language:
  //  https://nixos.org/guides/nix-pills/basics-of-language.html
  //  https://nixos.org/manual/nix/stable/#ch-expression-language
  //  https://github.com/NixOS/nix/blob/master/src/libexpr/parser.y

  public void testComment() {
    doTest(true);
  }

  public void testEmpty() {
    doTest(true);
  }

  public void testIncompleteExpressionsInBraces() {
    doTest(true);
  }

  public void testMissingSemicolon() {
    doTest(true);
  }

  public void testMissingSemicolonTrap() {
    doTest(true);
  }

  public void testRecoverFromAssertCondition() {
    doTest(true);
  }

  public void testRecoverFromIfCondition() {
    doTest(true);
  }

  public void testRecoverFromIfThenExpression() {
    doTest(true);
  }

  public void testRecoverFromLetBinding() {
    doTest(true);
  }

  public void testRecoverFromListItem() {
    doTest(true);
  }

  public void testRecoverFromParensExprPart() {
    doTest(true);
  }

  public void testRecoverFromSetBinding() {
    doTest(true);
  }

  public void testRecoverFromString() {
    doTest(true);
  }

  public void testRecoverFromWith() {
    doTest(true);
  }

  public void testRecWithoutSet() {
    doTest(true);
  }

  @Override
  protected String getTestDataPath() {
    return "src/test/testData";
  }

  @Override
  protected boolean includeRanges() {
    return true;
  }
}
