package org.nixos.idea.lang.references;

import com.intellij.find.usages.api.PsiUsage;
import com.intellij.find.usages.api.Usage;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.nixos.idea._testutil.Markers;
import org.nixos.idea._testutil.WithIdeaPlatform;
import org.nixos.idea.file.NixFileType;
import org.nixos.idea.lang.builtins.NixBuiltin;
import org.nixos.idea.lang.references.symbol.NixSymbol;
import org.nixos.idea.settings.NixSymbolSettings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@WithIdeaPlatform.OnEdt
@WithIdeaPlatform.CodeInsight
final class SymbolNavigationTest {

    private static final @NotNull Markers.TagName TAG_REF = Markers.tagName("ref");
    private static final @NotNull Markers.TagName TAG_DECL = Markers.tagName("decl");

    private final @NotNull CodeInsightTestFixture myFixture;
    private final @NotNull SymbolTestHelper mySymbolHelper;

    SymbolNavigationTest(@NotNull CodeInsightTestFixture fixture) {
        myFixture = fixture;
        mySymbolHelper = new SymbolTestHelper(fixture);
    }

    @BeforeEach
    void setUp() {
        NixSymbolSettings.getInstance().setJumpToFirstDeclaration(false);
    }

    @TestFactory
    Stream<DynamicNode> simple_assignment() {
        return createTests("""
                let <decl>some-variable</decl> = "..."; in
                <ref>some-variable</ref>
                """, "some-variable");
    }

    @TestFactory
    Stream<DynamicNode> child_assignment() {
        return createTests("""
                let <decl>some-variable</decl>.abc = "..."; in
                <ref>some-variable</ref>
                """, "some-variable");
    }

    @TestFactory
    Stream<DynamicNode> simple_inherit() {
        return createTests("""
                let inherit (unknown-value) <decl>some-variable</decl>; in
                <ref>some-variable</ref>
                """, "some-variable");
    }

    @TestFactory
    Stream<DynamicNode> recursive_inherit() {
        return createTests("""
                let <decl>some-variable</decl> = "..."; in
                let inherit <ref>some-variable</ref>; in
                <ref>some-variable</ref>
                """, "some-variable");
    }

    @TestFactory
    Stream<DynamicNode> from_inherit() {
        return createTests("""
                let <decl>some-variable</decl> = "..."; in
                { inherit <ref>some-variable</ref>; }
                """, "some-variable");
    }

    @TestFactory
    Stream<DynamicNode> simple_parameter() {
        return createTests("""
                <decl>some-variable</decl>:
                <ref>some-variable</ref>
                """, "some-variable");
    }

    @TestFactory
    Stream<DynamicNode> formal_parameter() {
        return createTests("""
                { <decl>some-variable</decl> ? "..." }:
                <ref>some-variable</ref>
                """, "some-variable");
    }

    @TestFactory
    Stream<DynamicNode> multiple_declarations() {
        return createTests("""
                let
                  <decl>some-variable</decl>.aaa = "...";
                  <decl>some-variable</decl>.bbb = "...";
                  <decl>some-variable</decl>.x.y = "...";
                in
                  <ref>some-variable</ref>
                """, "some-variable");
    }

    @TestFactory
    Stream<DynamicNode> conflicting_declarations() {
        return createTests("""
                let
                  inherit (unknown-value) <decl>some-variable</decl>;
                  <decl>some-variable</decl> = "...";
                in
                  <ref>some-variable</ref>
                """, "some-variable");
    }

    @TestFactory
    Stream<DynamicNode> shadowed_by_assignment() {
        return createTests("""
                let some-variable = "..."; in
                let inherit (unknown-value) some-variable; in
                let <decl>some-variable</decl> = "..."; in
                <ref>some-variable</ref>
                """, "some-variable");
    }

    @TestFactory
    Stream<DynamicNode> shadowed_by_inherit() {
        return createTests("""
                let some-variable = "..."; in
                let inherit (unknown-value) some-variable; in
                let inherit (unknown-value) <decl>some-variable</decl>; in
                <ref>some-variable</ref>
                """, "some-variable");
    }

    @TestFactory
    Stream<DynamicNode> string_notation_declaration() {
        return createTests("""
                let <decl>"some-variable"</decl> = "..."; in
                <ref>some-variable</ref>
                """, "some-variable");
    }

    @TestFactory
    Stream<DynamicNode> string_notation_with_special_character() {
        return createTests("""
                let <decl>"my very special variable ⇐"</decl> = "..."; in
                { inherit <ref>"my very special variable ⇐"</ref>; }
                """, "my very special variable ⇐");
    }

    @TestFactory
    // TODO: Maybe use some custom index in NixUsageSearcher?
    @Disabled("NixUsageSearcher uses text search, so it only finds usages where the text matches the symbol")
    Stream<DynamicNode> string_notation_with_escape_sequences() {
        return createTests("""
                let <decl>"$\\$$"</decl> = "..."; in
                { inherit <ref>"$\\$$"</ref>; }
                """, "${x}");
    }

    @TestFactory
    // TODO: Maybe use some custom index in NixUsageSearcher?
    @Disabled("NixUsageSearcher uses text search, so it only finds usages where the text matches the symbol")
    Stream<DynamicNode> string_notation_with_escape_sequences_non_normalized() {
        return createTests("""
                let <decl>"$\\$$"</decl> = "..."; in
                { inherit <ref>"$$\\$"</ref>; }
                """, "${x}");
    }

    @TestFactory
    Stream<DynamicNode> builtin() {
        return createTests("""
                [ <ref>builtins</ref> <ref>builtins</ref>.abc ]
                """, "builtins");
    }

    @TestFactory
    Stream<DynamicNode> shadowed_builtin() {
        return createTests("""
                let <decl>builtins</decl> = "..."; in
                <ref>builtins</ref>.abc
                """, "builtins");
    }

    /**
     * Creates test cases for the given code.
     * The code may contain the following tags:
     * <dl>
     *     <dt>{@code <decl>...</decl>}
     *     <dd>The identifiers where the symbol gets declared.
     *         Must not exist when the symbol is a builtin.
     *     <dt>{@code <ref>...</ref>}
     *     <dd>References to the symbol.
     * </dl>
     *
     * @param code The Nix code which shall be tested, interleaved the tags mentioned above.
     * @param name The name of the symbol.
     * @implNote The language of the parameter is specified as HTML because it provides the most useful highlighting.
     */
    private Stream<DynamicNode> createTests(@NotNull @Language("HTML") String code, @NotNull String name) {
        Markers markers = Markers.parse(code, TAG_DECL, TAG_REF);
        Markers declarations = markers.markers(TAG_DECL);
        Markers references = markers.markers(TAG_REF);

        List<DynamicNode> tests = new ArrayList<>();
        tests.add(dynamicTest("find declarations", () -> findDeclarations(prepareTest(markers, name))));
        tests.add(dynamicTest("find usages", () -> findUsages(prepareTest(markers, name))));
        for (Markers.Marker declaration : declarations) {
            String testName = "resolve symbol from declaration at offset " + declaration.start();
            tests.add(dynamicTest(testName, () -> resolveSymbolFromDeclaration(prepareTest(markers, name), declaration)));
        }
        for (Markers.Marker reference : references) {
            String testName = "resolve symbol from reference at offset " + reference.start();
            tests.add(dynamicTest(testName, () -> resolveSymbolFromReference(prepareTest(markers, name), reference)));
        }
        return tests.stream();
    }

    record Input(@NotNull PsiFile file,
                 @NotNull String code,
                 @NotNull Markers markers,
                 @NotNull String symbolName
    ) {}

    private @NotNull Input prepareTest(@NotNull Markers markers, @NotNull String symbolName) {
        PsiFile file = myFixture.configureByText(NixFileType.INSTANCE, markers.unmarkedText());
        PsiTestUtil.checkErrorElements(file); // Fail early if there is a syntax error
        return new Input(file, markers.unmarkedText(), markers, symbolName);
    }

    @SuppressWarnings({"UnstableApiUsage"})
    private void resolveSymbolFromDeclaration(@NotNull Input input, @NotNull Markers.Marker declarationMarker) {
        NixSymbol expectedSymbol = getSymbol(input);
        NixSymbolDeclaration declaration = mySymbolHelper.findDeclaration(NixSymbolDeclaration.class, input.file, declarationMarker.start());
        assertEquals(declarationMarker.range(), declaration.getAbsoluteRange());
        assertEquals(expectedSymbol, declaration.getSymbol());
    }

    private void resolveSymbolFromReference(@NotNull Input input, @NotNull Markers.Marker referenceMarker) {
        NixSymbol expectedSymbol = getSymbol(input);
        NixScopeReference reference = mySymbolHelper.findReference(NixScopeReference.class, input.file, referenceMarker.start());

        assertEquals(referenceMarker.range(), reference.getElement().getTextRange().cutOut(reference.getRangeInElement()));

        Collection<NixSymbol> symbols = reference.resolveReference();
        assertEquals(List.of(expectedSymbol), List.copyOf(symbols));
        assertEquals(input.symbolName, symbols.iterator().next().getName());
    }

    private void findDeclarations(@NotNull Input input) {
        NixSymbol symbol = getSymbol(input);
        Collection<NixNavigationTarget> navigationTargets = mySymbolHelper.findNavigationTargets(NixNavigationTarget.class, symbol);
        assertEquals(
                input.markers.markers(TAG_DECL),
                Markers.create(input.code, navigationTargets.stream().map(
                        target -> Markers.marker(TAG_DECL, target.getRangeInFile())
                ), TAG_DECL)
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    private void findUsages(@NotNull Input input) {
        NixSymbol symbol = getSymbol(input);
        Collection<Usage> usages = mySymbolHelper.findUsages(symbol, input.file);

        assertEquals(
                input.markers.markers(TAG_DECL, TAG_REF),
                Markers.create(input.code, usages.stream().map(usage -> {
                    PsiUsage psiUsage = (PsiUsage) usage;
                    assertEquals(input.file, psiUsage.getFile());
                    return Markers.marker(psiUsage.getDeclaration() ? TAG_DECL : TAG_REF, psiUsage.getRange());
                }), TAG_DECL, TAG_REF)
        );
    }

    private @NotNull NixSymbol getSymbol(@NotNull Input input) {
        return input.markers.ranges(TAG_DECL).stream().findFirst().map(declaration -> {
            return mySymbolHelper.findSymbol(input.file, input.symbolName, declaration.getStartOffset());
        }).orElseGet(() -> {
            NixBuiltin builtin = NixBuiltin.resolveGlobal(input.symbolName);
            return NixSymbol.builtin(Objects.requireNonNull(builtin, "Builtin not found: " + input.symbolName));
        });
    }
}
