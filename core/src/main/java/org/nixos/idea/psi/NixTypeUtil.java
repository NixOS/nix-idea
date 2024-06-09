package org.nixos.idea.psi;

import com.intellij.psi.tree.TokenSet;

public final class NixTypeUtil {

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

    /** Tokens would collapse if they were not separated by whitespace. */
    public static final TokenSet MIGHT_COLLAPSE_WITH_ID = TokenSet.orSet(
            KEYWORDS,
            TokenSet.create(
                    NixTypes.ID,
                    NixTypes.INT,
                    NixTypes.FLOAT,
                    NixTypes.SPATH,
                    NixTypes.PATH_SEGMENT,
                    NixTypes.PATH_END,
                    NixTypes.URI));

    private NixTypeUtil() {} // Cannot be instantiated
}
