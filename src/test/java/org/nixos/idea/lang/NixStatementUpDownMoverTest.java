package org.nixos.idea.lang;

import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.nixos.idea._testutil.TestFixtureUtil;
import org.nixos.idea._testutil.WithIdeaPlatform;
import org.nixos.idea.file.NixFileType;

@WithIdeaPlatform.OnEdt
@WithIdeaPlatform.CodeInsight
final class NixStatementUpDownMoverTest {

    private final CodeInsightTestFixture myFixture;

    NixStatementUpDownMoverTest(CodeInsightTestFixture fixture) {
        myFixture = fixture;
    }

    @Nested
    final class ListLikeElements {
        @Test
        void do_not_break_formulas() {
            // Moving the line with a comma down would cause a syntax error.
            // We may move the lines and fix the commas in the future.
            // For now, we just don't support moving lines in formulas.
            setup("""
                    {
                      with_comma<caret>,
                      without_comma
                    }:
                    _
                    """);
            doMoveDown();
            expect("""
                    {
                      with_comma<caret>,
                      without_comma
                    }:
                    _
                    """);
        }

        @Nested
        @DisplayName("support list-like elements")
        final class Simple {
            // Note: List-like elements are implemented by NixMoveElementLeftRightHandler.
            // We are just verifying that NixStatementUpDownMover handles these elements correctly with a small sample.

            @Test
            void list() {
                setup("""
                        [
                          a<caret>
                          b
                          c
                        ]
                        """);
                doMoveDown();
                expect("""
                        [
                          b
                          a<caret>
                          c
                        ]
                        """);
            }

            @Test
            void let() {
                setup("""
                        let
                          a = x;
                          b = y;<caret>
                        in _
                        """);
                doMoveUp();
                expect("""
                        let
                          b = y;<caret>
                          a = x;
                        in _
                        """);
            }

            @Test
            void bind_inherit() {
                setup("""
                        { inherit
                            a<caret>
                            b
                        ;}
                        """);
                doMoveDown();
                expect("""
                        { inherit
                            b
                            a<caret>
                        ;}
                        """);
            }
        }

        @Nested
        @DisplayName("move items on same line together")
        final class SameLine {
            @Test
            void simple_lines() {
                setup("""
                        [
                          a b<caret>
                          c d
                          e f
                        ]
                        """);
                doMoveDown();
                expect("""
                        [
                          c d
                          a b<caret>
                          e f
                        ]
                        """);
            }

            @Test
            void move_extended_line() {
                setup("""
                        [
                          a (
                            b
                          ) (
                            c<caret>
                          ) (
                            d
                          ) e
                          second_line
                        ]
                        """);
                doMoveDown();
                expect("""
                        [
                          second_line
                          a (
                            b
                          ) (
                            c<caret>
                          ) (
                            d
                          ) e
                        ]
                        """);
            }

            @Test
            void move_over_extended_line() {
                setup("""
                        [
                          a (
                            b
                          ) (
                            c
                          ) (
                            d
                          ) e
                          second_line<caret>
                        ]
                        """);
                doMoveUp();
                expect("""
                        [
                          second_line<caret>
                          a (
                            b
                          ) (
                            c
                          ) (
                            d
                          ) e
                        ]
                        """);
            }
        }

        @Nested
        @DisplayName("allow selecting multiple lines to move")
        final class Selection {
            @Test
            void simple_lines() {
                setup("""
                        [
                          <selection>line1
                          line2<caret></selection>
                          line3
                        ]
                        """);
                doMoveDown();
                expect("""
                        [
                          line3
                          <selection>line1
                          line2<caret></selection>
                        ]
                        """);
            }

            @Test
            void empty_selection_at_start_of_line() {
                setup("""
                        [
                          <selection>  <caret></selection>  (
                            line1)
                          line2
                        ]
                        """);
                doMoveDown();
                expect("""
                        [
                          line2
                          <selection>  <caret></selection>  (
                            line1)
                        ]
                        """);
            }

            @Test
            void empty_selection_at_end_of_line() {
                setup("""
                        [
                          (line1
                            )  <selection>  <caret></selection>
                          line2
                        ]
                        """);
                doMoveDown();
                expect("""
                        [
                          line2
                          (line1
                            )  <selection>  <caret></selection>
                        ]
                        """);
            }

            @Test
            void ignore_last_line_with_empty_selection() {
                setup("""
                        [
                          <selection>line1
                          line2
                        <caret></selection>  line3
                        ]
                        """);
                doMoveDown();
                expect("""
                        [
                          line3
                          <selection>line1
                          line2
                        <caret></selection>]
                        """);
            }

            @Test
            void expanded_lines() {
                setup("""
                        [
                          non_selected
                          a (
                            b
                          ) <selection>c
                          d<caret></selection> (
                            e
                          ) f
                        ]
                        """);
                doMoveUp();
                expect("""
                        [
                          a (
                            b
                          ) <selection>c
                          d<caret></selection> (
                            e
                          ) f
                          non_selected
                        ]
                        """);
            }
        }

        @Nested
        @DisplayName("fallback if range includes non-item elements")
        final class Fallback {
            @Test
            void empty_list() {
                setup("""
                        [
                          outer1
                          [<caret>]
                          outer2
                        ]
                        """);
                doMoveDown();
                expect("""
                        [
                          outer1
                          outer2
                          [<caret>]
                        ]
                        """);
            }

            @Test
            void opening_brackets_included() {
                setup("""
                        [
                          [ a<caret>
                            b ]
                          outer1
                          outer2
                        ]
                        """);
                doMoveDown();
                expect("""
                        [
                          outer1
                          [ a<caret>
                            b ]
                          outer2
                        ]
                        """);
            }

            @Test
            void closing_brackets_included() {
                setup("""
                        [
                          outer1
                          outer2
                          [ a
                            <caret>b ]
                        ]
                        """);
                doMoveUp();
                expect("""
                        [
                          outer1
                          [ a
                            <caret>b ]
                          outer2
                        ]
                        """);
            }

            @Test
            void opening_brackets_indirectly_included() {
                setup("""
                        [
                          [ (
                              a<caret>
                            )
                            b
                          ]
                          outer1
                          outer2
                        ]
                        """);
                doMoveDown();
                expect("""
                        [
                          outer1
                          [ (
                              a<caret>
                            )
                            b
                          ]
                          outer2
                        ]
                        """);
            }

            @Test
            void lambda_separator_sign() {
                setup("""
                        [
                          (
                            <selection><caret>x
                            @</selection>
                            {}
                            :
                            _
                          )
                          outer
                        ]
                        """);
                doMoveDown();
                expect("""
                        [
                          outer
                          (
                            <selection><caret>x
                            @</selection>
                            {}
                            :
                            _
                          )
                        ]
                        """);
            }
        }

        @Nested
        @DisplayName("no fallback on last position")
        final class NoFallback {
            @Test
            void single_element() {
                testNoFallback(true, true, """
                        [
                          prefix
                          [
                            x<caret>
                          ]
                          suffix
                        ]
                        """);
            }

            @Test
            void up() {
                testNoFallback(true, false, """
                        [
                          prefix
                          [
                            x<caret>
                            y
                            z
                          ]
                          suffix
                        ]
                        """);
            }

            @Test
            void down() {
                testNoFallback(false, true, """
                        [
                          prefix
                          [
                            x
                            y
                            z<caret>
                          ]
                          suffix
                        ]
                        """);
            }

            private void testNoFallback(boolean up, boolean down, @Language("HTML") String code) {
                setup(code);
                if (up) {
                    doMoveUp();
                    expect(code);
                }
                if (down) {
                    doMoveDown();
                    expect(code);
                }
            }
        }

        @Nested
        @DisplayName("support empty lines")
        final class EmptyLines {
            @Test
            void move_empty_line() {
                setup("""
                        [
                          prefix
                          [
                            <caret>
                          ]
                          suffix
                        ]
                        """);
                doMoveUp();
                expect("""
                        [
                          prefix
                            <caret>
                          [
                          ]
                          suffix
                        ]
                        """);
                doMoveDown();
                expect("""
                        [
                          prefix
                          [
                            <caret>
                          ]
                          suffix
                        ]
                        """);
            }

            @Test
            void move_down_over_empty_line() {
                setup("""
                        [
                          item1<caret>
                        \s
                          item2
                        ]
                        """);
                doMoveDown();
                expect("""
                        [
                        \s
                          item1<caret>
                          item2
                        ]
                        """);
            }

            @Test
            void move_up_over_empty_line() {
                setup("""
                        [
                          item1
                        \s
                          item2<caret>
                        ]
                        """);
                doMoveUp();
                expect("""
                        [
                          item1
                          item2<caret>
                        \s
                        ]
                        """);
            }

            @Test
            void move_down_with_empty_line() {
                setup("""
                        [
                        <selection>\s
                          item1<caret></selection>
                          item2
                        ]
                        """);
                doMoveDown();
                expect("""
                        [
                          item2
                        <selection>\s
                          item1<caret></selection>
                        ]
                        """);
            }

            @Test
            void move_up_with_empty_line() {
                setup("""
                        [
                          item1
                          <selection>item2
                        \s
                        <caret></selection>]
                        """);
                doMoveUp();
                expect("""
                        [
                          <selection>item2
                        \s
                        <caret></selection>  item1
                        ]
                        """);
            }

            @Test
            void move_over_empty_line_with_empty_line() {
                setup("""
                        [
                        <selection>\s
                          item
                        \s
                        <caret></selection>\s
                        ]
                        """);
                doMoveDown();
                expect("""
                        [
                        \s
                        <selection>\s
                          item
                        \s
                        <caret></selection>]
                        """);
                doMoveUp();
                expect("""
                        [
                        <selection>\s
                          item
                        \s
                        <caret></selection>\s
                        ]
                        """);
            }

            @Test
            void move_over_blank_line() {
                setup("""
                        [
                          item<caret>
                          \s
                        ]
                        """);
                doMoveDown();
                expect("""
                        [
                          \s
                          item<caret>
                        ]
                        """);
            }
        }

        @Nested
        @DisplayName("allow comments")
        final class Comments {
            @Test
            void move_single_line_comment() {
                setup("""
                        [
                          # Comment<caret>
                          item
                        ]
                        """);
                doMoveDown();
                expect("""
                        [
                          item
                          # Comment<caret>
                        ]
                        """);
            }

            @Test
            void move_multi_line_comment() {
                setup("""
                        [
                          /*
                           * Comment<caret>
                           */
                          item
                        ]
                        """);
                doMoveDown();
                expect("""
                        [
                          item
                          /*
                           * Comment<caret>
                           */
                        ]
                        """);
            }

            @Test
            void move_item_below_single_line_comment() {
                setup("""
                        [
                          # Comment
                          item1<caret>
                          item2
                        ]
                        """);
                doMoveDown();
                expect("""
                        [
                          # Comment
                          item2
                          item1<caret>
                        ]
                        """);
            }

            @Test
            void move_over_multi_line_comment() {
                setup("""
                        [
                          /*
                           * Comment
                           */
                          item<caret>
                        ]
                        """);
                doMoveUp();
                expect("""
                        [
                          item<caret>
                          /*
                           * Comment
                           */
                        ]
                        """);
            }
        }
    }

    private void setup(@Language("HTML") String code) {
        myFixture.configureByText(NixFileType.INSTANCE, code);
    }

    private void doMoveDown() {
        myFixture.performEditorAction(IdeActions.ACTION_MOVE_STATEMENT_DOWN_ACTION);
    }

    private void doMoveUp() {
        myFixture.performEditorAction(IdeActions.ACTION_MOVE_STATEMENT_UP_ACTION);
    }

    private void expect(@Language("HTML") String code) {
        TestFixtureUtil.expect(myFixture, code);
    }
}
