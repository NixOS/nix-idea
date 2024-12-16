package org.nixos.idea._testutil;

import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

public final class TestFixtureUtil {

    private TestFixtureUtil() {} // Cannot be instantiated.

    public static void expect(@NotNull CodeInsightTestFixture fixture, @Language("HTML") String code) {
        // TODO validating current selection is not supported right now
        code = code.replaceAll("</?selection>", "");

        int caretMarker = code.indexOf(CodeInsightTestFixture.CARET_MARKER);
        if (caretMarker >= 0) {
            code = code.substring(0, caretMarker) +
                   code.substring(caretMarker + CodeInsightTestFixture.CARET_MARKER.length());
        }
        Assertions.assertEquals(code, fixture.getEditor().getDocument().getText());
        if (caretMarker >= 0) {
            Assertions.assertEquals(caretMarker, fixture.getCaretOffset());
        }
    }
}
