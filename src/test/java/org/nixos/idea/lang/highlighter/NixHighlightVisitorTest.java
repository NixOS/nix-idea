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
        doTest("let\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">x</symbolName> = some_value;\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>.y = some_value;\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>.\"no-highlighting-for-string-attributes\" = some_value;\n" +
                "in [\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>.y\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>.y.z\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>.z\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>.\"no-highlighting-for-string-attributes\"\n" +
                "]");
    }

    public void testInheritExpression() {
        doTest("let\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">x</symbolName> = some_value;\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">y</symbolName> = some_value;\n" +
                "in {\n" +
                "  inherit <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>;\n" +
                "  inherit <symbolName type=\"LOCAL_VARIABLE\">x</symbolName> <symbolName type=\"LOCAL_VARIABLE\">y</symbolName>;\n" +
                "}");
    }

    public void testBuiltins() {
        doTest("[\n" +
                "  <symbolName type=\"LITERAL\">null</symbolName>\n" +
                "  <symbolName type=\"LITERAL\">true</symbolName>\n" +
                "  <symbolName type=\"LITERAL\">false</symbolName>\n" +
                "  <symbolName type=\"IMPORT\">import</symbolName>\n" +
                "  <symbolName type=\"BUILTIN\">map</symbolName>\n" +
                "  <symbolName type=\"BUILTIN\">builtins</symbolName>.<symbolName type=\"LITERAL\">null</symbolName>\n" +
                "  <symbolName type=\"BUILTIN\">builtins</symbolName>.<symbolName type=\"BUILTIN\">map</symbolName>\n" +
                "  <symbolName type=\"BUILTIN\">builtins</symbolName>.<symbolName type=\"BUILTIN\">compareVersions</symbolName>\n" +
                "  compareVersions\n" +
                "]");
    }

    public void testLetExpression() {
        doTest("let\n" +
                "  inherit (some_value) \"no-highlighting-for-string-attributes\" <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>;\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">x</symbolName> = some_value;\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>.y = some_value;\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>.y.z = some_value;\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>.\"no-highlighting-for-string-attributes\" = some_value;\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">copy</symbolName> = <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>;\n" +
                "in [\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>.y\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>.y.z\n" +
                "]");
    }

    public void testLegacyLetExpression() {
        doTest("let {\n" +
                "  inherit (some_value) \"no-highlighting-for-string-attributes\" <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>;\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">x</symbolName> = some_value;\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>.y = some_value;\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>.y.z = some_value;\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>.\"no-highlighting-for-string-attributes\" = some_value;\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">body</symbolName> = [\n" +
                "    <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>\n" +
                "    <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>.y\n" +
                "    <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>.y.z\n" +
                "  ];\n" +
                "}");
    }

    public void testRecursiveSet() {
        doTest("rec {\n" +
                "  inherit (some_value) \"no-highlighting-for-string-attributes\" <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>;\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">x</symbolName> = some_value;\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>.y = some_value;\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>.y.z = some_value;\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>.\"no-highlighting-for-string-attributes\" = some_value;\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">body</symbolName> = [\n" +
                "    <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>\n" +
                "    <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>.y\n" +
                "    <symbolName type=\"LOCAL_VARIABLE\">x</symbolName>.y.z\n" +
                "  ];\n" +
                "}");
    }

    public void testNoHighlightingForNonRecursiveSet() {
        doTest("{\n" +
                "  inherit (some_value) \"no-highlighting-for-string-attributes\" x;\n" +
                "  x = some_value;\n" +
                "  x.y = some_value;\n" +
                "  x.\"no-highlighting-for-string-attributes\" = some_value;\n" +
                "}");
    }

    public void testLambda() {
        doTest("<symbolName type=\"PARAMETER\">x</symbolName>:\n" +
                "{<symbolName type=\"PARAMETER\">y</symbolName>}:\n" +
                "<symbolName type=\"PARAMETER\">z</symbolName>@{<symbolName type=\"PARAMETER\">za</symbolName>, ...}: [\n" +
                "  <symbolName type=\"PARAMETER\">x</symbolName>\n" +
                "  <symbolName type=\"PARAMETER\">y</symbolName>\n" +
                "  <symbolName type=\"PARAMETER\">z</symbolName>\n" +
                "  <symbolName type=\"PARAMETER\">za</symbolName>\n" +
                "]");
    }

    public void testVariableHidesParameter() {
        doTest("{<symbolName type=\"PARAMETER\">f</symbolName>, <symbolName type=\"PARAMETER\">hidden</symbolName>}: [\n" +
                "  <symbolName type=\"PARAMETER\">f</symbolName> <symbolName type=\"PARAMETER\">hidden</symbolName>\n" +
                "  (\n" +
                "    let <symbolName type=\"LOCAL_VARIABLE\">hidden</symbolName> = some_value;\n" +
                "    in <symbolName type=\"PARAMETER\">f</symbolName> <symbolName type=\"LOCAL_VARIABLE\">hidden</symbolName>\n" +
                "  )\n" +
                "  (let {\n" +
                "    <symbolName type=\"LOCAL_VARIABLE\">hidden</symbolName> = some_value;\n" +
                "    <symbolName type=\"LOCAL_VARIABLE\">body</symbolName> = <symbolName type=\"PARAMETER\">f</symbolName> <symbolName type=\"LOCAL_VARIABLE\">hidden</symbolName>;\n" +
                "  })\n" +
                "  (rec {\n" +
                "    <symbolName type=\"LOCAL_VARIABLE\">hidden</symbolName> = some_value;\n" +
                "    <symbolName type=\"LOCAL_VARIABLE\">body</symbolName> = <symbolName type=\"PARAMETER\">f</symbolName> <symbolName type=\"LOCAL_VARIABLE\">hidden</symbolName>;\n" +
                "  })\n" +
                "]");
    }

    public void testParameterHidesVariable() {
        doTest("let\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">f</symbolName> = some_value;\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">hidden</symbolName> = some_value;\n" +
                "in\n" +
                "  <symbolName type=\"PARAMETER\">hidden</symbolName>:\n" +
                "  <symbolName type=\"LOCAL_VARIABLE\">f</symbolName> <symbolName type=\"PARAMETER\">hidden</symbolName>");
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
