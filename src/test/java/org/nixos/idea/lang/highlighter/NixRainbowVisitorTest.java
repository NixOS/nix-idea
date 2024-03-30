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
        doTest("""
                let
                  <rainbow color='ff000004'>x</rainbow> = null;
                in [
                  <rainbow color='ff000004'>x</rainbow>
                  <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow>
                  <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow>.<rainbow color='ff000002'>z</rainbow>
                  <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000003'>z</rainbow>
                  <rainbow color='ff000004'>x</rainbow>."no-highlighting-for-string-attributes"
                ]""");
    }

    public void testInheritExpression() {
        doTest("""
                let
                  <rainbow color='ff000004'>x</rainbow> = null;
                  <rainbow color='ff000002'>y</rainbow> = null;
                in {
                  inherit <rainbow color='ff000004'>x</rainbow>;
                  inherit <rainbow color='ff000004'>x</rainbow> <rainbow color='ff000002'>y</rainbow>;
                  inherit "no-highlighting-for-string-attributes";
                  inherit ({}) not-a-variable;
                }""");
    }

    public void testInheritExpressionInNestedLetExpression() {
        doTest("""
                let
                  <rainbow color='ff000003'>b</rainbow> = null;
                  <rainbow color='ff000001'>c</rainbow> = null;
                in
                  let
                    inherit <rainbow color='ff000001'>c</rainbow>;
                    inherit ({}) <rainbow color='ff000003'>a</rainbow> <rainbow color='ff000004'>b</rainbow>;
                  in [
                    <rainbow color='ff000003'>a</rainbow>
                    <rainbow color='ff000004'>b</rainbow>
                    <rainbow color='ff000001'>c</rainbow>
                  ]""");
    }

    public void testLetExpression() {
        doTest("""
                let
                  inherit (null) "no-highlighting-for-string-attributes" <rainbow color='ff000004'>x</rainbow>;
                  <rainbow color='ff000004'>x</rainbow> = null;
                  <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow> = null;
                  <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow>.<rainbow color='ff000002'>z</rainbow> = null;
                  <rainbow color='ff000004'>x</rainbow>."no-highlighting-for-string-attributes" = null;
                  <rainbow color='ff000003'>copy</rainbow> = <rainbow color='ff000004'>x</rainbow>;
                in [
                  <rainbow color='ff000004'>x</rainbow>
                  <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow>
                  <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow>.<rainbow color='ff000002'>z</rainbow>
                ]""");
    }

    public void testLegacyLetExpression() {
        doTest("""
                let {
                  inherit (null) "no-highlighting-for-string-attributes" <rainbow color='ff000004'>x</rainbow>;
                  <rainbow color='ff000004'>x</rainbow> = null;
                  <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow> = null;
                  <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow>.<rainbow color='ff000002'>z</rainbow> = null;
                  <rainbow color='ff000004'>x</rainbow>."no-highlighting-for-string-attributes" = null;
                  <rainbow color='ff000003'>body</rainbow> = [
                    <rainbow color='ff000004'>x</rainbow>
                    <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow>
                    <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow>.<rainbow color='ff000002'>z</rainbow>
                  ];
                }""");
    }

    public void testRecursiveSet() {
        doTest("""
                rec {
                  inherit (null) "no-highlighting-for-string-attributes" <rainbow color='ff000004'>x</rainbow>;
                  <rainbow color='ff000004'>x</rainbow> = null;
                  <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow> = null;
                  <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow>.<rainbow color='ff000002'>z</rainbow> = null;
                  <rainbow color='ff000004'>x</rainbow>."no-highlighting-for-string-attributes" = null;
                  <rainbow color='ff000003'>body</rainbow> = [
                    <rainbow color='ff000004'>x</rainbow>
                    <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow>
                    <rainbow color='ff000004'>x</rainbow>.<rainbow color='ff000001'>y</rainbow>.<rainbow color='ff000002'>z</rainbow>
                  ];
                }""");
    }

    public void testNoHighlightingForNonRecursiveSet() {
        doTest("""
                {
                  inherit (null) "no-highlighting-for-string-attributes" x;
                  x = null;
                  x.y = null;
                  x."no-highlighting-for-string-attributes" = null;
                }""");
    }

    public void testLambda() {
        // TODO: Ideally, za should have the same color as z.za.
        doTest("""
                <rainbow color='ff000004'>x</rainbow>:
                {<rainbow color='ff000002'>y</rainbow>}:
                <rainbow color='ff000003'>z</rainbow>@{<rainbow color='ff000001'>za</rainbow>, ...}: [
                  <rainbow color='ff000004'>x</rainbow>
                  <rainbow color='ff000002'>y</rainbow>
                  <rainbow color='ff000003'>z</rainbow>
                  <rainbow color='ff000001'>za</rainbow>
                ]""");
    }

    public void testUnknownSource() {
        doTest("""
                [
                  <rainbow color='ff000004'>x</rainbow>
                  <rainbow color='ff000001'>compareVersions</rainbow>
                ]""");
    }

    public void testNoRainbowForBuiltins() {
        doTest("""
                [
                  null
                  true
                  false
                  import
                  map
                  builtins.null
                  builtins.map
                  builtins.compareVersions
                ]""");
    }

    // TODO: Ideally, hidden elements should have a different color then the elements hiding them. Unfortunately,
    //  I haven't found a good way to implement this.
    @SuppressWarnings("unused")
    public void ignoreTestHidingChangesColor() {
        doTest("""
                {<rainbow color='ff000004'>f</rainbow>, <rainbow color='ff000002'>hidden</rainbow>}: [
                  <rainbow color='ff000004'>f</rainbow> <rainbow color='ff000002'>hidden</rainbow>
                  (let <rainbow color='ff000001'>hidden</rainbow> = null; in <rainbow color='ff000004'>f</rainbow> <rainbow color='ff000001'>hidden</rainbow>)
                  (let { <rainbow color='ff000001'>hidden</rainbow> = null; <rainbow color='ff000003'>body</rainbow> = <rainbow color='ff000004'>f</rainbow> <rainbow color='ff000001'>hidden</rainbow>; })
                  (rec { <rainbow color='ff000001'>hidden</rainbow> = null; <rainbow color='ff000003'>body</rainbow> = <rainbow color='ff000004'>f</rainbow> <rainbow color='ff000001'>hidden</rainbow>; })
                  (<rainbow color='ff000001'>hidden</rainbow>: <rainbow color='ff000004'>f</rainbow> <rainbow color='ff000001'>hidden</rainbow>)
                ]""");
    }

    private void doTest(@NotNull String code) {
        myFixture.testRainbow("rainbow.nix", code, true, true);
    }
}
