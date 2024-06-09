package org.nixos.idea.lang;

import com.intellij.lexer.Lexer;
import com.intellij.testFramework.LexerTestCase;
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * Parameterized lexer test for all Nix files inside src/test/testData/ParsingTest/.
 */
@RunWith(Parameterized.class)
public class LexerFilesTest extends LexerTestCase {
    private static final String DIRECTORY_NAME = "ParsingTest";

    private final String filePath;

    public LexerFilesTest(@NotNull String filePath) {
        this.filePath = filePath;
    }

    /**
     * Collects all the files to run this test with.
     *
     * @return Iterable of files src/test/testData/ParsingTest/*.nix.
     */
    @Parameterized.Parameters(name = "{0}")
    public static Iterable<String> filePaths() throws IOException {
        var baseDir = Paths.get(IdeaTestExecutionPolicy.getHomePathWithPolicy()).resolve(DIRECTORY_NAME);
        var fileStream = Files.find(baseDir,
                Integer.MAX_VALUE,
                (path1, attributes) -> attributes.isRegularFile() && path1.toString().endsWith(".nix"));

        try (fileStream) {
            return fileStream.map(path -> baseDir.relativize(path).toString()).collect(Collectors.toList());
        }
    }

    @Test
    public void check() {
        doFileTest("nix");
    }

    @Override
    protected Lexer createLexer() {
        return new NixLexer();
    }

    @Override
    protected String getDirPath() {
        return DIRECTORY_NAME;
    }

    @Override
    protected @NotNull String getTestName(boolean lowercaseFirstLetter) {
        return filePath.replace(".nix", "");
    }

    @Override
    protected @NotNull String getExpectedFileExtension() {
        return ".lexer.txt";
    }
}
