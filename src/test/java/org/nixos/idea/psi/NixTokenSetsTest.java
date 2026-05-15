package org.nixos.idea.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.nixos.idea._testutil.WithIdeaPlatform;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;

@WithIdeaPlatform.CodeInsight
final class NixTokenSetsTest {

    private final CodeInsightTestFixture myFixture;

    NixTokenSetsTest(CodeInsightTestFixture fixture) {
        myFixture = fixture;
    }

    @Nested
    @DisplayName("STRING_CONTENT contains all tokens inside NixStringText")
    final class StringContent {

        @Test
        void std_string() {
            doTest("""
                    "abc def"\
                    """);
        }

        @Test
        void std_string_with_escape_sequence() {
            doTest("""
                    "\\${}\\n"\
                    """);
        }

        @Test
        void single_line_ind_string() {
            doTest("""
                    ''abc def''\
                    """);
        }

        @Test
        void ind_string_with_escape_sequence() {
            doTest("""
                    ''''${}''\\n''\
                    """);
        }

        @Test
        void multiline_string() {
            doTest("""
                    ''
                        begin
                          ...

                        end
                      ''\
                    """);
        }

        private void doTest(String code) {
            ReadAction.run(() -> {
                NixString string = NixElementFactory.createString(myFixture.getProject(), code);
                for (NixStringPart stringPart : string.getStringParts()) {
                    assertStringContent(assertInstanceOf(NixStringText.class, stringPart));
                }
            });
        }

        private void assertStringContent(NixStringText code) {
            for (ASTNode current = code.getNode().getFirstChildNode(); current != null; current = current.getTreeNext()) {
                IElementType elementType = current.getElementType();
                if (!NixTokenSets.STRING_CONTENT.contains(elementType)) {
                    fail("Expected type in STRING_CONTENT, but found: " + elementType);
                }
            }
        }
    }
}
