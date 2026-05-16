package org.nixos.idea.psi

import com.intellij.openapi.application.ReadAction
import com.intellij.psi.ElementManipulators
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.util.containers.orNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.nixos.idea._testutil.Markers
import org.nixos.idea._testutil.WithIdeaPlatform

@WithIdeaPlatform.CodeInsight
class NixStringManipulatorTest(private val myFixture: CodeInsightTestFixture) {

    companion object {
        private val TAG_RANGE = Markers.tagName("range")
        private val TAG_STRING = Markers.tagName("string")
    }

    @Nested
    @DisplayName("handleContentChange()")
    inner class HandleContentChange {

        @Nested
        @DisplayName("NixStdString")
        inner class Std {

            @Test
            fun `add text to empty string`() {
                doTestChange(
                    """"<range/>"""",
                    "abc",
                    """"abc"""",
                )
            }

            @Test
            fun `make empty`() {
                doTestChange(
                    """"<range>abc</range>"""",
                    "",
                    "\"\"",
                )
            }

            @Test
            fun `add escape sequence`() {
                doTestChange(
                    """"|<range/>|"""",
                    """\n""",
                    """"|\\n|"""",
                )
            }

            @Test
            fun `add braces behind dollar`() {
                doTestChange(
                    """"|$<range/>|"""",
                    "{x}",
                    $$""""|\${x}|"""",
                )
            }

            @Test
            fun `add dollar before braces`() {
                doTestChange(
                    """"|<range/>{x}|"""",
                    "$",
                    $$""""|\${x}|"""",
                )
            }

            @Test
            fun `replace substring`() {
                doTestChange(
                    """"|<range>old content</range>|"""",
                    $$"new ${content}",
                    $$""""|new \${content}|"""",
                )
            }

            @Test
            fun `replace interpolations`() {
                doTestChange(
                    $$""""|<range>${x} and ${y}</range>|"""",
                    "new content",
                    """"|new content|"""",
                )
            }

            @Test
            @Disabled("not supported")
            fun `insert mid escape sequence`() {
                // TODO Do we need to support this? What would we expect?
                doTestChange(
                    """"|\<range/>n|"""",
                    "test",
                    """"|test\n|"""",
                )
            }

            @Test
            @Disabled("not supported")
            fun `fail on invalid range`() {
                // TODO Do we need to support this? What type of error would we expect?
                doTestChange(
                    $$""""|<range>${</range>x}|"""",
                    "new content",
                    "",
                )
            }

        }

        @Nested
        @DisplayName("NixIndString")
        inner class Ind {

            @Test
            fun `add text to empty string`() {
                doTestChange(
                    "''<range/>''",
                    "abc",
                    "''abc''",
                )
            }

            @Test
            fun `add spaces to empty string`() {
                // TODO Can we somehow handle this,
                //  so that the string actually resolves to the spaces, not to an empty string?
                doTestChange(
                    "''<range/>''",
                    "  ",
                    "''  ''",
                )
            }

            @Test
            fun `add line feed mid line`() {
                doTestChange(
                    """
                        |''
                        |  first<range> </range>line
                        |''""".trimMargin(),
                    "\n",
                    """
                        |''
                        |  first
                        |  line
                        |''""".trimMargin(),
                )
            }

            @Test
            fun `add multiple lines to empty string`() {
                doTestChange(
                    "''<range/>''",
                    """
                        |first line
                        |second line
                        |""".trimMargin(),
                    """
                        |''
                        |  first line
                        |  second line
                        |''""".trimMargin(),
                )
            }

            @Test
            fun `add multiple lines to single-line string`() {
                doTestChange(
                    "''first line<range/>''",
                    """
                        |
                        |second line
                        |third line
                        |""".trimMargin(),
                    """
                        |''
                        |  first line
                        |  second line
                        |  third line
                        |''""".trimMargin(),
                )
            }

            @Test
            fun `make empty (single line)`() {
                doTestChange(
                    "''<range>abc</range>''",
                    "",
                    "''''",
                )
            }

            @Test
            fun `make empty (multiline)`() {
                doTestChange(
                    """
                        |''<range>
                        |  first line
                        |  second line
                        |</range>''""".trimMargin(),
                    "",
                    "''''",
                )
            }

            @Test
            fun `add escape sequence`() {
                doTestChange(
                    "''|<range/>|''",
                    """''\n""",
                    """''|'''\n|''""",
                )
            }

            @Test
            fun `add braces behind dollar`() {
                doTestChange(
                    "''|$<range/>|''",
                    "{x}",
                    $$"''|''${x}|''",
                )
            }

            @Test
            fun `add dollar before braces`() {
                doTestChange(
                    "''|<range/>{x}|''",
                    "$",
                    $$"''|''${x}|''",
                )
            }

            @Test
            fun `add quote after existing quote`() {
                doTestChange(
                    "''|'<range/>|''",
                    "'",
                    "''|'''|''",
                )
            }

            @Test
            fun `add quote before existing quote`() {
                doTestChange(
                    "''|<range/>'|''",
                    "'",
                    "''|'''|''",
                )
            }

            @Test
            fun `remove line`() {
                doTestChange(
                    """
                        |''
                        |  line 1
                        |  line 2<range>
                        |  line 3</range>
                        |  line 4
                        |''""".trimMargin(),
                    "",
                    """
                        |''
                        |  line 1
                        |  line 2
                        |  line 4
                        |''""".trimMargin(),
                )
            }

            @Test
            fun `replace single line`() {
                // This is what the fragment editor will usually do
                doTestChange(
                    """
                        |''
                        |  first line
                        |  <range>third line
                        |</range>  second line
                        |''""".trimMargin(),
                    """
                        |new line
                        |""".trimMargin(),
                    """
                        |''
                        |  first line
                        |  new line
                        |  second line
                        |''""".trimMargin(),
                )
            }

            @Test
            fun `insert in front of line`() {
                // When adding content to the front of line 3 via the fragment editor,
                // The content might be injected at the end of the range from the previous line.
                doTestChange(
                    """
                        |''
                        |  line 1
                        |  <range>line 2
                        |</range>    line 3
                        |  line 4
                        |''""".trimMargin(),
                    """
                        |line 2
                        |x""".trimMargin(),
                    """
                        |''
                        |  line 1
                        |  line 2
                        |  x  line 3
                        |  line 4
                        |''""".trimMargin(),
                )
            }

            @Test
            fun `insert in front of empty line`() {
                // When adding content to the front of line 3 via the fragment editor,
                // The content might be injected at the end of the range from the previous line.
                doTestChange(
                    """
                        |''
                        |  line 1
                        |  <range>line 2
                        |</range>${" "}
                        |  line 4
                        |''""".trimMargin(),
                    """
                        |line 2
                        |x""".trimMargin(),
                    """
                        |''
                        |  line 1
                        |  line 2
                        |  x
                        |  line 4
                        |''""".trimMargin(),
                )
            }

            @Test
            fun `replace multiple lines`() {
                doTestChange(
                    """
                        |''
                        |  line 1
                        |  <range>line 2
                        |  line 3
                        |  line 4
                        |  line 5</range>
                        |  line 6
                        |''""".trimMargin(),
                    """
                        |x
                        |y
                        |z""".trimMargin(),
                    """
                        |''
                        |  line 1
                        |  x
                        |  y
                        |  z
                        |  line 6
                        |''""".trimMargin(),
                )
            }

            @Test
            fun `replace entire multiline string`() {
                doTestChange(
                    """
                        |''<range>
                        |  first line
                        |  second line
                        |</range>''""".trimMargin(),
                    """
                        |test 1
                        |test 2
                        |""".trimMargin(),
                    """
                        |''
                        |  test 1
                        |  test 2
                        |''""".trimMargin(),
                )
            }

            @Test
            fun `replace interpolations`() {
                doTestChange(
                    $$"""
                        |''
                        |  line ${1}
                        |  line <range>${2}
                        |  line ${3}
                        |  line ${4}</range>
                        |  line ${5}
                        |''""".trimMargin(),
                    "x",
                    $$"""
                        |''
                        |  line ${1}
                        |  line x
                        |  line ${5}
                        |''""".trimMargin(),
                )
            }

            @Test
            fun `insert line`() {
                doTestChange(
                    """
                        |''
                        |  line 1
                        |  line 2<range/>
                        |  line 3
                        |''""".trimMargin(),
                    "\nx",
                    """
                        |''
                        |  line 1
                        |  line 2
                        |  x
                        |  line 3
                        |''""".trimMargin(),
                )
            }

            @Test
            @Disabled("not supported")
            fun `insert mid escape sequence`() {
                // TODO Do we need to support this? What would we expect?
                doTestChange(
                    "''|''<range/>'|''",
                    "test",
                    "''|'test'|''",
                )
            }

            @Test
            @Disabled("not supported")
            fun `fail on invalid range`() {
                // TODO Do we need to support this? What type of error would we expect?
                doTestChange(
                    $$"''|<range>${</range>x}|''",
                    "new content",
                    "",
                )
            }

            @Test
            fun `preserve indent when entire range is replaced`() {
                doTestChange(
                    """
                        |{
                        |    x = <string>''<range>
                        |  strangely indented string
                        | </range>''</string>;
                        |}""".trimMargin(),
                    "new strangely indented string\n",
                    """
                        |{
                        |    x = ''
                        |  new strangely indented string
                        | '';
                        |}""".trimMargin(),
                )
            }

            @Test
            fun `preserve indent when partial range is replaced`() {
                doTestChange(
                    """
                        |{
                        |    x = <string>''
                        |  strangely indented string<range>
                        |</range> ''</string>;
                        |}""".trimMargin(),
                    " again\n",
                    """
                        |{
                        |    x = ''
                        |  strangely indented string again
                        | '';
                        |}""".trimMargin(),
                )
            }

            @Test
            fun `respect base indentation`() {
                doTestChange(
                    """
                        |{
                        |  x = {
                        |    y = <string>''<range/>''</string>;
                        |  };
                        |}
                        |""".trimMargin(),
                    """
                        |first line
                        |second line
                        |""".trimMargin(),
                    """
                        |{
                        |  x = {
                        |    y = ''
                        |      first line
                        |      second line
                        |    '';
                        |  };
                        |}
                        |""".trimMargin(),
                )
            }

        }

        fun doTestChange(initialSource: String, newContent: String, expectedSource: String) {
            val input = Markers.parse(initialSource, TAG_RANGE, TAG_STRING)
            ReadAction.run<RuntimeException> {
                val (stringElement, rangeInElement) = let {
                    val stringRange = input.markers(TAG_STRING).optional().map { it.range() }.orNull()
                    if (stringRange == null) {
                        Pair(
                            NixElementFactory.createString(myFixture.project, input.unmarkedText()),
                            input.singleRange(TAG_RANGE),
                        )
                    } else {
                        Pair(
                            NixElementFactory.createElement(
                                myFixture.project, NixString::class.java,
                                input.unmarkedText().substring(0, stringRange.startOffset),
                                stringRange.substring(input.unmarkedText()),
                                input.unmarkedText().substring(stringRange.endOffset),
                            ),
                            input.singleRange(TAG_RANGE).shiftLeft(stringRange.startOffset),
                        )
                    }
                }
                val file = stringElement.containingFile
                val result = ElementManipulators.handleContentChange(stringElement, rangeInElement, newContent)
                assertSame(file, result.containingFile)
                assertEquals(expectedSource, file.text)
            }
        }
    }

    @Nested
    @DisplayName("getRangeInElement()")
    inner class GetRangeInElement {

        @Nested
        @DisplayName("NixStdString")
        inner class Std {

            @Test
            fun empty() {
                doTestRange("\"<range/>\"")
            }

            @Test
            fun blank() {
                doTestRange("\"<range>    </range>\"")
            }

            @Test
            fun normal() {
                doTestRange("\"<range>abc</range>\"")
            }

        }

        @Nested
        @DisplayName("NixIndString")
        inner class Ind {

            @Test
            fun `single line`() {
                doTestRange("''<range>abc</range>''")
            }

            @Test
            fun `empty ()`() {
                doTestRange("''<range/>''")
            }

            @Test
            fun `empty with spaces on single line`() {
                doTestRange("''<range>    </range>''")
            }

            @Test
            fun `empty on two lines`() {
                doTestRange(
                    """
                    |''
                    |<range>    </range>''""".trimMargin()
                )
            }

            @Test
            fun `single newline`() {
                doTestRange(
                    """
                    |''
                    |<range>    ${""}
                    |  </range>''""".trimMargin()
                )
            }

            @Test
            fun `multiple lines`() {
                doTestRange(
                    """
                    |''
                    |<range>    first line
                    |    second line
                    |  </range>''""".trimMargin()
                )
            }

        }

        fun doTestRange(source: String) {
            val expected = Markers.parse(source, TAG_RANGE)
            ReadAction.run<RuntimeException> {
                val stringElement = NixElementFactory.createString(myFixture.project, expected.unmarkedText())
                val actualRange = ElementManipulators.getValueTextRange(stringElement)
                val actual = Markers.create(stringElement.text, TAG_RANGE, listOf(actualRange))
                assertEquals(expected, actual)
            }
        }
    }
}
