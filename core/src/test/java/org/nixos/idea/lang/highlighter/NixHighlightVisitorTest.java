package org.nixos.idea.lang.highlighter;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.testFramework.ExpectedHighlightingData;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.file.NixFileType;

import java.lang.reflect.Field;
import java.util.Collection;

public final class NixHighlightVisitorTest extends BasePlatformTestCase {

    public void testSelectExpression() {
        // TODO: Highlight x.y as a local variable
        doTest("""
                let
                  <symbolName type="LOCAL_VARIABLE">x</symbolName> = some_value;
                  <symbolName type="LOCAL_VARIABLE">x</symbolName>.y = some_value;
                  <symbolName type="LOCAL_VARIABLE">x</symbolName>."no-highlighting-for-string-attributes" = some_value;
                in [
                  <symbolName type="LOCAL_VARIABLE">x</symbolName>
                  <symbolName type="LOCAL_VARIABLE">x</symbolName>.y
                  <symbolName type="LOCAL_VARIABLE">x</symbolName>.y.z
                  <symbolName type="LOCAL_VARIABLE">x</symbolName>.z
                  <symbolName type="LOCAL_VARIABLE">x</symbolName>."no-highlighting-for-string-attributes"
                ]""");
    }

    public void testInheritExpression() {
        doTest("""
                let
                  <symbolName type="LOCAL_VARIABLE">x</symbolName> = some_value;
                  <symbolName type="LOCAL_VARIABLE">y</symbolName> = some_value;
                in {
                  inherit <symbolName type="LOCAL_VARIABLE">x</symbolName>;
                  inherit <symbolName type="LOCAL_VARIABLE">x</symbolName> <symbolName type="LOCAL_VARIABLE">y</symbolName>;
                  inherit "no-highlighting-for-string-attributes";
                  inherit ({}) not-a-variable;
                }""");
    }

    public void testBuiltins() {
        doTest("""
                [
                  <symbolName type="LITERAL">null</symbolName>
                  <symbolName type="LITERAL">true</symbolName>
                  <symbolName type="LITERAL">false</symbolName>
                  <symbolName type="IMPORT">import</symbolName>
                  <symbolName type="BUILTIN">map</symbolName>
                  <symbolName type="BUILTIN">builtins</symbolName>.<symbolName type="LITERAL">null</symbolName>
                  <symbolName type="BUILTIN">builtins</symbolName>.<symbolName type="BUILTIN">map</symbolName>
                  <symbolName type="BUILTIN">builtins</symbolName>.<symbolName type="BUILTIN">compareVersions</symbolName>
                  compareVersions
                ]""");
    }

    public void testLetExpression() {
        doTest("""
                let
                  inherit (some_value) "no-highlighting-for-string-attributes" <symbolName type="LOCAL_VARIABLE">x</symbolName>;
                  <symbolName type="LOCAL_VARIABLE">x</symbolName> = some_value;
                  <symbolName type="LOCAL_VARIABLE">x</symbolName>.y = some_value;
                  <symbolName type="LOCAL_VARIABLE">x</symbolName>.y.z = some_value;
                  <symbolName type="LOCAL_VARIABLE">x</symbolName>."no-highlighting-for-string-attributes" = some_value;
                  <symbolName type="LOCAL_VARIABLE">copy</symbolName> = <symbolName type="LOCAL_VARIABLE">x</symbolName>;
                in [
                  <symbolName type="LOCAL_VARIABLE">x</symbolName>
                  <symbolName type="LOCAL_VARIABLE">x</symbolName>.y
                  <symbolName type="LOCAL_VARIABLE">x</symbolName>.y.z
                ]""");
    }

    public void testLegacyLetExpression() {
        doTest("""
                let {
                  inherit (some_value) "no-highlighting-for-string-attributes" <symbolName type="LOCAL_VARIABLE">x</symbolName>;
                  <symbolName type="LOCAL_VARIABLE">x</symbolName> = some_value;
                  <symbolName type="LOCAL_VARIABLE">x</symbolName>.y = some_value;
                  <symbolName type="LOCAL_VARIABLE">x</symbolName>.y.z = some_value;
                  <symbolName type="LOCAL_VARIABLE">x</symbolName>."no-highlighting-for-string-attributes" = some_value;
                  <symbolName type="LOCAL_VARIABLE">body</symbolName> = [
                    <symbolName type="LOCAL_VARIABLE">x</symbolName>
                    <symbolName type="LOCAL_VARIABLE">x</symbolName>.y
                    <symbolName type="LOCAL_VARIABLE">x</symbolName>.y.z
                  ];
                }""");
    }

    public void testRecursiveSet() {
        doTest("""
                rec {
                  inherit (some_value) "no-highlighting-for-string-attributes" <symbolName type="LOCAL_VARIABLE">x</symbolName>;
                  <symbolName type="LOCAL_VARIABLE">x</symbolName> = some_value;
                  <symbolName type="LOCAL_VARIABLE">x</symbolName>.y = some_value;
                  <symbolName type="LOCAL_VARIABLE">x</symbolName>.y.z = some_value;
                  <symbolName type="LOCAL_VARIABLE">x</symbolName>."no-highlighting-for-string-attributes" = some_value;
                  <symbolName type="LOCAL_VARIABLE">body</symbolName> = [
                    <symbolName type="LOCAL_VARIABLE">x</symbolName>
                    <symbolName type="LOCAL_VARIABLE">x</symbolName>.y
                    <symbolName type="LOCAL_VARIABLE">x</symbolName>.y.z
                  ];
                }""");
    }

    public void testNoHighlightingForNonRecursiveSet() {
        doTest("""
                {
                  inherit (some_value) "no-highlighting-for-string-attributes" x;
                  x = some_value;
                  x.y = some_value;
                  x."no-highlighting-for-string-attributes" = some_value;
                }""");
    }

    public void testLambda() {
        doTest("""
                <symbolName type="PARAMETER">x</symbolName>:
                {<symbolName type="PARAMETER">y</symbolName>}:
                <symbolName type="PARAMETER">z</symbolName>@{<symbolName type="PARAMETER">za</symbolName>, ...}: [
                  <symbolName type="PARAMETER">x</symbolName>
                  <symbolName type="PARAMETER">y</symbolName>
                  <symbolName type="PARAMETER">z</symbolName>
                  <symbolName type="PARAMETER">za</symbolName>
                ]""");
    }

    public void testVariableHidesParameter() {
        doTest("""
                {<symbolName type="PARAMETER">f</symbolName>, <symbolName type="PARAMETER">hidden</symbolName>}: [
                  <symbolName type="PARAMETER">f</symbolName> <symbolName type="PARAMETER">hidden</symbolName>
                  (
                    let <symbolName type="LOCAL_VARIABLE">hidden</symbolName> = some_value;
                    in <symbolName type="PARAMETER">f</symbolName> <symbolName type="LOCAL_VARIABLE">hidden</symbolName>
                  )
                  (let {
                    <symbolName type="LOCAL_VARIABLE">hidden</symbolName> = some_value;
                    <symbolName type="LOCAL_VARIABLE">body</symbolName> = <symbolName type="PARAMETER">f</symbolName> <symbolName type="LOCAL_VARIABLE">hidden</symbolName>;
                  })
                  (rec {
                    <symbolName type="LOCAL_VARIABLE">hidden</symbolName> = some_value;
                    <symbolName type="LOCAL_VARIABLE">body</symbolName> = <symbolName type="PARAMETER">f</symbolName> <symbolName type="LOCAL_VARIABLE">hidden</symbolName>;
                  })
                ]""");
    }

    public void testParameterHidesVariable() {
        doTest("""
                let
                  <symbolName type="LOCAL_VARIABLE">f</symbolName> = some_value;
                  <symbolName type="LOCAL_VARIABLE">hidden</symbolName> = some_value;
                in
                  <symbolName type="PARAMETER">hidden</symbolName>:
                  <symbolName type="LOCAL_VARIABLE">f</symbolName> <symbolName type="PARAMETER">hidden</symbolName>""");
    }

    private void doTest(@NotNull String code) {
        PsiFile file = myFixture.configureByText(NixFileType.INSTANCE, code);
        Document document = myFixture.getEditor().getDocument();
        PsiManagerEx.getInstanceEx(getProject()).setAssertOnFileLoadingFilter(VirtualFileFilter.NONE, getTestRootDisposable());
        ExpectedHighlightingData data = new NixExpectedHighlightingData(document, false);
        data.init();
        Collection<HighlightInfo> infos = myFixture.doHighlighting();
        data.checkResult(file, infos, document.getText(), null);
    }

    private static final class NixExpectedHighlightingData extends ExpectedHighlightingData {
        public NixExpectedHighlightingData(@NotNull Document document, boolean ignoreExtraHighlighting) {
            super(document, true, true, true, ignoreExtraHighlighting);
            checkSymbolNames();
        }

        @Override
        protected HighlightInfoType getTypeByName(String typeString) throws Exception {
            try {
                Field field = NixHighlightVisitorDelegate.class.getField(typeString);
                return (HighlightInfoType) field.get(null);
            } catch (NoSuchFieldException e) {
                return super.getTypeByName(typeString);
            }
        }
    }
}
