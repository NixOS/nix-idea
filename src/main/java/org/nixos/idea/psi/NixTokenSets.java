package org.nixos.idea.psi;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.TokenSet;

public final class NixTokenSets {

    /** All tokens representing whitespaces. */
    public static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);

    /** All tokens representing comments. */
    public static final TokenSet COMMENTS = TokenSet.create(NixTypes.SCOMMENT, NixTypes.MCOMMENT);

    /** Elements representing string literals. Note that these types are used for non-leaf elements, they don't represent tokens. */
    public static final TokenSet STRING_LITERALS = TokenSet.create(NixTypes.STD_STRING, NixTypes.IND_STRING);

    /** All token types which represent a keyword. */
    public static final TokenSet KEYWORDS = TokenSet.create(
            NixTypes.IF,
            NixTypes.THEN,
            NixTypes.ELSE,
            NixTypes.ASSERT,
            NixTypes.WITH,
            NixTypes.LET,
            NixTypes.IN,
            NixTypes.REC,
            NixTypes.INHERIT,
            NixTypes.OR_KW);

    /** All tokens representing opening quotes. */
    public static final TokenSet OPENING_QUOTES = TokenSet.create(NixTypes.STRING_OPEN, NixTypes.IND_STRING_OPEN);
    /** All tokens representing closing quotes. */
    public static final TokenSet CLOSING_QUOTES = TokenSet.create(NixTypes.STRING_CLOSE, NixTypes.IND_STRING_CLOSE);
    /** All tokens representing text inside a string. */
    public static final TokenSet STRING_CONTENT = TokenSet.create(NixTypes.STR, NixTypes.STR_ESCAPE, NixTypes.IND_STR, NixTypes.IND_STR_ESCAPE);
    /** All tokens representing any part of a string, except interpolations. */
    public static final TokenSet STRING_ANY = TokenSet.orSet(CLOSING_QUOTES, OPENING_QUOTES, STRING_CONTENT);

    /** Tokens which collapse with an ID on either side if they were not separated by whitespace. */
    public static final TokenSet MIGHT_COLLAPSE_WITH_ID = TokenSet.orSet(
            KEYWORDS,
            TokenSet.create(
                    NixTypes.ID,
                    NixTypes.SPATH,
                    NixTypes.PATH_SEGMENT,
                    NixTypes.PATH_END,
                    NixTypes.URI));

    /** Tokens which would collapse with an ID on the left if they were not separated by whitespace. */
    public static final TokenSet MIGHT_COLLAPSE_WITH_ID_ON_THE_LEFT = TokenSet.orSet(
            MIGHT_COLLAPSE_WITH_ID,
            TokenSet.create(
                    NixTypes.MINUS, // a-b is parsed as one identifier, not as a - b.
                    NixTypes.INT,
                    NixTypes.FLOAT));

    private NixTokenSets() {} // Cannot be instantiated
}
