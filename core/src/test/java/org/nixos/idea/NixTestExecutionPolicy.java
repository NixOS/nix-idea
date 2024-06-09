package org.nixos.idea;

import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy;

/**
 * Test execution policy referenced by the build setup build.gradle.kts.
 * Tests like {@link org.nixos.idea.lang.LexerFilesTest} use it to locate the test data directory.
 */
@SuppressWarnings("unused")
public class NixTestExecutionPolicy extends IdeaTestExecutionPolicy {
    @Override
    protected String getName() {
        return "Nix";
    }

    @Override
    public String getHomePath() {
        return System.getProperty("plugin.testDataPath");
    }
}
