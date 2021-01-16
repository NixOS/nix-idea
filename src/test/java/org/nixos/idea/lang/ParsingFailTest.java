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

  public void testComment() {
    doTest(true);
  }

  public void testEmpty() {
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
