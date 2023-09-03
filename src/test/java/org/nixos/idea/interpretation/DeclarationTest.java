package org.nixos.idea.interpretation;

import com.intellij.openapi.project.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.nixos.idea._testutil.WithIdeaPlatform;
import org.nixos.idea.psi.NixElementFactory;
import org.nixos.idea.psi.NixExprLambda;
import org.nixos.idea.psi.NixExprLet;
import org.nixos.idea.psi.NixLegacyLet;
import org.nixos.idea.psi.NixPsiElement;
import org.nixos.idea.psi.NixSet;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@WithIdeaPlatform.OnEdt
final class DeclarationTest {

    private final Project myProject;

    DeclarationTest(Project project) {
        myProject = project;
    }

    @Test
    void testEmptyLet() {
        NixExprLet expr = NixElementFactory.createExpr(myProject, "let in z");
        zeroDeclarations(expr);
    }

    @Test
    void testSimpleAssignment() {
        NixExprLet expr = NixElementFactory.createExpr(myProject, "let a = x; in z");
        Declaration declaration = singleDeclaration(expr);
        assertEquals("a = x;", declaration.element().getText());
        assertSame(expr, declaration.scope());
        assertEquals("a", declaration.path().toString());
        assertEquals(1, declaration.attributeElements().length);
        assertEquals("a", declaration.attributeElements()[0].getText());
    }

    @Test
    void testAssignmentToAttributePath() {
        NixExprLet expr = NixElementFactory.createExpr(myProject, "let a.bc.d = x; in z");
        Declaration declaration = singleDeclaration(expr);
        assertEquals("a.bc.d = x;", declaration.element().getText());
        assertSame(expr, declaration.scope());
        assertEquals("a.bc.d", declaration.path().toString());
        assertEquals(3, declaration.attributeElements().length);
        assertEquals("a", declaration.attributeElements()[0].getText());
        assertEquals("bc", declaration.attributeElements()[1].getText());
        assertEquals("d", declaration.attributeElements()[2].getText());
    }

    @Test
    void testMultipleAssignments() {
        NixExprLet expr = NixElementFactory.createExpr(myProject, "let a = x; b.c = y; d = z; in z");
        Collection<Declaration> declarations = Declaration.allOf(expr);
        assertNotNull(declarations);
        assertEquals(3, declarations.size(), "size");
        Declaration[] array = declarations.toArray(Declaration[]::new);

        assertEquals("a = x;", array[0].element().getText());
        assertSame(expr, array[0].scope());
        assertEquals("a", array[0].path().toString());
        assertEquals(1, array[0].attributeElements().length);
        assertEquals("a", array[0].attributeElements()[0].getText());

        assertEquals("b.c = y;", array[1].element().getText());
        assertSame(expr, array[1].scope());
        assertEquals("b.c", array[1].path().toString());
        assertEquals(2, array[1].attributeElements().length);
        assertEquals("b", array[1].attributeElements()[0].getText());
        assertEquals("c", array[1].attributeElements()[1].getText());

        assertEquals("d = z;", array[2].element().getText());
        assertSame(expr, array[2].scope());
        assertEquals("d", array[2].path().toString());
        assertEquals(1, array[2].attributeElements().length);
        assertEquals("d", array[2].attributeElements()[0].getText());
    }

    @Test
    void testSimpleInherit() {
        NixExprLet expr = NixElementFactory.createExpr(myProject, "let inherit (x) a; in z");
        Declaration declaration = singleDeclaration(expr);
        assertEquals("a", declaration.element().getText());
        assertSame(expr, declaration.scope());
        assertEquals("a", declaration.path().toString());
        assertEquals(1, declaration.attributeElements().length);
        assertEquals("a", declaration.attributeElements()[0].getText());
    }

    @Test
    void testMultipleInherits() {
        NixExprLet expr = NixElementFactory.createExpr(myProject, "let inherit (x) a; inherit (y) b c; in z");
        Collection<Declaration> declarations = Declaration.allOf(expr);
        assertNotNull(declarations);
        assertEquals(3, declarations.size(), "size");
        Declaration[] array = declarations.toArray(Declaration[]::new);

        assertEquals("a", array[0].element().getText());
        assertSame(expr, array[0].scope());
        assertEquals("a", array[0].path().toString());
        assertEquals(1, array[0].attributeElements().length);
        assertEquals("a", array[0].attributeElements()[0].getText());

        assertEquals("b", array[1].element().getText());
        assertSame(expr, array[1].scope());
        assertEquals("b", array[1].path().toString());
        assertEquals(1, array[1].attributeElements().length);
        assertEquals("b", array[1].attributeElements()[0].getText());

        assertEquals("c", array[2].element().getText());
        assertSame(expr, array[2].scope());
        assertEquals("c", array[2].path().toString());
        assertEquals(1, array[2].attributeElements().length);
        assertEquals("c", array[2].attributeElements()[0].getText());
    }

    @Test
    void testInheritVariable() {
        NixExprLet expr = NixElementFactory.createExpr(myProject, "let inherit a; in z");
        zeroDeclarations(expr);
    }

    @Test
    void testRecursiveSet() {
        NixSet expr = NixElementFactory.createExpr(myProject, "rec { a = x; }");
        Declaration declaration = singleDeclaration(expr);
        assertEquals("a = x;", declaration.element().getText());
    }

    @Test
    void testNonRecursiveSet() {
        NixSet expr = NixElementFactory.createExpr(myProject, "{ a = x; }");
        assertNull(Declaration.allOf(expr));
    }

    @Test
    void testLegacyLet() {
        NixLegacyLet expr = NixElementFactory.createExpr(myProject, "let { a = x; body = z; }");
        Collection<Declaration> declarations = Declaration.allOf(expr);
        assertNotNull(declarations, "declarations");
        assertEquals(2, declarations.size(), "size");
        Declaration[] array = declarations.toArray(Declaration[]::new);
        assertEquals("a = x;", array[0].element().getText());
        assertEquals("body = z;", array[1].element().getText());
    }

    @Test
    void testFunctionWithIdentifierPattern() {
        NixExprLambda expr = NixElementFactory.createExpr(myProject, "a: x");
        Declaration declaration = singleDeclaration(expr);
        assertEquals("a", declaration.element().getText());
        assertSame(expr, declaration.scope());
        assertEquals("a", declaration.path().toString());
        assertEquals(1, declaration.attributeElements().length);
        assertEquals("a", declaration.attributeElements()[0].getText());
    }

    @Test
    void testFunctionWithSetPattern() {
        NixExprLambda expr = NixElementFactory.createExpr(myProject, "{a, b ? y}: x");
        Collection<Declaration> declarations = Declaration.allOf(expr);
        assertNotNull(declarations);
        assertEquals(2, declarations.size(), "size");
        Declaration[] array = declarations.toArray(Declaration[]::new);

        assertEquals("a", array[0].element().getText());
        assertSame(expr, array[0].scope());
        assertEquals("a", array[0].path().toString());
        assertEquals(1, array[0].attributeElements().length);
        assertEquals("a", array[0].attributeElements()[0].getText());

        assertEquals("b ? y", array[1].element().getText());
        assertSame(expr, array[1].scope());
        assertEquals("b", array[1].path().toString());
        assertEquals(1, array[1].attributeElements().length);
        assertEquals("b", array[1].attributeElements()[0].getText());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a@{b}: x", "{b}@a: x"})
    void testFunctionWithAtPattern(String code) {
        NixExprLambda expr = NixElementFactory.createExpr(myProject, code);
        Collection<Declaration> declarations = Declaration.allOf(expr);
        assertNotNull(declarations);
        assertEquals(2, declarations.size(), "size");
        Declaration[] array = declarations.toArray(Declaration[]::new);

        assertEquals("a", array[0].element().getText());
        assertSame(expr, array[0].scope());
        assertEquals("a", array[0].path().toString());
        assertEquals(1, array[0].attributeElements().length);
        assertEquals("a", array[0].attributeElements()[0].getText());

        assertEquals("b", array[1].element().getText());
        assertSame(expr, array[1].scope());
        assertEquals("b", array[1].path().toString());
        assertEquals(1, array[1].attributeElements().length);
        assertEquals("b", array[1].attributeElements()[0].getText());
    }

    private void zeroDeclarations(NixPsiElement element) {
        Collection<Declaration> declarations = Declaration.allOf(element);
        assertNotNull(declarations, "declarations");
        assertEquals(0, declarations.size(), "size");
    }

    private Declaration singleDeclaration(NixPsiElement element) {
        Collection<Declaration> declarations = Declaration.allOf(element);
        assertNotNull(declarations, "declarations");
        assertEquals(1, declarations.size(), "size");
        return declarations.iterator().next();
    }
}
