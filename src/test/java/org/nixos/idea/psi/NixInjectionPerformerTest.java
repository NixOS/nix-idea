package org.nixos.idea.psi;

import com.intellij.lang.injection.general.LanguageInjectionContributor;
import com.intellij.lang.injection.general.SimpleInjection;
import com.intellij.openapi.editor.CaretState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.util.TextRange;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.EditorTestFixture;
import com.intellij.testFramework.fixtures.InjectionTestFixture;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.nixos.idea._testutil.Markers;
import org.nixos.idea._testutil.WithIdeaPlatform;
import org.nixos.idea.lang.NixLanguage;

import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WithIdeaPlatform.CodeInsight
@WithIdeaPlatform.OnEdt
@NullMarked
final class NixInjectionPerformerTest {

    // TODO issue2: Preserving indentation doesn't work yet.
    //      The first line is missing its indent because prevSibling (the leading PsiWhiteSpace
    //      newline) is not null, so AbstractNixString.updateText sets indentStart=false.
    //      The closing '' also gains extra indent because nextSibling is IND_STRING_CLOSE.
    //      Fix requires implementing baseIndent() in AbstractNixString.
    // TODO issue3: How to handle cases when the last non-indented line is removed from an indented string?
    // TODO issue6: Handle insertion of `{` behind `$`

    private final CodeInsightTestFixture myFixture;
    private final InjectionTestFixture myInjectionFixture;
    private String prefix = "";
    private String suffix = "";

    NixInjectionPerformerTest(CodeInsightTestFixture fixture) {
        myFixture = fixture;
        myInjectionFixture = new InjectionTestFixture(fixture);

        // Inject a language into every NixString inside this test.
        // Without an injected language, we would not be able to open the fragment editor.
        LanguageInjectionContributor contributor =
                element -> element instanceof NixString
                        ? new SimpleInjection(PlainTextLanguage.INSTANCE, prefix, suffix, null)
                        : null;
        LanguageInjectionContributor.INJECTOR_EXTENSION.addExplicitExtension(
                NixLanguage.INSTANCE, contributor, myFixture.getTestRootDisposable());
    }

    // -------------------------------------------------------------------------
    // Read-direction tests: verify what the fragment editor sees
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("open fragment editor")
    final class Read {

        @Test
        void empty() {
            doTestRead(
                    "\"<caret>\"",
                    "<caret>"
            );
        }

        @Test
        void containing_escape_sequence_std() {
            doTestRead(
                    "\"first \\${<caret>} line\"",
                    "first ${<caret>} line"
            );
        }

        @Test
        void containing_escape_sequence_ind() {
            doTestRead(
                    "''first ''${<caret>} line''",
                    "first ${<caret>} line"
            );
        }

        @Test
        void caret_before_escape_sequence() {
            doTestRead(
                    "''|<caret>''\\${}|''",
                    "|<caret>${}|"
            );
        }

        @Test
        void caret_after_escape_sequence() {
            doTestRead(
                    "''|''\\$<caret>{}|''",
                    "|$<caret>{}|"
            );
        }

        @Test
        void caret_on_escape_sequence() {
            // Moving the caret before the unescaped character seems like the better fit.
            // The significant character tends to be at the end of the escape sequence.
            doTestRead(
                    "''|''<caret>\\${}|''",
                    "|<caret>${}|"
            );
        }

        @Test
        void trim_initial_line() {
            doTestRead(
                    "''\nfirst<caret> line''",
                    "first<caret> line"
            );
        }

        @Test
        void trim_initial_line_with_spaces() {
            doTestRead(
                    "''  \nfirst<caret> line''",
                    "first<caret> line"
            );
        }

        @Test
        void trim_indentation_simple() {
            doTestRead(
                    """
                            ''
                              single<caret> line
                            ''
                            """,
                    "single<caret> line\n"
            );
        }

        @Test
        void trim_indentation_empty() {
            doTestRead(
                    """
                            ''
                              <caret>
                            ''
                            """,
                    "<caret>\n"
            );
        }

        @Test
        void trim_indentation_multiline() {
            doTestRead(
                    """
                            ''
                              first line
                            
                                second<caret> line
                               \s
                              third line
                            ''
                            """,
                    """
                            first line
                            
                              second<caret> line
                             \s
                            third line
                            """
            );
        }

        @Test
        void include_prefix_and_suffix_ind() {
            prefix = "prefix\n";
            suffix = "suffix\n";
            doTestRead(
                    "''\n<caret>\n''",
                    "prefix\n<caret>\nsuffix\n"
            );
        }

        @Test
        void include_prefix_std() {
            prefix = "prefix";
            doTestRead(
                    "\"|<caret>\"",
                    "prefix|<caret>"
            );
        }

        @Test
        void include_suffix_std() {
            suffix = "suffix";
            doTestRead(
                    "\"<caret>|\"",
                    "<caret>|suffix"
            );
        }

        @Test
        void indented_string_starting_with_interpolation_on_new_line() {
            // Can trigger InjectionRegistrarImpl$PatchException (IJPL-244525),
            // depending on how LiteralTextEscaper.getRelevantTextRange is implemented.
            doTestRead(
                    """
                            ''
                              ${x}<caret>''\\\\''
                            """,
                    "<interpolation><caret>\\"
            );
        }

        @Test
        void indented_string_starting_with_interpolation_after_spaces() {
            // Can trigger InjectionRegistrarImpl$PatchException (IJPL-244525),
            // depending on how LiteralTextEscaper.getRelevantTextRange is implemented.
            doTestRead(
                    "''  ${x}<caret>x''",
                    "<interpolation><caret>x"
            );
        }

        @Test
        void interpolation_only_std() {
            doTestRead(
                    "\"<caret>${x}\"",
                    "<caret><interpolation>"
            );
        }

        @Test
        void interpolation_only_ind() {
            doTestRead(
                    "''${x}<caret>''",
                    "<interpolation><caret>"
            );
        }

    }

    // -------------------------------------------------------------------------
    // Write-direction tests: verify that edits to the injected editor
    // are reflected correctly in the host Nix source
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("edit via fragment editor (miscellaneous)")
    final class Miscellaneous {

        @Test
        void make_empty() {
            doTestUpdate(
                    "\"x<caret>\"",
                    "\b",
                    "\"\""
            );
        }

    }

    @Nested
    @DisplayName("edit escape sequences via fragment editor")
    final class EditEscapeSequences {

        @Nested
        @DisplayName("preserve existing escape sequences")
        final class Existing {

            // Currently, the existing escape sequences get normalized after making edits in the fragment editor.
            // While this behavior is reflected in these tests, it is not an intentional decision.
            // I expect it would be better if we could keep existing escape sequences stable.

            @Test
            void before_caret() {
                doTestUpdate(
                        "\"\\\\<caret>\""
                );
            }

            @Test
            void after_caret() {
                doTestUpdate(
                        "''<caret>''\\\\''",
                        "''<caret>\\''"
                );
            }

            @Test
            void idiomatic_ind() {
                doTestUpdate(
                        "''before ''${<caret>} after''"
                );
            }

            @Test
            void unusual_ind_1() {
                doTestUpdate(
                        "''before ''\\${<caret>} after''",
                        "''before ''${<caret>} after''"
                );
            }

            @Test
            void unusual_ind_2() {
                doTestUpdate(
                        "''before $''\\{<caret>} after''",
                        "''before ''${<caret>} after''"
                );
            }

            @Test
            void stable_std() {
                doTestUpdate(
                        "\"before \\${<caret>} after\""
                );
            }

            @Test
            void unstable_std() {
                doTestUpdate(
                        "\"before $\\{<caret>} after\"",
                        "\"before \\${<caret>} after\""
                );
            }

            @Test
            void separated_by_interpolations() {
                doTestUpdate(
                        "''|''\\${}${x}|<caret>|${x}''\\${}|''",
                        "''|''${}${x}|<caret>|${x}''${}|''"
                );
            }

        }

        @Nested
        @DisplayName("add escape sequences")
        final class Add {

            @Test
            void std_newline() {
                doTestUpdate(
                        "\"|<caret>|\"",
                        "\n",
                        "\"|\\n|\""
                );
            }

            @Test
            void std_interpolation() {
                doTestUpdate(
                        "\"|$<caret>|\"",
                        "{",
                        "\"|\\${|\""
                );
            }

            @Test
            void ind_interpolation() {
                doTestUpdate(
                        "''|$<caret>|''",
                        "{",
                        "''|''${|''"
                );
            }

            @Test
            void braces_after_dollar_std() {
                doTestUpdate(
                        "\"|$<caret>|\"",
                        "{",
                        "\"|\\${|\""
                );
            }

            @Test
            void braces_after_dollar_ind() {
                doTestUpdate(
                        "''|$<caret>|''",
                        "{",
                        "''|''${|''"
                );
            }

            @Test
            void braces_after_double_dollar_std() {
                doTestUpdate(
                        "\"|$$<caret>|\"",
                        "{",
                        "\"|$${|\""
                );
            }

            @Test
            void braces_after_double_dollar_ind() {
                doTestUpdate(
                        "''|$$<caret>|''",
                        "{",
                        "''|$${|''"
                );
            }

            @Test
            void braces_after_escaped_dollar_std() {
                doTestUpdate(
                        "\"|\\$<caret>|\"",
                        "{",
                        "\"|\\${|\""
                );
            }

            @Test
            void braces_after_escaped_dollar_ind() {
                doTestUpdate(
                        "''|''$<caret>|''",
                        "{",
                        "''|''${|''"
                );
            }

        }

        @Nested
        @DisplayName("delete escape sequences")
        final class Delete {

            @Test
            void backspace() {
                doTestUpdate(
                        "\"\\\\<caret>|\"",
                        "\b|42",
                        "\"|42|\""
                );
            }

            @Test
            void delete() {
                doTestUpdate(
                        "\"<caret>\\\\\"",
                        "\u007f",
                        "\"\""
                );
            }

            @Test
            void ind_string() {
                doTestUpdate(
                        "''<caret>''${}''",
                        "\u007f",
                        "''{}''"
                );
            }

        }

    }

    @Nested
    @DisplayName("use multiple carets in fragment editor")
    final class Multicursor {

        @Test
        void type() {
            doTestUpdate(
                    """
                            ''
                            1<caret>
                            2
                            3 4
                            ''
                            """,
                    """
                            1<caret>
                            2<caret>
                            3<caret> 4<caret>
                            """,
                    "\b42",
                    """
                            ''
                            42
                            42
                            42 42
                            ''
                            """
            );
        }

        @Test
        @Disabled("issue6") // TODO
        void multiselection() {
            doTestUpdate(
                    """
                            ''
                            1<caret>
                            2 - ''${x} - '''
                            3{
                            ''
                            """,
                    """
                            1<selection/>
                            <selection>2 - ${x} - ''</selection>
                            <selection>3</selection>{
                            """,
                    "$",
                    """
                            ''
                            1$
                            $
                            ''${
                            ''
                            """
            );
        }

    }

    @Nested
    @DisplayName("preserve indentation during edit via fragment editor")
    @Disabled("issue2") // TODO
    final class Indentation {

        @Test
        void add_line() {
            doTestUpdate(
                    """
                            ''
                              test<caret>
                            ''
                            """,
                    "\n42",
                    """
                            ''
                              test
                              42
                            ''
                            """
            );
        }

        @Test
        void keep_indentation_of_closing_quotes_1() {
            doTestUpdate(
                    """
                              ''
                                content<caret>
                              ''
                            """
            );
        }

        @Test
        void keep_indentation_of_closing_quotes_2() {
            doTestUpdate(
                    """
                              ''
                                content<caret>
                                ''
                            """
            );
        }

        @Test
        void insert_into_empty_line() {
            // Covers a scenario where typing one character requires inserting multiple spaces for indentation.
            doTestUpdate(
                    """
                            ''
                              first line
                              <caret>
                              last line
                            ''
                            """,
                    " ",
                    """
                            ''
                              first line<caret>
                              \s
                              last line
                            ''
                            """
            );
        }

        @Test
        void insert_into_blank_string() {
            // Covers a scenario without text to infer the indentation.
            doTestUpdate(
                    """
                              func ''
                                <caret>
                              ''
                            """,
                    """
                            first line
                            second line\
                            """,
                    """
                              func ''
                                first line
                                second line
                              ''
                            """
            );
        }

        @Test
        @Disabled("issue3") // TODO
        void remove_last_line_without_indentation() {
            doTestUpdate(
                    """
                            ''
                            x<caret>
                              nested
                            ''
                            """,
                    "\b",
                    """
                            ''
                            ${""}
                              nested
                            ''
                            """
            );
        }

    }

    @Nested
    @DisplayName("edit text before and after interpolations in fragment editor")
    final class Interpolations {

        @Test
        void insert_before_interpolation() {
            doTestUpdate(
                    "''<caret>${x}''"
            );
        }

        @Test
        void insert_after_interpolation() {
            doTestUpdate(
                    "''${x}<caret>''"
            );
        }

        @Test
        void insert_between_interpolations() {
            doTestUpdate(
                    "''${x}<caret>${y}''"
            );
        }

        @Test
        void cannot_modify_interpolation() {
            doTestUpdateInternal(
                    "''${x} <caret> ${y}''",
                    "\b\b\b\u007f\u007f\u007f",
                    "<interpolation><caret><interpolation>"
            );
        }

    }

    @Nested
    @DisplayName("try touching prefix and suffix in fragment editor")
    final class PrefixAndSuffix {

        @Test
        void prefix() {
            prefix = "prefix";
            doTestUpdateInternal(
                    "\"<caret>|\"",
                    "\b\b",
                    "prefix<caret>|"
            );
        }

        @Test
        void suffix() {
            suffix = "suffix";
            doTestUpdateInternal(
                    "''|<caret>''",
                    "\u007f\u007f",
                    "|<caret>suffix"
            );
        }

    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Verifies the read direction: Loads {@code sourceCode} into an editor
     * respecting the {@code <caret>} marker, tries to open a fragment editor
     * from the caret location, and checks that the content of the fragment
     * editor matches {@code expectedFragment} (with {@code <interpolation>}
     * replaced by the actual placeholder strings).
     */
    private void doTestRead(String sourceCode, String expectedFragment) {
        myFixture.configureByText("test.nix", sourceCode);
        EditorTestFixture fragmentEditor = myInjectionFixture.openInFragmentEditor();

        int actualCaretOffset = fragmentEditor.getEditor().getCaretModel().getOffset();
        Markers actual = Markers.create(
                fragmentEditor.getEditor().getDocument().getText(),
                Markers.TAG_CARET,
                List.of(TextRange.from(actualCaretOffset, 0))
        );

        Markers expected = Markers.parse(buildExpectedContent(expectedFragment), Markers.TAG_CARET);
        assertEquals(expected, actual, "text in fragment editor");
    }

    private void doTestUpdate(String sourceCode) {
        doTestUpdate(sourceCode, sourceCode);
    }

    private void doTestUpdate(String initialSource, String expectedSource) {
        doTestUpdate(initialSource, "****", expectedSource.replace("<caret>", "****"));
    }

    private void doTestUpdate(String initialSource, String input, String expectedSource) {
        doTestUpdate(initialSource, null, input, expectedSource);
    }

    /**
     * Verifies the write direction: configures a Nix file with {@code initialSource},
     * opens a fragment editor at the {@code <caret>} marker,
     * optionally updates the selection and caret position,
     * types the given {@code input},
     * and asserts the resulting Nix file matches {@code expectedSource}.
     */
    private void doTestUpdate(String initialSource, @Nullable String selection, String input, String expectedSource) {
        myFixture.configureByText("test.nix", initialSource);
        EditorTestFixture fragmentFixture = myInjectionFixture.openInFragmentEditor();
        Editor fragmentEditor = fragmentFixture.getEditor();

        if (selection != null) {
            updateSelection(fragmentEditor, selection);
        }

        fragmentFixture.type(input);
        String actual = myInjectionFixture.getTopLevelEditor().getDocument().getText();
        assertEquals(expectedSource, actual, "resulting Nix source");
    }

    /**
     * Verifies writes inside the fragment: configures a Nix file with {@code initialSource},
     * opens a fragment editor at the {@code <caret>} marker,
     * types the given {@code input},
     * and asserts the resulting text inside the fragment editor matches {@code expectedFragment}.
     */
    private void doTestUpdateInternal(String initialSource, String input, String expectedFragment) {
        myFixture.configureByText("test.nix", initialSource);
        EditorTestFixture fragmentFixture = myInjectionFixture.openInFragmentEditor();
        Editor fragmentEditor = fragmentFixture.getEditor();

        fragmentFixture.type(input);

        int actualCaretOffset = fragmentEditor.getCaretModel().getOffset();
        Markers actual = Markers.create(
                fragmentEditor.getDocument().getText(),
                Markers.TAG_CARET,
                List.of(TextRange.from(actualCaretOffset, 0))
        );

        Markers expected = Markers.parse(buildExpectedContent(expectedFragment), Markers.TAG_CARET);
        assertEquals(expected, actual, "resulting text in fragment editor");
    }

    private static void updateSelection(Editor editor, String selection) {
        Markers allMarkers = Markers.parse(buildExpectedContent(selection), Markers.TAG_CARET, Markers.TAG_SELECTION);
        assertEquals(allMarkers.unmarkedText(), editor.getDocument().getText(), "text before selection");
        Markers selectionMarkers = allMarkers.markers(Markers.TAG_SELECTION);
        Markers caretMarkers = allMarkers.markers(Markers.TAG_CARET);
        if (caretMarkers.size() != selectionMarkers.size() && !selectionMarkers.isEmpty() && !caretMarkers.isEmpty()) {
            throw new IllegalStateException(String.format(
                    "caretMarkers.size() == %d; selectionMarkers.size() == %d",
                    caretMarkers.size(), selectionMarkers.size()));
        }

        int carets = Math.max(caretMarkers.size(), selectionMarkers.size());
        editor.getCaretModel().setCaretsAndSelections(IntStream.range(0, carets).mapToObj(i -> {
            LogicalPosition caretPosition;
            LogicalPosition selectionStart = null;
            LogicalPosition selectionEnd = null;
            if (!selectionMarkers.isEmpty()) {
                Markers.Marker marker = selectionMarkers.get(i);
                selectionStart = editor.offsetToLogicalPosition(marker.start());
                selectionEnd = editor.offsetToLogicalPosition(marker.end());
            }
            if (!caretMarkers.isEmpty()) {
                caretPosition = editor.offsetToLogicalPosition(caretMarkers.get(i).offset());
            } else {
                caretPosition = selectionStart;
            }
            return new CaretState(caretPosition, selectionStart, selectionEnd);
        }).toList());
    }

    /**
     * Converts an expected fragment template (with {@code <interpolation>} markers)
     * into the actual string to compare against.
     */
    private static String buildExpectedContent(String template) {
        String result = template;
        int index = 0;
        while (result.contains("<interpolation>")) {
            result = result.replaceFirst("<interpolation>",
                    Matcher.quoteReplacement(interpolationPlaceholder(index++)));
        }
        return result;
    }

    private static String interpolationPlaceholder(int index) {
        return "(__interpolation" + index + "__)";
    }
}
