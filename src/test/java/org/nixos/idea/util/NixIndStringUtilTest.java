package org.nixos.idea.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.nixos.idea.psi.NixStringLiteralEscaper;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class NixIndStringUtilTest {
    @ParameterizedTest(name = "[{index}] {0} -> {1}")
    @CsvSource(quoteCharacter = '|', textBlock = """
            ||              , ||
            abc             , abc
            "               , "
            \\              , \\
            \\x             , \\x
            a${b}c          , a${b}c
            |\n|            , |\n|
            |\r|            , |\r|
            |\t|            , |\t|
            |''\\t|         , |\t|
            |''\\r|         , |\r|
            |''\\n|         , |\n|
            |'''|           , |''|
            $$              , $$
            ''$             , $
            # supplementary character, i.e. character form a supplementary plane,
            # which needs a surrogate pair to be represented in UTF-16
            \uD83C\uDF09    , \uD83C\uDF09
            """)
    void unescape(String escaped, String expectedResult) {
        var sb = new StringBuilder();
        NixStringLiteralEscaper.Companion.unescapeAndDecode(escaped, sb, null);
        var str = sb.toString();
        assertEquals(expectedResult, str);
    }
}
