package org.nixos.idea.lang;

import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.nixos.idea._testutil.WithIdeaPlatform;
import org.nixos.idea.file.NixFileType;

@WithIdeaPlatform.OnEdt
@WithIdeaPlatform.CodeInsight
final class NixMoveElementLeftRightHandlerTest {

    private final CodeInsightTestFixture myFixture;

    NixMoveElementLeftRightHandlerTest(CodeInsightTestFixture fixture) {
        myFixture = fixture;
    }

    @Nested
    final class List {
        @Test
        void moveLeft() {
            setup("[a b c d e f<caret>]");
            doMoveLeft();
            expect("[a b c d f<caret> e]");
            doMoveLeft();
            expect("[a b c f<caret> d e]");
            doMoveLeft();
            expect("[a b f<caret> c d e]");
            doMoveLeft();
            expect("[a f<caret> b c d e]");
            doMoveLeft();
            expect("[f<caret> a b c d e]");
            doMoveLeft();
            expect("[f<caret> a b c d e]");
        }

        @Test
        void moveRight() {
            setup("[ <caret>a b c d ]");
            doMoveRight();
            expect("[ b <caret>a c d ]");
            doMoveRight();
            expect("[ b c <caret>a d ]");
            doMoveRight();
            expect("[ b c d <caret>a ]");
            doMoveRight();
            expect("[ b c d <caret>a ]");
        }
    }

    @Nested
    final class BindInherit {
        @Test
        void moveLeft() {
            setup("{ inherit (x) a b <caret>c; }");
            doMoveLeft();
            expect("{ inherit (x) a <caret>c b; }");
            doMoveLeft();
            expect("{ inherit (x) <caret>c a b; }");
            doMoveLeft();
            expect("{ inherit (x) <caret>c a b; }");
        }

        @Test
        void moveRight() {
            setup("{ inherit (x) a<caret> b c; }");
            doMoveRight();
            expect("{ inherit (x) b a<caret> c; }");
            doMoveRight();
            expect("{ inherit (x) b c a<caret>; }");
            doMoveRight();
            expect("{ inherit (x) b c a<caret>; }");
        }
    }

    @Nested
    final class Attrs {
        @Test
        void moveLeft() {
            setup("{ a = A; b = B; c <caret>= C; }");
            doMoveLeft();
            expect("{ a = A; c <caret>= C; b = B; }");
            doMoveLeft();
            expect("{ c <caret>= C; a = A; b = B; }");
            doMoveLeft();
            expect("{ c <caret>= C; a = A; b = B; }");
        }

        @Test
        void moveRight() {
            setup("rec { a =<caret> A; b = B; c = C; }");
            doMoveRight();
            expect("rec { b = B; a =<caret> A; c = C; }");
            doMoveRight();
            expect("rec { b = B; c = C; a =<caret> A; }");
            doMoveRight();
            expect("rec { b = B; c = C; a =<caret> A; }");
        }
    }

    @Nested
    final class Let {
        @Test
        void moveLeft() {
            setup("let a = A; b = B; c <caret>= C; in _");
            doMoveLeft();
            expect("let a = A; c <caret>= C; b = B; in _");
            doMoveLeft();
            expect("let c <caret>= C; a = A; b = B; in _");
            doMoveLeft();
            expect("let c <caret>= C; a = A; b = B; in _");
        }

        @Test
        void moveRight() {
            setup("let a =<caret> A; b = B; c = C; in _");
            doMoveRight();
            expect("let b = B; a =<caret> A; c = C; in _");
            doMoveRight();
            expect("let b = B; c = C; a =<caret> A; in _");
            doMoveRight();
            expect("let b = B; c = C; a =<caret> A; in _");
        }
    }

    @Nested
    final class Lambda {
        @Test
        void moveLeft() {
            setup("{ x } @ a<caret>: _");
            doMoveLeft();
            expect("a @ { x }: _");
            doMoveLeft();
            expect("a @ { x }: _");
        }

        @Test
        void moveRight() {
            setup("a<caret> @ { x, y }: _");
            doMoveRight();
            expect("{ x, y } @ a<caret>: _");
            doMoveRight();
            expect("{ x, y } @ a<caret>: _");
        }
    }

    @Nested
    final class LambdaFormals {
        @Test
        void moveLeft() {
            setup("{ a, b, c<caret> } @ x: _");
            doMoveLeft();
            expect("{ a, c<caret>, b } @ x: _");
            doMoveLeft();
            expect("{ c<caret>, a, b } @ x: _");
            doMoveLeft();
            expect("{ c<caret>, a, b } @ x: _");
        }

        @Test
        void moveRight() {
            setup("{ a<caret>, b, c } @ x: _");
            doMoveRight();
            expect("{ b, a<caret>, c } @ x: _");
            doMoveRight();
            expect("{ b, c, a<caret> } @ x: _");
            doMoveRight();
            expect("{ b, c, a<caret> } @ x: _");
        }
    }

    @Nested
    final class App {
        @Test
        void doNotMoveFunction() {
            setup("f<caret> a b c");
            doMoveLeft();
            expect("f<caret> a b c");
        }

        @Test
        void moveLeft() {
            setup("f a b <caret>c");
            doMoveLeft();
            expect("f a <caret>c b");
            doMoveLeft();
            expect("f <caret>c a b");
            doMoveLeft();
            expect("f <caret>c a b");
        }

        @Test
        void moveRight() {
            setup("f <caret>a b c");
            doMoveRight();
            expect("f b <caret>a c");
            doMoveRight();
            expect("f b c <caret>a");
            doMoveRight();
            expect("f b c <caret>a");
        }
    }

    private void setup(@Language("HTML") String code) {
        myFixture.configureByText(NixFileType.INSTANCE, code);
    }

    private void doMoveLeft() {
        myFixture.performEditorAction(IdeActions.MOVE_ELEMENT_LEFT);
    }

    private void doMoveRight() {
        myFixture.performEditorAction(IdeActions.MOVE_ELEMENT_RIGHT);
    }

    private void expect(@Language("HTML") String code) {
        int caretMarker = code.indexOf(CodeInsightTestFixture.CARET_MARKER);
        if (caretMarker >= 0) {
            code = code.substring(0, caretMarker) +
                   code.substring(caretMarker + CodeInsightTestFixture.CARET_MARKER.length());
        }
        Assertions.assertEquals(code, myFixture.getEditor().getDocument().getText());
        if (caretMarker >= 0) {
            Assertions.assertEquals(caretMarker, myFixture.getCaretOffset());
        }
    }
}
