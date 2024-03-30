package org.nixos.idea.lang.highlighter;

import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;

public final class NixRainbowVisitorTest extends BasePlatformTestCase {

    public void testNoHighlightingWhenDisabled() {
        myFixture.testRainbow("rainbow.nix",
                "let x = y; in x",
                false, false);
    }

    public void testSelectExpression() {
        doTest("let\n" +
                "  <rainbow color='ff000004'>x</rainbow> = null;\n" +
                "in [\n" +
                "  <rainbow color='ff000004'>x</rainbow>\n" +
                "  <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow>\n" +
                "  <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow>.<rainbow color='ff000002'>z</rainbow>\n" +
                "  <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000003'>z</rainbow>\n" +
                "  <rainbow color='ff000004'>x</rainbow>.\"no-highlighting-for-string-attributes\"\n" +
                "]");
    }

    public void testInheritExpression() {
        doTest("let\n" +
                "  <rainbow color='ff000004'>x</rainbow> = null;\n" +
                "  <rainbow color='ff000002'>y</rainbow> = null;\n" +
                "in {\n" +
                "  inherit <rainbow color='ff000004'>x</rainbow>;\n" +
                "  inherit <rainbow color='ff000004'>x</rainbow> <rainbow color='ff000002'>y</rainbow>;\n" +
                "  inherit \"no-highlighting-for-string-attributes\";\n" +
                "  inherit ({}) not-a-variable;\n" +
                "}");
    }

    public void testInheritExpressionInNestedLetExpression() {
        doTest("let\n" +
                "  <rainbow color='ff000003'>b</rainbow> = null;\n" +
                "  <rainbow color='ff000001'>c</rainbow> = null;\n" +
                "in\n" +
                "  let\n" +
                "    inherit <rainbow color='ff000001'>c</rainbow>;\n" +
                "    inherit ({}) <rainbow color='ff000003'>a</rainbow> <rainbow color='ff000004'>b</rainbow>;\n" +
                "  in [\n" +
                "    <rainbow color='ff000003'>a</rainbow>\n" +
                "    <rainbow color='ff000004'>b</rainbow>\n" +
                "    <rainbow color='ff000001'>c</rainbow>\n" +
                "  ]");
    }

    public void testLetExpression() {
        doTest("let\n" +
                "  inherit (null) \"no-highlighting-for-string-attributes\" <rainbow color='ff000004'>x</rainbow>;\n" +
                "  <rainbow color='ff000004'>x</rainbow> = null;\n" +
                "  <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow> = null;\n" +
                "  <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow>.<rainbow color='ff000002'>z</rainbow> = null;\n" +
                "  <rainbow color='ff000004'>x</rainbow>.\"no-highlighting-for-string-attributes\" = null;\n" +
                "  <rainbow color='ff000003'>copy</rainbow> = <rainbow color='ff000004'>x</rainbow>;\n" +
                "in [\n" +
                "  <rainbow color='ff000004'>x</rainbow>\n" +
                "  <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow>\n" +
                "  <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow>.<rainbow color='ff000002'>z</rainbow>\n" +
                "]");
    }

    public void testLegacyLetExpression() {
        doTest("let {\n" +
                "  inherit (null) \"no-highlighting-for-string-attributes\" <rainbow color='ff000004'>x</rainbow>;\n" +
                "  <rainbow color='ff000004'>x</rainbow> = null;\n" +
                "  <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow> = null;\n" +
                "  <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow>.<rainbow color='ff000002'>z</rainbow> = null;\n" +
                "  <rainbow color='ff000004'>x</rainbow>.\"no-highlighting-for-string-attributes\" = null;\n" +
                "  <rainbow color='ff000003'>body</rainbow> = [\n" +
                "    <rainbow color='ff000004'>x</rainbow>\n" +
                "    <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow>\n" +
                "    <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow>.<rainbow color='ff000002'>z</rainbow>\n" +
                "  ];\n" +
                "}");
    }

    public void testRecursiveSet() {
        doTest("rec {\n" +
                "  inherit (null) \"no-highlighting-for-string-attributes\" <rainbow color='ff000004'>x</rainbow>;\n" +
                "  <rainbow color='ff000004'>x</rainbow> = null;\n" +
                "  <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow> = null;\n" +
                "  <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow>.<rainbow color='ff000002'>z</rainbow> = null;\n" +
                "  <rainbow color='ff000004'>x</rainbow>.\"no-highlighting-for-string-attributes\" = null;\n" +
                "  <rainbow color='ff000003'>body</rainbow> = [\n" +
                "    <rainbow color='ff000004'>x</rainbow>\n" +
                "    <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow>\n" +
                "    <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow>.<rainbow color='ff000002'>z</rainbow>\n" +
                "  ];\n" +
                "}");
    }

    public void testNoHighlightingForNonRecursiveSet() {
        doTest("{\n" +
                "  inherit (null) \"no-highlighting-for-string-attributes\" x;\n" +
                "  x = null;\n" +
                "  x.y = null;\n" +
                "  x.\"no-highlighting-for-string-attributes\" = null;\n" +
                "}");
    }

    public void testLambda() {
        // TODO: Ideally, za should have the same color as z.za.
        doTest("<rainbow color='ff000004'>x</rainbow>:\n" +
                "{<rainbow color='ff000002'>y</rainbow>}:\n" +
                "<rainbow color='ff000003'>z</rainbow>@{<rainbow color='ff000001'>za</rainbow>, ...}: [\n" +
                "  <rainbow color='ff000004'>x</rainbow>\n" +
                "  <rainbow color='ff000002'>y</rainbow>\n" +
                "  <rainbow color='ff000003'>z</rainbow>\n" +
                "  <rainbow color='ff000001'>za</rainbow>\n" +
                "]");
    }

    public void testUnknownSource() {
        doTest("[\n" +
                "  <rainbow color='ff000004'>x</rainbow>\n" +
                "  <rainbow color='ff000001'>compareVersions</rainbow>\n" +
                "]");
    }

    public void testNoRainbowForBuiltins() {
        doTest("[\n" +
                "  null\n" +
                "  true\n" +
                "  false\n" +
                "  import\n" +
                "  map\n" +
                "  builtins.null\n" +
                "  builtins.map\n" +
                "  builtins.compareVersions\n" +
                "]");
    }

    // TODO: Ideally, hidden elements should have a different color then the elements hiding them. Unfortunately,
    //  I haven't found a good way to implement this.
    @SuppressWarnings("unused")
    public void ignoreTestHidingChangesColor() {
        doTest("{<rainbow color='ff000004'>f</rainbow>, <rainbow color='ff000002'>hidden</rainbow>}: [\n" +
                "  <rainbow color='ff000004'>f</rainbow> <rainbow color='ff000002'>hidden</rainbow>\n" +
                "  (let <rainbow color='ff000001'>hidden</rainbow> = null; in <rainbow color='ff000004'>f</rainbow> <rainbow color='ff000001'>hidden</rainbow>)\n" +
                "  (let { <rainbow color='ff000001'>hidden</rainbow> = null; <rainbow color='ff000003'>body</rainbow> = <rainbow color='ff000004'>f</rainbow> <rainbow color='ff000001'>hidden</rainbow>; })\n" +
                "  (rec { <rainbow color='ff000001'>hidden</rainbow> = null; <rainbow color='ff000003'>body</rainbow> = <rainbow color='ff000004'>f</rainbow> <rainbow color='ff000001'>hidden</rainbow>; })\n" +
                "  (<rainbow color='ff000001'>hidden</rainbow>: <rainbow color='ff000004'>f</rainbow> <rainbow color='ff000001'>hidden</rainbow>)\n" +
                "]");
    }

    private void doTest(@NotNull String code) {
        myFixture.testRainbow("rainbow.nix", code, true, true);
    }
}
