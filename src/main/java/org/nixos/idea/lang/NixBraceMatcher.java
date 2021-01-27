package org.nixos.idea.lang;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.psi.NixTypes;

public class NixBraceMatcher implements PairedBraceMatcher {
    public static final BracePair[] PAIRS = new BracePair[] {
      new BracePair(NixTypes.LBRAC,NixTypes.RBRAC,false),
      new BracePair(NixTypes.LPAREN,NixTypes.RPAREN,false),
      new BracePair(NixTypes.LCURLY,NixTypes.RCURLY,false),
      new BracePair(NixTypes.DOLLAR_CURLY,NixTypes.RCURLY,true),
      new BracePair(NixTypes.IND_STRING_OPEN,NixTypes.IND_STRING_CLOSE,true),
      new BracePair(NixTypes.STRING_OPEN,NixTypes.STRING_CLOSE,false)
    };

    @Override
    public BracePair[] getPairs() {
        return PAIRS;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType iElementType, @Nullable IElementType iElementType1) {
        return false;
    }

    @Override
    public int getCodeConstructStart(PsiFile psiFile, int i) {
        return 0;
    }
}


