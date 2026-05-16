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
                doChangeTest(
                    """"<range/>"""",
                    "abc",
                    """"abc"""",
                )
            }

            @Test
            fun `make empty`() {
                doChangeTest(
                    """"<range>abc</range>"""",
                    "",
                    "\"\"",
                )
            }

            @Test
            fun `add escape sequence`() {
                doChangeTest(
                    """"|<range/>|"""",
                    """\n""",
                    """"|\\n|"""",
                )
            }

            @Test
            fun `add braces behind dollar`() {
                doChangeTest(
                    """"|$<range/>|"""",
                    "{x}",
                    $$""""|\${x}|"""",
                )
            }

            @Test
            fun `replace substring`() {
                doChangeTest(
                    """"|<range>old content</range>|"""",
                    $$"new ${content}",
                    $$""""|new \${content}|"""",
                )
            }

            @Test
            fun `replace interpolations`() {
                doChangeTest(
                    $$""""|<range>${x} and ${y}</range>|"""",
                    "new content",
                    """"|new content|"""",
                )
            }

            @Test
            @Disabled("not supported")
            fun `insert mid escape sequence`() {
                // TODO Do we need to support this? What would we expect?
                doChangeTest(
                    """"|\<range/>n|"""",
                    "test",
                    """"|test\n|"""",
                )
            }

            @Test
            @Disabled("not supported")
            fun `fail on invalid range`() {
                // TODO Do we need to support this? What type of error would we expect?
                doChangeTest(
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
                doChangeTest(
                    "''<range/>''",
                    "abc",
                    "''abc''",
                )
            }

            @Test
            fun `add spaces to empty string`() {
                // TODO Can we somehow handle this,
                //  so that the string actually resolves to the spaces, not to an empty string?
                doChangeTest(
                    "''<range/>''",
                    "  ",
                    "''  ''",
                )
            }

            @Test
            fun `add multiple lines to empty string`() {
                doChangeTest(
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
                doChangeTest(
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
                doChangeTest(
                    "''<range>abc</range>''",
                    "",
                    "''''",
                )
            }

            @Test
            fun `make empty (multiline)`() {
                doChangeTest(
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
                doChangeTest(
                    "''|<range/>|''",
                    """''\n""",
                    """''|'''\n|''""",
                )
            }

            @Test
            fun `add braces behind dollar`() {
                doChangeTest(
                    "''|$<range/>|''",
                    "{x}",
                    $$"''|''${x}|''",
                )
            }

            @Test
            fun `remove line`() {
                doChangeTest(
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
                doChangeTest(
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
                doChangeTest(
                    """
                        |''
                        |  line 1
                        |  <range>line 2
                        |<range/>  line 3
                        |  line 4
                        |''""".trimMargin(),
                    """
                        |''
                        |line 2
                        |x ''""".trimMargin(),
                    """
                        |''
                        |  line 1
                        |  line 2
                        |  x line 3
                        |  line 4
                        |''""".trimMargin(),
                )
            }

            @Test
            fun `replace multiple lines`() {
                doChangeTest(
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
                doChangeTest(
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
                doChangeTest(
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
                doChangeTest(
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
                doChangeTest(
                    "''|''<range/>'|''",
                    "test",
                    "''|'test'|''",
                )
            }

            @Test
            @Disabled("not supported")
            fun `fail on invalid range`() {
                // TODO Do we need to support this? What type of error would we expect?
                doChangeTest(
                    $$"''|<range>${</range>x}|''",
                    "new content",
                    "",
                )
            }

            @Test
            fun `respect base indentation`() {
                doChangeTest(
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
                        |    ''
                        |  };
                        |}
                        |""".trimMargin(),
                )
            }

        }

        fun doChangeTest(initialSource: String, newContent: String, expectedSource: String) {
            val input = Markers.parse(initialSource, TAG_RANGE, TAG_STRING)
            val stringRange = input.markers(TAG_STRING).optional().map { it.range() }.orNull()
            ReadAction.run<RuntimeException> {
                val stringElement = if (stringRange == null) {
                    NixElementFactory.createString(myFixture.project, input.unmarkedText())
                } else {
                    NixElementFactory.createElement(
                        myFixture.project, NixString::class.java,
                        input.unmarkedText().substring(0, stringRange.startOffset),
                        stringRange.substring(input.unmarkedText()),
                        input.unmarkedText().substring(stringRange.endOffset),
                    )
                }
                val file = stringElement.containingFile
                val result = ElementManipulators.handleContentChange(stringElement, input.singleRange(TAG_RANGE), newContent)
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
                doRangeTest("\"<range/>\"")
            }

            @Test
            fun blank() {
                doRangeTest("\"<range>    </range>\"")
            }

            @Test
            fun normal() {
                doRangeTest("\"<range>abc</range>\"")
            }

        }

        @Nested
        @DisplayName("NixIndString")
        inner class Ind {

            @Test
            fun `single line`() {
                doRangeTest("''<range>abc</range>''")
            }

            @Test
            fun `empty ()`() {
                doRangeTest("''<range/>''")
            }

            @Test
            fun `empty with spaces on single line`() {
                doRangeTest("''<range>    </range>''")
            }

            @Test
            fun `empty on two lines`() {
                doRangeTest(
                    """
                    |''
                    |<range>    </range>''""".trimMargin()
                )
            }

            @Test
            fun `single newline`() {
                doRangeTest(
                    """
                    |''
                    |<range>    ${""}
                    |  </range>''""".trimMargin()
                )
            }

            @Test
            fun `multiple lines`() {
                doRangeTest(
                    """
                    |''
                    |<range>    first line
                    |    second line
                    |  </range>''""".trimMargin()
                )
            }

        }

        fun doRangeTest(source: String) {
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
