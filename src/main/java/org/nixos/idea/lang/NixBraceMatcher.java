package org.nixos.idea.lang;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.psi.NixTypes;

public class NixBraceMatcher implements PairedBraceMatcher {
    // Grammar-Kit uses the first pair of this array to guide the error recovery
    // (even when structural is set to false). Since the lexer tracks curly
    // braces for its state transitions, the curly braces must be on top to keep
    // the state of parser and lexer consistent. See
    // https://intellij-support.jetbrains.com/hc/en-us/community/posts/360010379000
    public static final BracePair[] PAIRS = new BracePair[] {
      new BracePair(NixTypes.LCURLY,NixTypes.RCURLY,true),
      new BracePair(NixTypes.LBRAC,NixTypes.RBRAC,false),
      new BracePair(NixTypes.LPAREN,NixTypes.RPAREN,false),
      new BracePair(NixTypes.IND_STRING_OPEN,NixTypes.IND_STRING_CLOSE,false),
      new BracePair(NixTypes.STRING_OPEN,NixTypes.STRING_CLOSE,false)
    };

    @Override
    public BracePair @NotNull [] getPairs() {
        return PAIRS;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
        return true;
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        PsiElement openingBrace = file.findElementAt(openingBraceOffset);
        if (openingBrace != null && openingBrace.getNode().getElementType() == NixTypes.LCURLY) {
            PsiElement previousToken = openingBrace.getPrevSibling();
            if (previousToken != null && previousToken.getNode().getElementType() == NixTypes.DOLLAR) {
                return openingBraceOffset - 1;
            }
            else {
                return openingBraceOffset;
            }
        }
        else {
            return openingBraceOffset;
        }
    }
}


