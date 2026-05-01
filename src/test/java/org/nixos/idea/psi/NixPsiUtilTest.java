package org.nixos.idea.psi;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.nixos.idea._testutil.Markers;
import org.nixos.idea._testutil.WithIdeaPlatform;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WithIdeaPlatform.OnEdt
final class NixPsiUtilTest {

    private final Project myProject;

    NixPsiUtilTest(Project project) {
        myProject = project;
    }

    @ParameterizedTest(name = "[{index}] {0} => {1}")
    @CsvSource(delimiterString = "=>", textBlock = """
            <attr>x</attr>             => false
            <attr>x</attr>.y           => false
            x.<attr>y</attr>           => false
            { inherit <attr>x</attr> } => false
            { <attr>x</attr> = _ }     => true
            { <attr>x</attr>.y = _ }   => true
            { x.<attr>y</attr> = _ }   => true
            let { <attr>x</attr> = _ } => true
            rec { <attr>x</attr> = _ } => true
            <attr>x</attr>: _          => true
            { <attr>x</attr>, y }: _   => true
            <attr>x</attr> @ { y }: _  => true
            x @ { <attr>y</attr> }: _  => true
            """)
    void isDeclaration(String code, boolean expectedResult) {
        Markers markedCode = Markers.parse(code, Markers.tagName("attr"));
        TextRange attr = markedCode.single().range();
        NixIdentifier element = NixElementFactory.createElement(
                myProject,
                NixIdentifier.class,
                markedCode.unmarkedText().substring(0, attr.getStartOffset()),
                attr.substring(markedCode.unmarkedText()),
                markedCode.unmarkedText().substring(attr.getEndOffset()));
        assertEquals(expectedResult, NixPsiUtil.isDeclaration(element));
    }
}
