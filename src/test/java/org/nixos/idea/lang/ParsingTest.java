package org.nixos.idea.lang;

import com.intellij.testFramework.ParsingTestCase;
import com.intellij.testFramework.TestDataPath;
import org.nixos.idea.lang.NixParserDefinition;

@TestDataPath("$PROJECT_ROOT/src/test/testData/ParsingTest")
public final class ParsingTest extends ParsingTestCase {
  public ParsingTest() {
    super("ParsingTest", "nix", new NixParserDefinition());
  }

  // References for the syntax of the Nix Expression Language:
  //  https://nixos.org/guides/nix-pills/basics-of-language.html
  //  https://nixos.org/manual/nix/stable/#ch-expression-language
  //  https://github.com/NixOS/nix/blob/master/src/libexpr/parser.y

  public void testAssertion() {
    doTest(true);
  }

  public void testBoolean() {
    doTest(true);
  }

  public void testFunction() {
    doTest(true);
  }

  public void testIdentifier() {
    doTest(true);
  }

  public void testIf() {
    doTest(true);
  }

  public void testLegacyLet() {
    // See https://github.com/NixOS/nix/issues/867
    doTest(true);
  }

  public void testLegacyLetInList() {
    // See https://github.com/NixOS/nix/issues/867
    doTest(true);
  }

  public void testLegacyOrAsArgument() {
    // See https://github.com/NixOS/nix/issues/975 and
    // https://github.com/NixOS/nix/pull/1369.
    doTest(true);
  }

  public void testLegacyOrAsAttribute() {
    // See https://github.com/NixOS/nix/issues/975 and
    // https://github.com/NixOS/nix/pull/1369.
    doTest(true);
  }

  public void testLegacyOrInList() {
    // See https://github.com/NixOS/nix/issues/975 and
    // https://github.com/NixOS/nix/pull/1369.
    doTest(true);
  }

  public void testLet() {
    doTest(true);
  }

  public void testLetEmpty() {
    doTest(true);
  }

  public void testList() {
    doTest(true);
  }

  public void testListWithFunction() {
    doTest(true);
  }

  public void testNull() {
    doTest(true);
  }

  public void testNumber() {
    doTest(true);
  }

  public void testOperators() {
    doTest(true);
  }

  public void testOperatorsAssociativity() {
    doTest(true);
  }

  public void testPath() {
    doTest(true);
  }

  public void testRecursiveSet() {
    doTest(true);
  }

  public void testSet() {
    doTest(true);
  }

  public void testSetAccess() {
    doTest(true);
  }

  public void testString() {
    doTest(true);
  }

  public void testStringWithAntiquotation() {
    doTest(true);
  }

  public void testStringWithEscapeSequences() {
    doTest(true);
  }

  public void testStringWithMultipleLines() {
    doTest(true);
  }

  public void testUri() {
    doTest(true);
  }

  public void testWith() {
    doTest(true);
  }

  @Override
  protected void tearDown() throws Exception {
    // Ensure that the parser does not generate errors even when the errors have
    // accidentally been added to the expected result.
    ensureNoErrorElements();
    super.tearDown();
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
