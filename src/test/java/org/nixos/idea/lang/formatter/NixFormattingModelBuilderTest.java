package org.nixos.idea.lang.formatter;

import com.intellij.application.options.CodeStyle;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.nixos.idea._testutil.WithIdeaPlatform;
import org.nixos.idea.file.NixFileType;
import org.nixos.idea.lang.NixLanguage;
import org.nixos.idea.settings.NixCodeStyleSettings;

@WithIdeaPlatform.CodeInsight
final class NixFormattingModelBuilderTest {

    private final CodeInsightTestFixture myFixture;

    NixFormattingModelBuilderTest(CodeInsightTestFixture fixture) {
        myFixture = fixture;
    }

    @Nested
    final class WhenTypingEnter {
        @Test
        void braces() {
            testEnterNewLine("{<caret>}", """
                {
                  <caret>
                }
                """);
        }

        @Test
        void braces_lambda() {
            testEnterNewLine("{<caret>}: _", """
                {
                  <caret>
                }: _
                """);
        }

        @Test
        void brackets() {
            testEnterNewLine("[<caret>]", """
                [
                  <caret>
                ]
                """);
        }

        @Test
        void parens() {
            testEnterNewLine("(<caret>)", """
                (
                  <caret>
                )
                """);
        }

        @Test
        void ind_string() {
            testEnterNewLine("''<caret>''", """
                ''
                  <caret>
                ''
                """);
        }

        private void testEnterNewLine(@Language("HTML") String initial, @Language("HTML") String expectedResult) {
            // First align trailing line feeds of both strings.
            // Using myFixture.checkResult(expectedResult, /*stripTrailingSpaces=*/ true); doesn't work for some reason.
            if (expectedResult.endsWith("\n") && !initial.endsWith("\n")) {
                initial += "\n";
            }
            myFixture.configureByText(NixFileType.INSTANCE, initial);
            myFixture.type('\n');
            myFixture.checkResult(expectedResult);
        }
    }

    @Nested
    final class Attrs {
        @Test
        void indent() {
            testIndent("""
                {
                  x = y;
                  inherit z;
                }
                """);
        }

        @Test
        void nested_indent() {
            testIndent("""
                {
                  x = {
                    inherit y;
                    z = "";
                  };
                }
                """);
        }

        @Nested
        final class Alignment {
            @Test
            void consecutive() {
                nixSettings().ALIGN_ASSIGNMENTS = NixCodeStyleSettings.AttributeAlignment.ALIGN_CONSECUTIVE;
                testAlignment("""
                    {
                      x.a  = 1;
                      x.bb = 2;

                      y.ccc = 3;
                      z.d   = 4;
                    }
                    """);
            }

            @Test
            void siblings() {
                nixSettings().ALIGN_ASSIGNMENTS = NixCodeStyleSettings.AttributeAlignment.ALIGN_SIBLINGS;
                testAlignment("""
                    {
                      x.aa  = 1;
                      x.bbb = 2;

                      y.c   = 3;

                      z = {
                        dd = 4;
                        e  = 5;
                      };
                    }
                    """);
            }

            @Test
            void nested() {
                nixSettings().ALIGN_ASSIGNMENTS = NixCodeStyleSettings.AttributeAlignment.ALIGN_NESTED;
                testAlignment("""
                    {
                      x = {
                        a   = 1;
                      };
                      y = {
                        bbb = 2;
                      };
                      z.cc  = 3;
                    }
                    """);
            }

            @Test
            void nested_inline() {
                nixSettings().ALIGN_ASSIGNMENTS = NixCodeStyleSettings.AttributeAlignment.ALIGN_NESTED;
                testAlignment("""
                    {
                      x.aaa = 1;
                      y     = { b = 2; };
                    }
                    """);
            }
        }
    }

    @Nested
    final class If {
        @Test
        void one_line() {
            testIndent("""
                if condition then case_1 else case_2
                """);
        }

        @Test
        void special_case() {
            testIndent("""
                if condition then case else
                rest
                """);
        }

        @Test
        void special_case_2() {
            testIndent("""
                if condition_1 then case_1 else
                if condition_2 then case_2 else
                rest
                """);
        }

        @Test
        void else_if() {
            testIndent("""
                if condition_1 then
                  case_1
                else if condition_2 then
                  case_2
                else if condition_3 then
                  case_3
                else
                  case_4
                """);
        }

        @Test
        void else_if_short() {
            testIndent("""
                if condition_1 then case_1
                else if condition_2 then case_2
                else if condition_3 then case_3
                else case_4
                """);
        }

        @Test
        void wrap_all_if_condition_is_on_separate_line() {
            setup("""
                if
                  condition
                then case_1
                else case_2
                """);
            doFormat();
            expect("""
                if
                  condition
                then
                  case_1
                else
                  case_2
                """);
        }

        @Test
        void wrap_cases_if_one_case_is_on_separate_line() {
            setup("""
                if condition_1 then
                  case_1
                else if condition_2 then case_2
                else case_3
                """);
            doFormat();
            expect("""
                if condition then
                  case_1
                else if condition_2 then
                  case_2
                else
                  case_2
                """);
        }
    }

    @Nested
    final class Minus {
        @Test
        void unaryMinus() {
            commonSettings().SPACE_AROUND_ADDITIVE_OPERATORS = true;
            commonSettings().SPACE_AROUND_UNARY_OPERATOR = false;
            setup("- x");
            doFormat();
            expect("-x");
        }

        @Test
        void binaryMinus() {
            commonSettings().SPACE_AROUND_ADDITIVE_OPERATORS = true;
            setup("x  -y");
            doFormat();
            expect("x - y");
        }

        @Test
        void doNotJoinIdentifiers() {
            commonSettings().SPACE_AROUND_ADDITIVE_OPERATORS = false;
            setup("x - y");
            doFormat();
            expect("x -y");
        }
    }

    private void testAlignment(@Language("Nix") String code) {
        setup(code.replaceAll("[ ]+", "   "));
        doFormat();
        expect(code);
    }

    private void testIndent(@Language("Nix") String code) {
        setup(code.replaceAll("^\\s+", ""));
        doFormat();
        expect(code);
    }

    private void setup(@Language("Nix") String code) {
        myFixture.configureByText(NixFileType.INSTANCE, code);
    }

    private void doFormat() {
        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_REFORMAT);
    }

    private void expect(@Language("Nix") String code) {
        Assertions.assertEquals(code, myFixture.getEditor().getDocument().getText());
    }

    private CommonCodeStyleSettings commonSettings() {
        return CodeStyle.getSettings(myFixture.getProject()).getCommonSettings(NixLanguage.INSTANCE);
    }

    private NixCodeStyleSettings nixSettings() {
        return CodeStyle.getSettings(myFixture.getProject()).getCustomSettings(NixCodeStyleSettings.class);
    }
}
