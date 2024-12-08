package org.nixos.idea.lang;

import com.intellij.codeInsight.editorActions.MultiCharQuoteHandler;
import com.intellij.codeInsight.editorActions.QuoteHandler;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.psi.NixTypes;

/**
 * Quote handler for the Nix Language.
 * This class handles the automatic insertion of closing quotes after the user enters opening quotes.
 * The methods are called in the following order whenever the user enters a quote character.
 * <ol>
 *     <li>{@link #isClosingQuote(HighlighterIterator, int)} is only called if the typed quote character already exists just behind the caret.
 *         If the method returns {@code true}, the insertion of the quote and all further methods will be skipped.
 *         The caret will just move over the existing quote one character to the right.
 *     <li><em>*Insert quote character.*</em>
 *     <li>Handling of {@link MultiCharQuoteHandler}:<ol>
 *         <li>{@link #getClosingQuote(HighlighterIterator, int)} is called with the offset behind the inserted quote.
 *             The returned value represents the string which shall be inserted.
 *             Can return {@code null} to skipp further processing of {@code MultiCharQuoteHandler}.
 *         <li>{@link #hasNonClosedLiteral(Editor, HighlighterIterator, int)} is called with the offset before the inserted quote.
 *             The closing quotes returned by the previous method will only be inserted if this method returns {@code true}.
 *         <li><em>*Insert closing quotes as returned by {@code getClosingQuote(...)}.*</em>
 *     </ol>
 *     <li>Standard handling of {@link QuoteHandler} (skipped if closing quotes were already inserted):<ol>
 *         <li>{@link #isOpeningQuote(HighlighterIterator, int)} is called with the offset before the inserted quote.
 *             The following steps will only be executed if this method returns {@code true}.
 *         <li>{@link #hasNonClosedLiteral(Editor, HighlighterIterator, int)} is called with the offset before the inserted quote.
 *             The following steps will only be executed if this method returns {@code true}.
 *         <li><em>*Insert same quote character as initially typed by the user again.*</em>
 *     </ol>
 * </ol>
 *
 * @see <a href="https://plugins.jetbrains.com/docs/intellij/additional-minor-features.html#quote-handling">Quote Handling Documentation</a>
 */
public final class NixQuoteHandler implements MultiCharQuoteHandler {

    private static final TokenSet CLOSING_QUOTE = TokenSet.create(NixTypes.STRING_CLOSE, NixTypes.IND_STRING_CLOSE);
    private static final TokenSet OPENING_QUOTE = TokenSet.create(NixTypes.STRING_OPEN, NixTypes.IND_STRING_OPEN);
    private static final TokenSet STRING_CONTENT = TokenSet.create(NixTypes.STR, NixTypes.STR_ESCAPE, NixTypes.IND_STR, NixTypes.IND_STR_ESCAPE);
    private static final TokenSet STRING_ANY = TokenSet.orSet(CLOSING_QUOTE, OPENING_QUOTE, STRING_CONTENT);

    @Override
    public boolean isClosingQuote(HighlighterIterator iterator, int offset) {
        return CLOSING_QUOTE.contains(iterator.getTokenType());
    }

    @Override
    public boolean isOpeningQuote(HighlighterIterator iterator, int offset) {
        // This method comes from QuoteHandler and assumes the quote is only one char in size.
        // We therefore ignore indented strings ('') in this method.
        // Note that this method is not actually used for the insertion of closing quotes,
        // as that is already handled by MultiCharQuoteHandler.getClosingQuote(...). See class documentation.
        // However, this method is also called by BackspaceHandler to delete the closing quotes of an empty string
        // when the opening quotes are removed.
        return NixTypes.STRING_OPEN == iterator.getTokenType();
    }

    @Override
    public boolean hasNonClosedLiteral(Editor editor, HighlighterIterator iterator, int offset) {
        IElementType openingToken = iterator.getTokenType();
        if (iterator.getEnd() != offset + 1) {
            return false; // The caret isn't behind the opening quote.
        } else if (openingToken == NixTypes.STRING_OPEN) {
            // Insert closing quotes only if we would otherwise get a non-closed string at the end of the line.
            Document doc = editor.getDocument();
            int lineEnd = doc.getLineEndOffset(doc.getLineNumber(offset));
            while (true) {
                IElementType lastToken = iterator.getTokenType();
                iterator.advance();
                if (iterator.atEnd() || iterator.getStart() >= lineEnd) {
                    return STRING_ANY.contains(lastToken) && !CLOSING_QUOTE.contains(lastToken);
                }
            }
        } else if (openingToken == NixTypes.IND_STRING_OPEN) {
            // Insert closing quotes only if we would otherwise get a non-closed string at the end of the file.
            while (true) {
                IElementType lastToken = iterator.getTokenType();
                iterator.advance();
                if (iterator.atEnd()) {
                    return STRING_ANY.contains(lastToken) && !CLOSING_QUOTE.contains(lastToken);
                }
            }
        }
        return false;
    }

    @Override
    public boolean isInsideLiteral(HighlighterIterator iterator) {
        // Not sure why we need this. It seems to enable some special handling for escape sequences in IDEA.
        return STRING_ANY.contains(iterator.getTokenType());
    }

    @Override
    public @Nullable CharSequence getClosingQuote(@NotNull HighlighterIterator iterator, int offset) {
        // May need to retreat iterator by one token.
        // In contrast to all the other methods, this method is called with the offset behind the inserted quote.
        // However, the iterator may already be at the right location if the offset is at the end of the file.
        if (iterator.getEnd() != offset) {
            iterator.retreat();
            if (iterator.atEnd()) {
                return null; // There was no previous token
            }
        }
        IElementType tokenType = iterator.getTokenType();
        return tokenType == NixTypes.STRING_OPEN ? "\"" :
                tokenType == NixTypes.IND_STRING_OPEN ? "''" :
                        null;
    }
}
