package org.nixos.idea.lang;

import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.nixos.idea._testutil.TestFixtureUtil;
import org.nixos.idea._testutil.WithIdeaPlatform;
import org.nixos.idea.file.NixFileType;

import java.util.List;

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
                        <selection><caret>x
                        @</selection>
                        {}
                        :
                        assert assertion;
                        _
                        """);
                doMoveDown();
                expect("""
                        assert assertion;
                        <selection><caret>x
                        @</selection>
                        {}
                        :
                        _
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

            @Test
            void expansion() {
                setup("""
                        [
                          a /*
                          */ /*
                          */ b<caret> /*
                          */ /*
                          */ c
                          not_selected
                        ]
                        """);
                doMoveDown();
                expect("""
                        [
                          not_selected
                          a /*
                          */ /*
                          */ b<caret> /*
                          */ /*
                          */ c
                        ]
                        """);
                doMoveUp();
                expect("""
                        [
                          a /*
                          */ /*
                          */ b<caret> /*
                          */ /*
                          */ c
                          not_selected
                        ]
                        """);
            }
        }
    }

    @Nested
    final class StatementLikeElements {
        @Nested
        @DisplayName("support statement-like expressions")
        final class SimpleStatements {
            @ParameterizedTest
            @MethodSource("statements")
            void up(String statement) {
                String stmt1 = statement.replace("<placeholder>", "first");
                String stmt2 = statement.replace("<placeholder>", "second");
                String stmt3 = statement.replace("<placeholder>", "third") + "<caret>";
                setup(stmt1 + '\n' + stmt2 + '\n' + stmt3 + '\n' + "true");
                doMoveUp();
                expect(stmt1 + '\n' + stmt3 + '\n' + stmt2 + '\n' + "true");
                doMoveUp();
                expect(stmt3 + '\n' + stmt1 + '\n' + stmt2 + '\n' + "true");
                doMoveUp();
                expect(stmt3 + '\n' + stmt1 + '\n' + stmt2 + '\n' + "true");
            }

            @ParameterizedTest
            @MethodSource("statements")
            void down(String statement) {
                String stmt1 = statement.replace("<placeholder>", "first") + "<caret>";
                String stmt2 = statement.replace("<placeholder>", "second");
                String stmt3 = statement.replace("<placeholder>", "third");
                setup(stmt1 + '\n' + stmt2 + '\n' + stmt3 + '\n' + "true");
                doMoveDown();
                expect(stmt2 + '\n' + stmt1 + '\n' + stmt3 + '\n' + "true");
                doMoveDown();
                expect(stmt2 + '\n' + stmt3 + '\n' + stmt1 + '\n' + "true");
                doMoveDown();
                expect(stmt2 + '\n' + stmt3 + '\n' + stmt1 + '\n' + "true");
            }

            static List<Named<String>> statements() {
                return List.of(
                        Named.of("assert", "assert <placeholder>;"),
                        Named.of("with", "with <placeholder>;"),
                        Named.of("let", "let x = <placeholder>; in"),
                        Named.of("lambda", "<placeholder>:")
                );
            }
        }

        @Nested
        @DisplayName("support if-then-else expressions")
        final class IfElseExpression {
            @Test
            void move_into() {
                // Not sure if we want to move into the condition first,
                // currently we skip the condition and move directly to the first case.
                setup("""
                        assert outside;<caret>
                        if
                          condition
                        then
                          case1
                        else
                          case2
                        """);
                doMoveDown();
                expect("""
                        if
                          condition
                        then
                        assert outside;<caret>
                          case1
                        else
                          case2
                        """);
            }

            @Test
            void move_out() {
                // Currently we skip the condition and move directly out of the if-expression.
                // Not sure if we want to keep this.
                setup("""
                        if
                          condition
                        then
                          assert inside;<caret>
                          case1
                        else
                          case2
                        """);
                doMoveUp();
                expect("""
                          assert inside;<caret>
                        if
                          condition
                        then
                          case1
                        else
                          case2
                        """);
            }

            @Test
            void move_then_down() {
                setup("""
                        if
                          condition
                        then
                          assert stationary1;
                          assert moved;<caret>
                          case1
                        else
                          assert stationary2;
                          case2
                        """);
                doMoveDown();
                expect("""
                        if
                          condition
                        then
                          assert stationary1;
                          case1
                        else
                          assert moved;<caret>
                          assert stationary2;
                          case2
                        """);
            }

            @Test
            void move_else_up() {
                setup("""
                        if
                          condition
                        then
                          assert stationary1;
                          case1
                        else
                          assert moved;<caret>
                          assert stationary2;
                          case2
                        """);
                doMoveUp();
                expect("""
                        if
                          condition
                        then
                          assert stationary1;
                          assert moved;<caret>
                          case1
                        else
                          assert stationary2;
                          case2
                        """);
            }

            @Test
            void recursive_move_else_down() {
                setup("""
                        if
                          condition1
                        then
                          if
                            condition2
                          then
                            case1
                          else
                            assert stationary1;
                            assert moved;<caret>
                            case2
                        else
                          assert stationary2;
                          case3
                        """);
                doMoveDown();
                expect("""
                        if
                          condition1
                        then
                          if
                            condition2
                          then
                            case1
                          else
                            assert stationary1;
                            case2
                        else
                            assert moved;<caret>
                          assert stationary2;
                          case3
                        """);
            }

            @Test
            void recursive_move_else_up() {
                setup("""
                        if
                          condition1
                        then
                          if
                            condition2
                          then
                            case1
                          else
                            assert stationary1;
                            case2
                        else
                          assert moved;<caret>
                          assert stationary2;
                          case3
                        """);
                doMoveUp();
                expect("""
                        if
                          condition1
                        then
                          if
                            condition2
                          then
                            case1
                          else
                            assert stationary1;
                          assert moved;<caret>
                            case2
                        else
                          assert stationary2;
                          case3
                        """);
            }

            @Test
            void do_not_move_itself_up() {
                setup("""
                        assert before;
                        if<caret> condition then case1 else case2
                        """);
                doMoveUp();
                expect("""
                        assert before;
                        if<caret> condition then case1 else case2
                        """);
            }

            @Test
            void do_not_move_itself_down() {
                setup("""
                        if<caret> condition
                        then
                          assert stationary1;
                          case1
                        else
                          assert stationary2;
                          case2
                        """);
                doMoveDown();
                expect("""
                        if<caret> condition
                        then
                          assert stationary1;
                          case1
                        else
                          assert stationary2;
                          case2
                        """);
            }

            @Test
            void do_not_move_when_selection_covers_multiple_subexpressions() {
                @Language("HTML")
                String code = """
                        assert before;
                        if condition
                        then
                          assert a;
                          <selection>assert b;
                          case1
                        else
                          assert c;<caret></selection>
                          assert d;
                          case2
                        """;
                setup(code);
                doMoveDown();
                expect(code);
                doMoveUp();
                expect(code);
            }
        }

        @Nested
        @DisplayName("move items on same line together")
        final class SameLine {
            @Test
            void simple_lines() {
                setup("""
                        assert stmt11; assert stmt12;<caret>
                        assert stmt21; assert stmt22;
                        assert stmt31; assert stmt32;
                        _
                        """);
                doMoveDown();
                expect("""
                        assert stmt21; assert stmt22;
                        assert stmt11; assert stmt12;<caret>
                        assert stmt31; assert stmt32;
                        _
                        """);
                doMoveDown();
                expect("""
                        assert stmt21; assert stmt22;
                        assert stmt31; assert stmt32;
                        assert stmt11; assert stmt12;<caret>
                        _
                        """);
            }

            @Test
            void move_extended_line_down() {
                setup("""
                        assert
                          a
                        ; assert
                          b<caret>
                        ; assert
                          c
                        ;
                        assert second;
                        _
                        """);
                doMoveDown();
                expect("""
                        assert second;
                        assert
                          a
                        ; assert
                          b<caret>
                        ; assert
                          c
                        ;
                        _
                        """);
            }

            @Test
            void move_extended_line_up() {
                setup("""
                        assert second;
                        assert
                          a
                        ; assert
                          b<caret>
                        ; assert
                          c
                        ;
                        _
                        """);
                doMoveUp();
                expect("""
                        assert
                          a
                        ; assert
                          b<caret>
                        ; assert
                          c
                        ;
                        assert second;
                        _
                        """);
            }

            @Test
            void move_over_extended_line_down() {
                setup("""
                        assert second;<caret>
                        assert
                          a
                        ; assert
                          b
                        ; assert
                          c
                        ;
                        _
                        """);
                doMoveDown();
                expect("""
                        assert
                          a
                        ; assert
                          b
                        ; assert
                          c
                        ;
                        assert second;<caret>
                        _
                        """);
            }

            @Test
            void move_over_extended_line_up() {
                setup("""
                        assert
                          a
                        ; assert
                          b
                        ; assert
                          c
                        ;
                        assert second;<caret>
                        _
                        """);
                doMoveUp();
                expect("""
                        assert second;<caret>
                        assert
                          a
                        ; assert
                          b
                        ; assert
                          c
                        ;
                        _
                        """);
            }
        }

        @Nested
        @DisplayName("allow selecting multiple lines to move")
        final class Selection {
            @Test
            void down() {
                setup("""
                        <selection>assert line1;
                        assert line2;<caret></selection>
                        assert line3;
                        _
                        """);
                doMoveDown();
                expect("""
                        assert line3;
                        <selection>assert line1;
                        assert line2;<caret></selection>
                        _
                        """);
            }

            @Test
            void up() {
                setup("""
                        assert line1;
                        <selection>assert line2;
                        assert line3;<caret></selection>
                        _
                        """);
                doMoveUp();
                expect("""
                        <selection>assert line2;
                        assert line3;<caret></selection>
                        assert line1;
                        _
                        """);
            }

            @Test
            void expanded_lines() {
                setup("""
                        assert non_selected;
                        assert a
                        ; assert b
                        ; <selection>assert c;
                        assert d;<caret></selection> assert e
                        ; assert f
                        ; assert g;
                        _
                        """);
                doMoveUp();
                expect("""
                        assert a
                        ; assert b
                        ; <selection>assert c;
                        assert d;<caret></selection> assert e
                        ; assert f
                        ; assert g;
                        assert non_selected;
                        _
                        """);
                doMoveDown();
                expect("""
                        assert non_selected;
                        assert a
                        ; assert b
                        ; <selection>assert c;
                        assert d;<caret></selection> assert e
                        ; assert f
                        ; assert g;
                        _
                        """);
            }
        }

        @Nested
        @DisplayName("no fallback")
        final class NoFallback {
            @Test
            void do_not_move_statement_further_up() {
                @Language("HTML")
                String code = """
                        assert outside1;
                        assert (
                          assert inside;<caret>
                          _
                        );
                        assert outside2;
                        _
                        """;
                setup(code);
                doMoveDown();
                expect(code);
                doMoveUp();
                expect(code);
            }
        }

        @Nested
        @DisplayName("support empty lines")
        final class EmptyLines {
            @Test
            void move_blank_line() {
                setup("""
                        (
                          assert a;
                          assert b;
                          assert c;
                          _
                          <caret>
                        )
                        """);
                doMoveUp();
                expect("""
                        (
                          assert a;
                          assert b;
                          assert c;
                          <caret>
                          _
                        )
                        """);
                doMoveUp();
                expect("""
                        (
                          assert a;
                          assert b;
                          <caret>
                          assert c;
                          _
                        )
                        """);
                doMoveDown();
                expect("""
                        (
                          assert a;
                          assert b;
                          assert c;
                          <caret>
                          _
                        )
                        """);
            }

            @Test
            void move_down_over_empty_line() {
                setup("""
                        assert a;<caret>
                        \s
                        _
                        """);
                doMoveDown();
                expect("""
                        \s
                        assert a;<caret>
                        _
                        """);
            }

            @Test
            void move_up_over_empty_line() {
                setup("""
                        \s
                        assert a;<caret>
                        _
                        """);
                doMoveUp();
                expect("""
                        assert a;<caret>
                        \s
                        _
                        """);
            }

            @Test
            void move_down_with_empty_line() {
                setup("""
                        <selection>\s
                        assert a;<caret></selection>
                        assert b;
                        _
                        """);
                doMoveDown();
                expect("""
                        assert b;
                        <selection>\s
                        assert a;<caret></selection>
                        _
                        """);
            }

            @Test
            void move_up_with_empty_line() {
                setup("""
                        assert a;
                        <selection>assert b;
                        \s
                        <caret></selection>_""");
                doMoveUp();
                expect("""
                        <selection>assert b;
                        \s
                        <caret></selection>assert a;
                        _""");
            }

            @Test
            void move_over_empty_line_with_empty_line() {
                setup("""
                        <selection>\s
                        assert a;
                        \s
                        <caret></selection>assert b;
                        _""");
                doMoveDown();
                expect("""
                        assert b;
                        <selection>\s
                        assert a;
                        \s
                        <caret></selection>_""");
                doMoveUp();
                expect("""
                        <selection>\s
                        assert a;
                        \s
                        <caret></selection>assert b;
                        _""");
            }

            @Test
            void move_over_blank_line() {
                setup("""
                        assert a;<caret>
                          \s
                        _
                        """);
                doMoveDown();
                expect("""
                          \s
                        assert a;<caret>
                        _
                        """);
            }
        }

        @Nested
        @DisplayName("allow comments")
        final class Comments {
            @Test
            void move_single_line_comment() {
                setup("""
                        # Comment<caret>
                        assert a;
                        assert b;
                        _
                        """);
                doMoveDown();
                expect("""
                        assert a;
                        # Comment<caret>
                        assert b;
                        _
                        """);
                doMoveDown();
                expect("""
                        assert a;
                        assert b;
                        # Comment<caret>
                        _
                        """);
            }

            @Test
            void move_multi_line_comment_down() {
                setup("""
                        /*
                         * Comment<caret>
                         */
                        assert a;
                        assert b;
                        _
                        """);
                doMoveDown();
                expect("""
                        assert a;
                        /*
                         * Comment<caret>
                         */
                        assert b;
                        _
                        """);
                doMoveDown();
                expect("""
                        assert a;
                        assert b;
                        /*
                         * Comment<caret>
                         */
                        _
                        """);
            }

            @Test
            void move_multi_line_comment_up() {
                setup("""
                        assert a;
                        assert b;
                        /*
                         * Comment<caret>
                         */
                        _
                        """);
                doMoveUp();
                expect("""
                        assert a;
                        /*
                         * Comment<caret>
                         */
                        assert b;
                        _
                        """);
                doMoveUp();
                expect("""
                        /*
                         * Comment<caret>
                         */
                        assert a;
                        assert b;
                        _
                        """);
            }

            @Test
            void move_item_below_single_line_comment() {
                setup("""
                        # Comment
                        assert a;<caret>
                        assert b;
                        _
                        """);
                doMoveDown();
                expect("""
                        # Comment
                        assert b;
                        assert a;<caret>
                        _
                        """);
            }

            @Test
            void move_over_multi_line_comment_down() {
                setup("""
                        assert a;<caret>
                        /*
                         * Comment
                         */
                        _
                        """);
                doMoveDown();
                expect("""
                        /*
                         * Comment
                         */
                        assert a;<caret>
                        _
                        """);
            }

            @Test
            void move_over_multi_line_comment_up() {
                setup("""
                        /*
                         * Comment
                         */
                        assert a;<caret>
                        _
                        """);
                doMoveUp();
                expect("""
                        assert a;<caret>
                        /*
                         * Comment
                         */
                        _
                        """);
            }

            @Test
            void expansion() {
                setup("""
                        assert a; /*
                        */ /*
                        */ assert b;<caret> /*
                        */ /*
                        */ assert c;
                        assert not_selected;
                        _
                        """);
                doMoveDown();
                expect("""
                        assert not_selected;
                        assert a; /*
                        */ /*
                        */ assert b;<caret> /*
                        */ /*
                        */ assert c;
                        _
                        """);
                doMoveUp();
                expect("""
                        assert a; /*
                        */ /*
                        */ assert b;<caret> /*
                        */ /*
                        */ assert c;
                        assert not_selected;
                        _
                        """);
            }

            @Test
            void select_via_comment_above() {
                setup("""
                        /* <caret>
                        */ assert a;
                        assert not_selected;
                        _
                        """);
                doMoveDown();
                expect("""
                        assert not_selected;
                        /* <caret>
                        */ assert a;
                        _
                        """);
            }

            @Test
            void select_via_comment_below() {
                setup("""
                        assert not_selected;
                        assert a; /*
                        */<caret>
                        _
                        """);
                doMoveUp();
                expect("""
                        assert a; /*
                        */<caret>
                        assert not_selected;
                        _
                        """);
            }
        }

        @Nested
        @DisplayName("do exception on file bounderies")
        final class FildBounderies {
            @Test
            void down_at_end_of_file() {
                setup("""
                        assert a;
                        assert b;<caret>""");
                doMoveDown();
                expect("""
                        assert a;
                        assert b;<caret>""");
            }

            @Test
            void up_at_start_of_file() {
                setup("""
                        <caret>assert a;
                        _
                        """);
                doMoveUp();
                expect("""
                        <caret>assert a;
                        _
                        """);
            }
        }
    }

    @Nested
    final class Mixed {
        @Test
        void move_list_items_before_moving_itself_as_a_statement() {
            // Note: This test assumes that NixBindAttr has the let-expression as a direct parent.
            // If this changes in the future, the test no-longer covers this scenario.
            // However, let-expressions are also the only nodes which are both, statement-like and list-like.
            setup("""
                    assert x;
                    let
                      a = y;
                      b = z;<caret>
                    in
                    _
                    """);
            doMoveUp();
            expect("""
                    assert x;
                    let
                      b = z;<caret>
                      a = y;
                    in
                    _
                    """);
            doMoveUp();
            expect("""
                    assert x;
                    let
                      b = z;<caret>
                      a = y;
                    in
                    _
                    """);
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
