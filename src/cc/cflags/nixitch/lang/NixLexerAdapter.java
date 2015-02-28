package cc.cflags.nixitch.lang;

import com.intellij.lexer.FlexAdapter;

import java.io.Reader;

public class NixLexerAdapter extends FlexAdapter {
    public NixLexerAdapter() {
        super(new NixLexer((Reader) null));
    }
}

