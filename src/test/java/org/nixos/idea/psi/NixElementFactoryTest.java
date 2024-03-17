package org.nixos.idea.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.tree.IElementType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.nixos.idea._testutil.WithIdeaPlatform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@WithIdeaPlatform.OnEdt
final class NixElementFactoryTest {

    private final Project myProject;

    NixElementFactoryTest(Project project) {
        myProject = project;
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"\"", "''''", "\"x\"", "''abc''", "\"x${y}z\"", "''${\"42\"}''"})
    void createString(String code) {
        NixString result = NixElementFactory.createString(myProject, code);
        assertEquals(code, result.getText());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "''", "\"", "\"x\" \"y\"", "''abc'' suffix", "${\"x\"}", "''${\"${❌}\"}''"})
    void createStringFail(String code) {
        assertThrows(RuntimeException.class,
                () -> NixElementFactory.createString(myProject, code));
    }

    @ParameterizedTest
    @ValueSource(strings = {"x", "\"x\"", "${x}", "${\"x\"}", "\"x${y}z\""})
    void createAttr(String code) {
        NixAttr result = NixElementFactory.createAttr(myProject, code);
        assertEquals(code, result.getText());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "x.y", "x y"})
    void createAttrFail(String code) {
        assertThrows(RuntimeException.class,
                () -> NixElementFactory.createAttr(myProject, code));
    }

    @ParameterizedTest
    @ValueSource(strings = {"x", "x.y", "x . y", "x.\"y\".${z}"})
    void createAttrPath(String code) {
        NixAttrPath result = NixElementFactory.createAttrPath(myProject, code);
        assertEquals(code, result.getText());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", ".", ".x", "x.", "x y"})
    void createAttrPathFail(String code) {
        assertThrows(RuntimeException.class,
                () -> NixElementFactory.createAttrPath(myProject, code));
    }

    @ParameterizedTest
    @ValueSource(strings = {"a = x;", "a.b = x;", "inherit a;", "inherit (x) a;"})
    void createBind(String code) {
        NixBind result = NixElementFactory.createBind(myProject, code);
        assertEquals(code, result.getText());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "a = x"})
    void createBindFail(String code) {
        assertThrows(RuntimeException.class,
                () -> NixElementFactory.createBind(myProject, code));
    }

    @ParameterizedTest
    @CsvSource({
            "IND_STRING, ''abc''",
            "IDENTIFIER, x",
            "EXPR_SELECT, x.y",
            "LIST, []",
            "LIST, [x y z]",
            "SET, {}",
            "SET, {x = y; y = z;}",
            "EXPR_IF, if x then y else z",
            "EXPR_LAMBDA, x: x",
            "EXPR_LET, let x = y; y = z; in x",
            "LEGACY_LET, let { x = y; body = x; }",
            "EXPR_OP_PLUS, 2 + 2",
            "EXPR_WITH, with x; y",
    })
    void createExpr(String typeName, String code) throws Exception {
        NixExpr result = NixElementFactory.createExpr(myProject, code);
        IElementType type = (IElementType) NixTypes.class.getField(typeName).get(null);
        assertEquals(code, result.getText());
        assertEquals(type, result.getNode().getElementType());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "x = y;"})
    void createExprFail(String code) {
        assertThrows(RuntimeException.class,
                () -> NixElementFactory.createExpr(myProject, code));
    }
}
