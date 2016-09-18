package org.nixos.idea.lang;

import com.intellij.lexer.FlexAdapter;

public class NixLexerAdapter extends FlexAdapter {
    public NixLexerAdapter() {
        super(new NixLexer(null));
    }
}

