package org.nixos.idea.psi;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Objects;

final class NixInjectionPerformerTest {

    @Test
    void trivial_ind_string() throws Exception {
        doTest(
                "''first ''${<edit>} line''",
                "first ${<edit>} line"
        );
    }

    @Test
    void trivial_std_string() throws Exception {
        doTest(
                "\"first \\${<edit>} line\"",
                "first ${<edit>} line"
        );
    }

    @Test
    void trim_initial_line() throws Exception {
        doTest(
                "''\nfirst<edit> line''",
                "first<edit> line"
        );
    }

    @Test
    void trim_indentation() throws Exception {
        doTest(
                """
                        ''
                          first line

                            second<edit> line
                           \s
                          third line
                        ''
                        """,
                """
                        first line

                          second<edit> line
                         \s
                        third line
                        """
        );
    }

    @Test
    void keep_indentation_of_closing_quotes() throws Exception {
        doTest(
                """
                          ''
                            first line
                          ''
                        """,
                """
                        first line
                        """
        );
    }

    @Test
    void insert_into_empty_line() throws Exception {
        // TODO remove spaces from empty line in source code.
        //  This test is supposed to test a scenario where the edited line misses the indent in the host language
        doTest(
                """
                        ''
                          first line
                          <edit>
                          second line
                        ''
                        """,
                """
                        first line
                        <edit>
                        second line
                        """
        );
    }

    @Test
    void insert_into_string() throws Exception {
        // TODO remove spaces from empty line in source code.
        //  This test is supposed to test a scenario where the edited line misses the indent in the host language
        doTest(
                """
                        ''
                          <edit>
                        ''
                        """,
                """
                        first line
                        <edit>
                        second line
                        """
        );
    }

    @Test
    void insert_before_interpolation() throws Exception {
        doTest(
                "''<edit>${x}''",
                "<edit><interpolation>"
        );
    }

    @Test
    void insert_after_interpolation() throws Exception {
        doTest(
                "''${x}<edit>''",
                "<interpolation><edit>"
        );
    }

    @Test
    void insert_between_interpolations() throws Exception {
        doTest(
                "''${x}<edit>${y}''",
                "<interpolation><edit><interpolation>"
        );
    }

    private void doTest(@NotNull String sourceCode, @NotNull String injected) throws Exception {
        IdeaTestFixtureFactory factory = IdeaTestFixtureFactory.getFixtureFactory();
        IdeaTestExecutionPolicy policy = Objects.requireNonNull(IdeaTestExecutionPolicy.current());
        IdeaProjectTestFixture baseFixture = factory.createLightFixtureBuilder("test").getFixture();
        CodeInsightTestFixture fixture = factory.createCodeInsightFixture(baseFixture, policy.createTempDirTestFixture());
        fixture.setTestDataPath(FileUtil.toSystemIndependentName(policy.getHomePath()));
        fixture.setUp();
        try {
            // TODO Implement test. This test is supposed to
            //  1. Enable a language injection.
            //  2. Open the injected language in the editor.
            //  3. Insert text at <edit> in the editor of the injected language
            //  4. Remove text at <edit> again
            //  5. Insert another text at <edit> which needs to be escaped
            //  6. Verify that during step 3 to 5, the text in the editor of the host language is adjusted appropriately
            // How to enable language injection using code?
            // There doesn't seem to be an action I could use via fixture.performEditorAction(IdeActions...).
            // Maybe I can use TemporaryPlacesRegistry.addHostWithUndo directly, but it is not available in the classpath right now.
            InjectedLanguageManager instance = InjectedLanguageManager.getInstance(fixture.getProject());
            // instance.getNonEditableFragments(); This seems like it could be used to detect the interpolations
            int pos = sourceCode.indexOf("<edit>");
            fixture.setCaresAboutInjection(true); // This doesn't do anything as true is the default, but I left it here for now.
            fixture.configureByText("test.nix", sourceCode.replace("<edit>", "<caret>"));
        } finally {
            EdtTestUtil.runInEdtAndWait(fixture::tearDown);
        }
    }
}
