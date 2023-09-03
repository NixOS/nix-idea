package org.nixos.idea.interpretation;

import com.intellij.openapi.project.Project;
import org.junit.jupiter.api.Test;
import org.nixos.idea._testutil.WithIdeaPlatform;
import org.nixos.idea.psi.NixAttr;
import org.nixos.idea.psi.NixElementFactory;
import org.nixos.idea.psi.NixExprSelect;
import org.nixos.idea.psi.NixVariableAccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@WithIdeaPlatform.OnEdt
final class VariableUsageTest {

    private final Project myProject;

    VariableUsageTest(Project project) {
        myProject = project;
    }

    @Test
    void testSimpleVariableAccess() {
        NixVariableAccess expr = NixElementFactory.createExpr(myProject, "a");
        VariableUsage usage = VariableUsage.by(expr);
        assertNotNull(usage);
        assertEquals("a", usage.element().getText());
        assertEquals("a", usage.path().toString());
        assertEquals(1, usage.attributeElements().length);
        assertEquals("a", usage.attributeElements()[0].getText());
    }

    @Test
    void testSelectExpression() {
        NixExprSelect expr = NixElementFactory.createExpr(myProject, "a.b.c");
        VariableUsage usage = VariableUsage.by(expr);
        assertNotNull(usage);
        assertEquals("a.b.c", usage.element().getText());
        assertEquals("a.b.c", usage.path().toString());
        assertEquals(3, usage.attributeElements().length);
        assertEquals("a", usage.attributeElements()[0].getText());
        assertEquals("b", usage.attributeElements()[1].getText());
        assertEquals("c", usage.attributeElements()[2].getText());
    }

    @Test
    void testInheritVariable() {
        NixAttr attr = NixElementFactory.createElement(myProject, NixAttr.class,
                "{inherit ", "a", ";}");
        VariableUsage usage = VariableUsage.by(attr);
        assertNotNull(usage);
        assertEquals("a", usage.element().getText());
        assertEquals("a", usage.path().toString());
        assertEquals(1, usage.attributeElements().length);
        assertEquals("a", usage.attributeElements()[0].getText());
    }

    @Test
    void testInheritFromExpression() {
        NixAttr attr = NixElementFactory.createElement(myProject, NixAttr.class,
                "{inherit (x) ", "a", ";}");
        assertNull(VariableUsage.by(attr));
    }

    @Test
    void testAttributeOutsideOfInherit() {
        NixAttr attr = NixElementFactory.createAttr(myProject, "a");
        assertNull(VariableUsage.by(attr));
    }
}
