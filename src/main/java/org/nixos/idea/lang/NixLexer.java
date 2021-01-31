package org.nixos.idea.lang;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import java.io.IOException;

public class NixLexer extends FlexAdapter {
    // todo: Implement RestartableLexer when it becomes non-experimental. See
    //  https://intellij-support.jetbrains.com/hc/en-us/community/posts/360010305800/comments/360002861979

    public NixLexer() {
        // We need this anonymous wrapper class to map between internal states
        // of Flex and external states for IntelliJ. See
        // https://intellij-support.jetbrains.com/hc/en-us/community/posts/360010305800
        super(new FlexLexer() {
            private final _NixLexer lexer = new _NixLexer(null);

            @Override
            public void yybegin(int state) {
                lexer.restoreState(state);
            }

            @Override
            public int yystate() {
                return lexer.getStateIndex();
            }

            @Override
            public int getTokenStart() {
                return lexer.getTokenStart();
            }

            @Override
            public int getTokenEnd() {
                return lexer.getTokenEnd();
            }

            @Override
            public IElementType advance() throws IOException {
                return lexer.advance();
            }

            @Override
            public void reset(CharSequence buf, int start, int end, int initialState) {
                lexer.reset(buf, start, end, _NixLexer.YYINITIAL);
                lexer.restoreState(initialState);
            }
        });
    }
}
