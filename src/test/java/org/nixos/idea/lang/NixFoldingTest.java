package org.nixos.idea.lang;

import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.nixos.idea._testutil.WithIdeaPlatform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;


@WithIdeaPlatform.CodeInsight
class NixFoldingTest {
    static final String DIRECTORY_NAME = "FoldingTest";


    static String getTestDataPath() {
        return IdeaTestExecutionPolicy.getHomePathWithPolicy() + "/" + DIRECTORY_NAME;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("filePaths")
    void check(String filePath, CodeInsightTestFixture myfixture) {
        myfixture.testFolding(getTestDataPath() + "/" + filePath);
    }

    public static Iterable<String> filePaths() throws IOException {
        var baseDir = Paths.get(getTestDataPath());
        var fileStream = Files.find(baseDir,
                Integer.MAX_VALUE,
                (path1, attributes) -> attributes.isRegularFile() && path1.toString().endsWith(".nix"));

        try (fileStream) {
            return fileStream.map(path -> baseDir.relativize(path).toString()).collect(Collectors.toList());
        }
    }
}


