package org.nixos.idea.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.nixos.idea._testutil.WithIdeaPlatform;
import org.nixos.idea.interpretation.AttributeMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@WithIdeaPlatform.OnEdt
final class NixDeclarationElementTest {

    private final Project myProject;

    NixDeclarationElementTest(Project project) {
        myProject = project;
    }

    @Test
    void testEmptyLet() {
        test("let in z", true);
    }

    @Test
    void testSimpleAssignment() {
        test("let a = x; in z", true,
                new Pattern("a = x;", "a")
                        .attribute("a")
        );
    }

    @Test
    void testAssignmentToAttributePath() {
        test("let a.bc.d = x; in z", true,
                new Pattern("a.bc.d = x;", "a.bc.d")
                        .attribute("a")
                        .attribute("bc")
                        .attribute("d")
        );
    }

    @Test
    void testMultipleAssignments() {
        test("let a = x; b.c = y; d = z; in z", true,
                new Pattern("a = x;", "a")
                        .attribute("a"),
                new Pattern("b.c = y;", "b.c")
                        .attribute("b")
                        .attribute("c"),
                new Pattern("d = z;", "d")
                        .attribute("d")
        );
    }

    @Test
    void testSimpleInherit() {
        test("let inherit (x) a; in z", true,
                new Pattern("a", "a")
                        .attribute("a")
        );
    }

    @Test
    void testMultipleInherits() {
        test("let inherit (x) a; inherit (y) b c; in z", true,
                new Pattern("a", "a")
                        .attribute("a"),
                new Pattern("b", "b")
                        .attribute("b"),
                new Pattern("c", "c")
                        .attribute("c")
        );
    }

    @Test
    void testInheritVariable() {
        test("let inherit a; in z", true);
    }

    @Test
    void testRecursiveSet() {
        test("rec { a = x; }", true,
                new Pattern("a = x;", "a")
        );
    }

    @Test
    void testNonRecursiveSet() {
        test("{ a = x; }", false,
                new Pattern("a = x;", "a")
                        .attribute("a")
        );
    }

    @Test
    void testLegacyLet() {
        test("let { a = x; body = z; }", true,
                new Pattern("a = x;", "a")
                        .attribute("a"),
                new Pattern("body = z;", "body")
                        .attribute("body")
        );
    }

    @Test
    void testFunctionWithIdentifierPattern() {
        test("a: x", true,
                new Pattern("a", "a")
                        .attribute("a")
        );
    }

    @Test
    void testFunctionWithSetPattern() {
        test("{a, b ? y}: x", true,
                new Pattern("a", "a")
                        .attribute("a"),
                new Pattern("b ? y", "b")
                        .attribute("b")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"a@{b}: x", "{b}@a: x"})
    void testFunctionWithAtPattern(String code) {
        test(code, true,
                new Pattern("a", "a")
                        .attribute("a"),
                new Pattern("b", "b")
                        .attribute("b")
        );
    }

    private void test(String code, boolean expandingScope, Pattern... declarationPatterns) {
        NixDeclarationHost host = NixElementFactory.createExpr(myProject, code);
        assertEquals(expandingScope, host.isExpandingScope(), "isExpandingScope()");

        AttributeMap<NixDeclarationElement> declarations = host.getDeclarations();
        assertEquals(declarationPatterns.length, declarations.size(), "getDeclarations().size()");

        List<NixDeclarationElement> sortedDeclarations = declarations.streamValues()
                .sorted(Comparator.comparingInt(PsiElement::getTextOffset))
                .toList();
        for (int i = 0; i < declarationPatterns.length; i++) {
            Pattern pattern = declarationPatterns[i];
            NixDeclarationElement declaration = sortedDeclarations.get(i);
            String pos = "getDeclarations()[%d]".formatted(i);

            assertEquals(pattern.myText, declaration.getText(), pos + ".getText()");
            assertSame(host, declaration.getDeclarationHost(), pos + ".getDeclarationHost()");
            assertEquals(pattern.myPath, declaration.getAttributePath().toString(), pos + ".getAttributePath()");
            assertEquals(pattern.myAttributes.size(), declaration.getAttributeElements().length, pos + ".getAttributeElements().length");
            for (int j = 0; j < pattern.myAttributes.size(); j++) {
                assertEquals(pattern.myAttributes.get(j), declaration.getAttributeElements()[j].getText(), pos + ".getAttributeElements()[%d]".formatted(j));
            }
        }
    }

    private static final class Pattern {
        private final String myText;
        private final String myPath;
        private final List<String> myAttributes = new ArrayList<>();

        private Pattern(String text, String path) {
            this.myText = text;
            this.myPath = path;
        }

        private Pattern attribute(String attribute) {
            myAttributes.add(attribute);
            return this;
        }
    }
}
