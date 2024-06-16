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

final class NixIndStringUtilTest {
//    @ParameterizedTest(name = "[{index}] {0} -> {1}")
//    @CsvSource(textBlock = """
//            ''              , ""
//            abc             , "abc"
//            "               , "\\\""
//            \\              , "\\\\"
//            \\x             , "\\\\x"
//            a${b}c          , "a\\${b}c"
//            '\n'            , "\\n"
//            '\r'            , "\\r"
//            '\t'            , "\\t"
//            # supplementary character, i.e. character form a supplementary plane,
//            # which needs a surrogate pair to be represented in UTF-16
//            \uD83C\uDF09    , "\uD83C\uDF09"
//            """)
//    void quote(String unescaped, String expectedResult) {
//        assertEquals(expectedResult, NixStringUtil.quote(unescaped));
//    }

    @ParameterizedTest(name = "[{index}] {0} -> {1}")
    @CsvSource(textBlock = """
            ''              , ''
            abc             , abc
            "               , \\"
            \\              , \\\\
            \\x             , \\\\x
            a${b}c          , a\\${b}c
            '\n'            , \\n
            '\r'            , \\r
            '\t'            , \\t
            # supplementary character, i.e. character form a supplementary plane,
            # which needs a surrogate pair to be represented in UTF-16
            \uD83C\uDF09    , \uD83C\uDF09
            """)
    void escape(String unescaped, String expectedResult) {
        var sb = new StringBuilder();
        NixIndStringUtil.INSTANCE.escape(sb, unescaped);
        assertEquals(expectedResult, sb.toString());
    }

    @ParameterizedTest(name = "[{index}] {0} -> {1}")
    @CsvSource(quoteCharacter = '|', textBlock = """
            ""              , ||
            "x"             , x
            ''abc''         , abc
            "\\""           , "
            "\\\\"          , \\
            "\\\\x"         , \\x
            ''\\"''         , \\"
            ''\\\\''        , \\\\
            ''\\\\x''       , \\\\x
            ''''\\"''       , "
            ''''\\\\''      , \\
            ''''\\\\x''     , \\x
            ''  '''  ''     , |  '''|
            "''\\""         , ''"
            "a\\${b}c"      , a${b}c
            ''a''${b}c''    , a${b}c
            ''a''\\${b}c''  , a${b}c
            "a$${b}c"       , a$${b}c
            ''a$${b}c''     , a$${b}c
            |"\n"|          , |\n|
            |"\r"|          , |\r|
            |"\t"|          , |\t|
            # supplementary character, i.e. character form a supplementary plane,
            # which needs a surrogate pair to be represented in UTF-16
            "\uD83C\uDF09"  , \uD83C\uDF09
            ''\uD83C\uDF09'', \uD83C\uDF09
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
