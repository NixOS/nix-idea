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

  public void testLet() {
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
  protected String getTestDataPath() {
    return "src/test/testData";
  }

  @Override
  protected boolean includeRanges() {
    return true;
  }
}
