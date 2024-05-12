package org.nixos.idea.lang;

import com.intellij.lang.LanguageBraceMatching;
import com.intellij.testFramework.ParsingTestCase;
import com.intellij.testFramework.TestDataPath;
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy;

import java.util.Objects;

@TestDataPath("$PROJECT_ROOT/src/test/testData/ParsingFailTest")
public final class ParsingFailTest extends ParsingTestCase {
  public ParsingFailTest() {
    super("ParsingFailTest", "nix", new NixParserDefinition());
  }

  // References for the syntax of the Nix Expression Language:
  //  https://nixos.org/guides/nix-pills/basics-of-language.html
  //  https://nixos.org/manual/nix/stable/#ch-expression-language
  //  https://github.com/NixOS/nix/blob/master/src/libexpr/parser.y

  public void testAntiquotationStateSynchronization1() {
    // See https://intellij-support.jetbrains.com/hc/en-us/community/posts/360010379000
    doTest(true);
  }

  public void testAntiquotationStateSynchronization2() {
    // See https://intellij-support.jetbrains.com/hc/en-us/community/posts/360010379000
    doTest(true);
  }

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

  public void testRecoverFromAntiquotation() {
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

  public void testRecoverFromParens() {
    doTest(true);
  }

  public void testRecoverFromParensWithValidSubexpressions() {
    doTest(true);
  }

  public void testRecoverFromSetBinding() {
    doTest(true);
  }

  public void testRecoverFromWith() {
    doTest(true);
  }

  public void testRecWithoutSet() {
    doTest(true);
  }

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // Test environment of ParsingTestCase does not detect the brace matcher on its own. The brace matcher effects the
    // error recovery of Grammar-Kit and must therefore be registered to get correct test results.
    addExplicitExtension(LanguageBraceMatching.INSTANCE, NixLanguage.INSTANCE, new NixBraceMatcher());
  }

  @Override
  protected String getTestDataPath() {
    return Objects.requireNonNull(IdeaTestExecutionPolicy.current()).getHomePath();
  }

  @Override
  protected boolean includeRanges() {
    return true;
  }
}
