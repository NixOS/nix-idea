package org.nixos.idea.util;

import com.intellij.openapi.project.Project;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.nixos.idea._testutil.WithIdeaPlatform;
import org.nixos.idea.psi.NixElementFactory;
import org.nixos.idea.psi.NixString;
import org.nixos.idea.psi.NixStringPart;
import org.nixos.idea.psi.NixStringText;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("UnnecessaryStringEscape")
final class NixStringUtilTest {
    @ParameterizedTest(name = "[{index}] {0} -> {1}")
    @CsvSource(textBlock = """
            ''              , ""
            abc             , "abc"
            "               , "\\\""
            \\              , "\\\\"
            \\x             , "\\\\x"
            a${b}c          , "a\\${b}c"
            a$${b}c         , "a$${b}c"
            '\n'            , "\\n"
            '\r'            , "\\r"
            '\t'            , "\\t"
            # supplementary character, i.e. character form a supplementary plane,
            # which needs a surrogate pair to be represented in UTF-16
            \uD83C\uDF09    , "\uD83C\uDF09"
            """)
    void quote(String unescaped, String expectedResult) {
        assertEquals(expectedResult, NixStringUtil.quote(unescaped));
    }

    @ParameterizedTest(name = "[{index}] {0} -> {1}")
    @CsvSource(textBlock = """
            ''              , ''
            abc             , abc
            "               , \\"
            \\              , \\\\
            \\x             , \\\\x
            a${b}c          , a\\${b}c
            a$${b}c         , a$${b}c
            '\n'            , \\n
            '\r'            , \\r
            '\t'            , \\t
            # supplementary character, i.e. character form a supplementary plane,
            # which needs a surrogate pair to be represented in UTF-16
            \uD83C\uDF09    , \uD83C\uDF09
            """)
    void escapeStd(String unescaped, String expectedResult) {
        StringBuilder stringBuilder = new StringBuilder();
        NixStringUtil.escapeStd(stringBuilder, unescaped, 0);
        assertEquals(expectedResult, stringBuilder.toString());
    }

    @ParameterizedTest(name = "[{index}] {0} -> {1}")
    @CsvSource(textBlock = """
            # Do not add escape sequences when not necessary
            '|$|{x}'         , '\\${x}'
            '|$$|{x}'        , '$${x}'
            '|$$$|{x}'       , '$$\\${x}'
            '|$|${}'         , '$${}'
            '|$$|${}'        , '$$\\${}'
            # Ignore anything before the look-back marker
            '$||{x}'         , '${x}'
            '$|$|{x}'        , '$\\${x}'
            '$|$$|{x}'       , '$$${x}'
            '$||${}'         , '$\\${}'
            '$|$|${}'        , '$$${}'
            # Only look at trailing dolla signs in look-back area
            '|$x|${}'        , '$x\\${}'
            '|$x$|{}'        , '$x\\${}'
            '|$xx|${}'       , '$xx\\${}'
            '|$xx$|{}'       , '$xx\\${}'
            """)
    void escapeStdWithLookback(String unescaped, String expectedResult) {
        String[] split = unescaped.split("[|]", 3);
        StringBuilder stringBuilder = new StringBuilder(expectedResult.length());
        stringBuilder.append(split[0]).append(split[1]);
        NixStringUtil.escapeStd(stringBuilder, split[2], split[1].length());
        assertEquals(expectedResult, stringBuilder.toString());
    }

    @ParameterizedTest(name = "[{index}] {0}, indent={1}, indentStart={2}, indentEnd={3} -> {4}")
    @CsvSource(quoteCharacter = '|', textBlock = """
            # Indent non-empty lines
            ||              , 4, false, 2, ||
            ||              , 4, true , 2, ||
            |a|             , 4, false, 2, |a|
            |a|             , 4, true , 2, |    a|
            |\n\n|          , 4, false, 2, |\n|
            |\n\n|          , 4, true , 2, |\n|
            | \n \n|        , 4, false, 2, | \n     \n  |
            | \n \n |       , 4, false, 2, | \n     \n     |
            | \n \n|        , 4, true , 2, |     \n     \n  |
            | \n \n |       , 4, true , 2, |     \n     \n     |
            # Should be be escaped
            |''|            , 2, false, 0, |'''|
            |'''|           , 2, false, 0, |''''|
            |''''|          , 2, false, 0, |''''''|
            |${|            , 2, false, 0, |''${|
            |\r|            , 2, false, 0, |''\\r|
            |\t|            , 2, false, 0, |''\\t|
            # Should not be escaped
            |\\|            , 2, false, 0, |\\|
            |\\x|           , 2, false, 0, |\\x|
            |$${|           , 2, false, 0, |$${|
            |\nx|           , 2, false, 0, |\n  x|
            # supplementary character, i.e. character form a supplementary plane,
            # which needs a surrogate pair to be represented in UTF-16
            |\uD83C\uDF09|  , 2, false, 0, |\uD83C\uDF09|
            """)
    void escapeInd(String unescaped, int indent, boolean indentStart, int indentEnd, String expectedResult) {
        StringBuilder stringBuilder = new StringBuilder();
        NixStringUtil.escapeInd(stringBuilder, unescaped, indent, indentStart, indentEnd);
        assertEquals(expectedResult, stringBuilder.toString());
    }

    @ParameterizedTest(name = "[{index}] {0} -> {1}")
    @CsvSource(quoteCharacter = '|', textBlock = """
            # Non-indented strings always return zero
            |""|                , 0
            |"    a"|           , 0
            |"  a\n  b"|        , 0
            # When there are only spaces, we return the fallback
            |''''|              , 10
            |''    ''|          , 10
            |''\n  \n  ''|      , 10
            # Unless the indention of the blank lines is larger then the fallback
            |''\n            ''|, 12
            # The smallest indentation counts
            |''\n  a\n b''|     , 1
            |''\n a\n  b''|     , 1
            |''\n  a\n  b''|    , 2
            |''\n  a\n ${b}''|  , 1
            |''\n  a\n ''\\b''| , 1
            # First line counts
            |''a\n  b''|        , 0
            |''${a}\n  b''|     , 0
            |''''\\a\n  b''|    , 0
            # But only the first token in a line counts
            |''  a${b}''|       , 2
            |''  a''\\b''|      , 2
            |''  ${a}b''|       , 2
            |''  ${a}${b}''|    , 2
            |''  ${a}''\\b''|   , 2
            |''  ''\\ab''|      , 2
            |''  ''\\a${b}''|   , 2
            |''  ''\\a''\\b''|  , 2
            # Tab and CR are treated as normal characters, not as spaces
            # See NixOS/nix#2911 and NixOS/nix#3759
            |''\t''|            , 0
            |''\n  \t''|        , 2
            |''\r\n''|          , 0
            |''\n  \r\n''|      , 2
            # Indentation within interpolations is ignored
            |''  ${\n"a"}''|    , 2
            |''  ${\n''a''}''|  , 2
            """)
    @WithIdeaPlatform.OnEdt
    void detectIndent(String code, int expectedResult, Project project) {
        NixString string = NixElementFactory.createString(project, code);
        assertEquals(expectedResult, NixStringUtil.detectIndent(string, () -> 10));
    }

    @ParameterizedTest(name = "[{index}] {0} -> {1}")
    @CsvSource(quoteCharacter = '|', textBlock = """
            ""              , ||
            "x"             , x
            ''abc''         , abc
            "\\""           , "
            "\\\\"          , \\
            "\\\\x"         , \\x
            ''"''           , "
            ''\\"''         , \\"
            ''\\x''         , \\x
            ''\\\\''        , \\\\
            ''\\\\x''       , \\\\x
            ''''\\"''       , "
            ''''\\\\''      , \\
            ''''\\\\x''     , \\x
            '''''''         , |''|
            "''\\""         , ''"
            "a\\${b}c"      , a${b}c
            ''a''${b}c''    , a${b}c
            ''a''\\${b}c''  , a${b}c
            "a$${b}c"       , a$${b}c
            ''a$${b}c''     , a$${b}c
            |"\n"|          , |\n|
            |"\r"|          , |\r|
            |"\t"|          , |\t|
            |"\\n"|         , |\n|
            |"\\r"|         , |\r|
            |"\\t"|         , |\t|
            |''_\n''|       , |_\n|
            |''\r''|        , |\r|
            |''\t''|        , |\t|
            |''''\\n''|     , |\n|
            |''''\\r''|     , |\r|
            |''''\\t''|     , |\t|
            # supplementary character, i.e. character form a supplementary plane,
            # which needs a surrogate pair to be represented in UTF-16
            "\uD83C\uDF09"  , \uD83C\uDF09
            ''\uD83C\uDF09'', \uD83C\uDF09
            # Remove common indentation in indented strings
            |''  ''|        , ||
            |'' a ''|       , |a |
            |''    a    ''| , |a    |
            |'' a\n b\n''|  , |a\nb\n|
            |'' a\n  b\n''| , |a\n b\n|
            # But don't remove indentation when there is one line without it
            |'' a\nb\n c''| , | a\nb\n c|
            |''a\n b\n c''| , |a\n b\n c|
            |''    a\n\tb''|, |    a\n\tb|
            |''\ta\n    b''|, |\ta\n    b|
            # Even when the line is blank
            |'' a\n  ''|    , |a\n |
            # Ignore indentation of empty lines
            |'' a\n\n b\n''|, |a\n\nb\n|
            # Remove initial line break in indented strings
            |''\n    a''|   , |a|
            |''  \n  a''|   , |a|
            """)
    @WithIdeaPlatform.OnEdt
    void parse(String code, String expectedResult, Project project) {
        NixString string = NixElementFactory.createString(project, code);
        List<NixStringPart> parts = string.getStringParts();
        assert parts.isEmpty() || parts.size() == 1;
        for (NixStringPart part : parts) {
            assertEquals(expectedResult, NixStringUtil.parse((NixStringText) part));
        }
    }
}
