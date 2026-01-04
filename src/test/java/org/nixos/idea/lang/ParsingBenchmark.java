package org.nixos.idea.lang;

import com.intellij.testFramework.TestDataFile;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.nixos.idea._testutil.WithIdeaPlatform;
import org.nixos.idea._testutil._benchmark.Benchmark;

@WithIdeaPlatform.OnEdt
@WithIdeaPlatform.CodeInsight(basePath = "ParsingBenchmark")
final class ParsingBenchmark {

    private final CodeInsightTestFixture myFixture;

    ParsingBenchmark(CodeInsightTestFixture fixture) {
        myFixture = fixture;
    }

    @Benchmark
    void issue95() {
        parse("Issue95.nix");
    }

    @Benchmark
    void recursiveMissingSemicolon() {
        parse("RecursiveMissingSemicolon.nix");
    }

    private void parse(@TestDataFile String filePath) {
        myFixture.configureByFile(filePath);
    }

}
