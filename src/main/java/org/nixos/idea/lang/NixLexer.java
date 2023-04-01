package org.nixos.idea.lang;

import com.intellij.lexer.FlexAdapter;

public class NixLexer extends FlexAdapter {
    // todo: Implement RestartableLexer when it becomes non-experimental. See
    //  https://intellij-support.jetbrains.com/hc/en-us/community/posts/360010305800/comments/360002861979

    public NixLexer() {
        super(new _NixLexer(null) {
            @Override
            public void reset(CharSequence buffer, int start, int end, int initialState) {
                onReset();
                super.reset(buffer, start, end, initialState);
            }
        });
    }
}
