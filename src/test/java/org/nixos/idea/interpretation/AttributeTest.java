package org.nixos.idea.interpretation;

import com.intellij.openapi.project.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.nixos.idea._testutil.WithIdeaPlatform;
import org.nixos.idea.psi.NixAttr;
import org.nixos.idea.psi.NixElementFactory;
import org.nixos.idea.psi.NixIdentifier;
import org.nixos.idea.util.TriState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WithIdeaPlatform.OnEdt
final class AttributeTest {

    private final Project myProject;

    AttributeTest(Project project) {
        myProject = project;
    }

    @Test
    void testIsIdentifierForIdentifier() {
        NixIdentifier identifier1 = NixElementFactory.createParamName(myProject, "x");
        NixIdentifier identifier2 = NixElementFactory.createVariableAccess(myProject, "abc");
        assertTrue(Attribute.of(identifier1).isIdentifier());
        assertTrue(Attribute.of(identifier2).isIdentifier());
    }

    @ParameterizedTest(name = "[{index}] {0} -> {1}")
    @CsvSource({
            "abc, true",
            "\"abc\", false",
            "${\"abc\"}, false",
            "${''abc''}, false",
            "${x}, false",
    })
    void testIsIdentifierForAttr(String code, boolean expectedValue) {
        NixAttr element = NixElementFactory.createAttr(myProject, code);
        assertEquals(expectedValue, Attribute.of(element).isIdentifier());
    }

    @Test
    void testHasQuotesForIdentifier() {
        NixIdentifier identifier1 = NixElementFactory.createParamName(myProject, "x");
        NixIdentifier identifier2 = NixElementFactory.createVariableAccess(myProject, "abc");
        assertFalse(Attribute.of(identifier1).hasQuotes());
        assertFalse(Attribute.of(identifier2).hasQuotes());
    }

    @ParameterizedTest(name = "[{index}] {0} -> {1}")
    @CsvSource({
            "xyz, false",
            "\"\", true",
            "\"42\", true",
            "\"a\", true",
            "${\"a\"}, true",
            "${''a''}, true",
            "${\"a\" + \"b\"}, false",
    })
    void testHasQuotesForAttr(String code, boolean expectedValue) {
        NixAttr element = NixElementFactory.createAttr(myProject, code);
        assertEquals(expectedValue, Attribute.of(element).hasQuotes());
    }

    @Test
    void testIsDynamicForIdentifier() {
        NixIdentifier identifier1 = NixElementFactory.createParamName(myProject, "x");
        NixIdentifier identifier2 = NixElementFactory.createVariableAccess(myProject, "abc");
        assertFalse(Attribute.of(identifier1).isDynamic());
        assertFalse(Attribute.of(identifier2).isDynamic());
    }

    @ParameterizedTest(name = "[{index}] {0} -> {1}")
    @CsvSource({
            "abc, false",
            "\"abc\", false",
            "${abc}, true",
            "${''abc''}, false",
            "${\"abc\"}, false",
            "\"${\"abc\"}\", true",
            "${\"a${\"b\"}c\"}, true",
            "${\"ab\" + \"c\"}, true",
    })
    void testIsDynamicForAttr(String code, boolean expectedValue) {
        NixAttr element = NixElementFactory.createAttr(myProject, code);
        assertEquals(expectedValue, Attribute.of(element).isDynamic());
    }

    @Test
    void testGetNameForIdentifier() {
        NixIdentifier identifier1 = NixElementFactory.createParamName(myProject, "x");
        NixIdentifier identifier2 = NixElementFactory.createVariableAccess(myProject, "abc");
        assertEquals("x", Attribute.of(identifier1).getName());
        assertEquals("abc", Attribute.of(identifier2).getName());
    }

    @ParameterizedTest(name = "[{index}] {0} -> {1}")
    @CsvSource(nullValues = "null", value = {
            "abc, abc",
            "\"abc\", abc",
            "${abc}, null",
            "${''abc''}, abc",
            "${\"abc\"}, abc",
            "\"${\"abc\"}\", ",
            "${\"a${\"b\"}c\"}, null",
            "${\"ab\" + \"c\"}, null",
    })
    void testGetNameForAttr(String code, String expectedValue) {
        NixAttr element = NixElementFactory.createAttr(myProject, code);
        assertEquals(expectedValue, Attribute.of(element).getName());
    }

    @ParameterizedTest(name = "[{index}] {0}, {1} -> {2}")
    @CsvSource({
            "a, a, TRUE",
            "a, b, FALSE",
            "a, ${x}, MAYBE",
            "${x}, a, MAYBE",
            "${x}, ${x}, MAYBE",
            "ab, \"ab${x}\", MAYBE",
            "ba, \"${x}ba\", MAYBE",
            "a, \"a${x}a\", FALSE",
            "aa, \"a${x}a\", MAYBE",
            "\"ab${x}\", ab, MAYBE",
            "\"${x}ba\", ba, MAYBE",
            "\"a${x}a\", a, FALSE",
            "\"a${x}a\", aa, MAYBE",
            "\"a${x}\", \"a${x}\", MAYBE",
            "\"${x}a\", \"${x}a\", MAYBE",
            "\"a${x}b\", \"a${x}b\", MAYBE",
            "\"ab${x}\", \"a${x}\", MAYBE",
            "\"a${x}\", \"ab${x}\", MAYBE",
            "\"${x}ba\", \"${x}a\", MAYBE",
            "\"${x}a\", \"${x}ba\", MAYBE",
            "\"a${x}\", \"b${x}\", FALSE",
            "\"${x}a\", \"${x}b\", FALSE",
            "\"${x}ab\", \"${x}a\", FALSE",
            "\"${x}a\", \"${x}ab\", FALSE",
            "\"${x}${y}\", ${x}, MAYBE",
            "${x}, \"${x}${y}\", MAYBE",
            "\"$a{x}b\", \"${x}${y}\", MAYBE",
            "\"${x}\", \"a${x}b${y}c\", MAYBE",
            "\"a\", \"\\a\", TRUE",
            "\"\\a\", \"a\", TRUE",
            "${''\\''}, \"\\\\\", TRUE",
            "\"\\\\\", ${''\\''}, TRUE",
    })
    void testMatches(String attrCode1, String attrCode2, TriState expectedResult) {
        Attribute attr1 = Attribute.of(NixElementFactory.createAttr(myProject, attrCode1));
        Attribute attr2 = Attribute.of(NixElementFactory.createAttr(myProject, attrCode2));
        assertEquals(expectedResult, attr1.matches(attr2));
    }

    @ParameterizedTest(name = "[{index}] {0} -> {1}")
    @CsvSource({
            "a, a",
            "\"a\", \"a\"",
            "\"$\\{a}\", \"$\\{a}\"",
            "\"\\${a}\", \"\\${a}\"",
            "${\"a\"}, ${\"a\"}",
            "${''a\\$''}, ${''a\\$''}",
            "${x}, *",
            "${''${x}''}, ${''*''}",
            "\"a${x}\", \"a*\"",
            "\"${x}a\", \"*a\"",
            "\"a${x}b\", \"a*b\"",
            "\"${x}_${y}\", \"*_*\"",
            "\"${x}_${y}\", \"*_*\"",
    })
    void testToString(String code, String expectedResult) {
        Attribute attr = Attribute.of(NixElementFactory.createAttr(myProject, code));
        assertEquals(expectedResult, attr.toString());
    }
}
