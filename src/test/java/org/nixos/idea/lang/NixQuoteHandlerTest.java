package org.nixos.idea.lang;

import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nixos.idea._testutil.WithIdeaPlatform;
import org.nixos.idea.file.NixFileType;

@WithIdeaPlatform.OnEdt
@WithIdeaPlatform.CodeInsight
final class NixQuoteHandlerTest {

    private final CodeInsightTestFixture myFixture;

    NixQuoteHandlerTest(CodeInsightTestFixture fixture) {
        myFixture = fixture;
    }

    @Test
    void insertClosingQuotesForStandardString() {
        setup("<caret>");
        type('"');
        expect("\"<caret>\"");
    }

    @Test
    void insertClosingQuotesForIndentedString() {
        setup("<caret>");
        type('\'');
        expect("'<caret>");
        type('\'');
        expect("''<caret>''");
    }

    @Test
    void insertClosingQuotesForIndentedStringMidFile() {
        // Requires special case in NixQuoteHandler.getClosingQuote
        setup("<caret> x");
        type('\'');
        expect("'<caret> x");
        type('\'');
        expect("''<caret>'' x");
    }

    @Test
    void doNotInsertClosingQuotesWhenTypingMidQuote() {
        setup("<caret>'");
        type('\'');
        expect("'<caret>'");
    }

    @Test
    void doNotInsertClosingQuotesForStandardStringWhenTheyAlreadyExistInSameLine() {
        setup("<caret> x \"");
        type('"');
        expect("\"<caret> x \"");
    }

    @Test
    void doNotInsertClosingQuotesForIndentedStringWhenTheyAlreadyExist() {
        setup("'<caret> x \n''");
        type('\'');
        expect("''<caret> x \n''");
    }

    @Test
    void typeOverClosingQuotesForStandardString() {
        setup("\"<caret>\"");
        type('"');
        expect("\"\"<caret>");
    }

    @Test
    void typeOverClosingQuotesForIndentedString() {
        setup("''<caret>''");
        type('\'');
        expect("'''<caret>'");
        type('\'');
        expect("''''<caret>");
    }

    @Test
    void backspaceInEmptyStandardString() {
        setup("\"<caret>\"");
        backspace();
        expect("");
    }

    @Test
    void backspaceInEmptyIndentedString() {
        // Would be nice if we could delete the closing quotes as well, but it is currently not implemented.
        // This test verifies that we are at least not ending up with a single quote character (').
        // See NixQuoteHandler.isOpeningQuote(...)
        setup("''<caret>''");
        backspace();
        expect("'<caret>''");
        backspace();
        expect("<caret>''");
    }

    private void setup(@Language("HTML") String code) {
        myFixture.configureByText(NixFileType.INSTANCE, code);
    }

    private void type(char character) {
        myFixture.type(character);
    }

    private void backspace() {
        myFixture.type('\b');
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
