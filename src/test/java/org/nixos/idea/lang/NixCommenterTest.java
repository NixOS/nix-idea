package org.nixos.idea.lang;

import com.intellij.codeInsight.generation.actions.CommentByBlockCommentAction;
import com.intellij.codeInsight.generation.actions.CommentByLineCommentAction;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.nixos.idea.file.NixFileType;

public class NixCommenterTest extends BasePlatformTestCase {

    public void testLineComment() {
        myFixture.configureByText(NixFileType.INSTANCE, "<caret>services.nginx.enable = true;");

        CommentByLineCommentAction commentAction = new CommentByLineCommentAction();
        commentAction.actionPerformedImpl(getProject(), myFixture.getEditor());
        myFixture.checkResult("#services.nginx.enable = true;");
        commentAction.actionPerformedImpl(getProject(), myFixture.getEditor());
        myFixture.checkResult("services.nginx.enable = true;");
    }

    public void testBlockComment() {
        myFixture.configureByText(NixFileType.INSTANCE, "<selection><caret>services.nginx.enable = true;</selection>\n");

        CommentByBlockCommentAction commentAction = new CommentByBlockCommentAction();
        commentAction.actionPerformedImpl(getProject(), myFixture.getEditor());
        myFixture.checkResult("/*services.nginx.enable = true;*/\n");
        commentAction.actionPerformedImpl(getProject(), myFixture.getEditor());
        myFixture.checkResult("services.nginx.enable = true;\n");
    }
}