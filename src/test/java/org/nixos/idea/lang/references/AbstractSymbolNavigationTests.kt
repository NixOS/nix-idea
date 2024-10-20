package org.nixos.idea.lang.references

import com.intellij.find.usages.api.UsageAccess
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicNode
import org.nixos.idea._testutil.Markers
import org.nixos.idea._testutil.TestFactoryDsl
import org.nixos.idea._testutil.WithIdeaPlatform
import org.nixos.idea.file.NixFileType
import org.nixos.idea.lang.builtins.NixBuiltin
import org.nixos.idea.lang.references.symbol.NixSymbol
import org.nixos.idea.psi.NixDeclarationHost
import org.nixos.idea.settings.NixSymbolSettings

@WithIdeaPlatform.OnEdt
@WithIdeaPlatform.CodeInsight
@Suppress("UnstableApiUsage")
abstract class AbstractSymbolNavigationTests {

    private lateinit var myFixture: CodeInsightTestFixture
    private lateinit var mySymbolHelper: SymbolTestHelper

    @BeforeEach
    fun setUp(fixture: CodeInsightTestFixture) {
        myFixture = fixture
        mySymbolHelper = SymbolTestHelper(fixture)
    }

    class Config {
        @Language("HTML")
        lateinit var code: String

        var findDeclarations = true
        var findReferences = true
        var resolveDeclarations = true
        var resolveReferences = true
    }

    /**
     * Test factory for symbol navigation.
     *
     * ```
     * @TestFactory
     * fun simple_assignment() = test {
     *     code = """
     *         let <symbol>x.<decl>y</decl></symbol> = "..."; in
     *         x.<ref>y</ref>
     *         """.trimIndent()
     * }
     * ```
     *
     * The code may contain the following tags:
     *
     *  *  **`<builtin>...</builtin>`**
     *     The name of a [NixBuiltin] which is used by the test.
     *     The location of this tag doesn't have any impact, only the content.
     *     By convention, it is recommended to but this tag into a comment at the top of the file.
     *  *  **`<symbol>...</symbol>`**
     *     The attribute path of a [NixUserSymbol] which is used by the test.
     *     The tag must be located within the corresponding [NixDeclarationHost].
     *     The exact location within the declaration host doesn't have any impact.
     *     There must be at most one symbol per declaration host.
     *  *  **`<decl>...</decl>`**
     *     The identifiers which declare the symbol.
     *     The test factory verifies that each marked identifier is a declaration
     *     which resolves to the symbol within the same declaration host.
     *  *  **`<ref>...</ref>`**
     *     The identifiers which reference the symbols.
     *     The test factory verifies that each marked identifier is a reference
     *     which resolves to all the marked symbols and builtins.
     *
     * Based on the given tags, the method will generate tests for the following scenarios:
     *
     *  *  **Go to Declaration:**
     *     For each symbol, try to resolve all declarations in the same declaration host.
     *  *  **Find Usages:**
     *     For each symbol, try to find all usages (declarations and references).
     *  *  **Resolve Declaration:**
     *     For each declaration, try to resolve the matching symbol.
     *  *  **Resolve References:**
     *     For each reference, try to resolve all symbols.
     */
    fun test(init: Config.() -> Unit): List<DynamicNode> {
        val config = Config()
        config.init()

        val markers = Markers.parse(config.code, TAG_BUILTIN, TAG_SYMBOL, TAG_DECL, TAG_REF)
        val unmarkedCode = markers.unmarkedText()
        val symbolMarkers = markers.markers(TAG_BUILTIN, TAG_SYMBOL)
        val declarationMarkers = markers.markers(TAG_DECL)
        val referenceMarkers = markers.markers(TAG_REF)

        NixSymbolSettings.getInstance().enabled = true
        val file = myFixture.configureByText(NixFileType.INSTANCE, unmarkedCode)
        PsiTestUtil.checkErrorElements(file) // Fail early if there is a syntax error

        return TestFactoryDsl.testFactory {
            if (config.findDeclarations) {
                containers("go to declaration", symbolMarkers) { symbolMarker ->
                    test("jump to first declaration = true") {
                        testGoToDeclaration(file, symbolMarker, declarationMarkers, true)
                    }
                    test("jump to first declaration = false") {
                        testGoToDeclaration(file, symbolMarker, declarationMarkers, false)
                    }
                }
            }
            if (config.findReferences) {
                containers("find usages", symbolMarkers) { symbolMarker ->
                    test("show declarations as usages = false") {
                        NixSymbolSettings.getInstance().showDeclarationsAsUsages = false
                        testFindUsages(file, symbolMarker, referenceMarkers, false)
                    }
                    test("show declarations as usages = true") {
                        NixSymbolSettings.getInstance().showDeclarationsAsUsages = true
                        if (config.findDeclarations) {
                            val withDeclarations = referenceMarkers.withMarkers(
                                declarationMarkers.filterSameHostAs(
                                    file,
                                    symbolMarker.start()
                                ).stream(),
                                TAG_DECL
                            )
                            testFindUsages(file, symbolMarker, withDeclarations, false)
                        } else {
                            testFindUsages(file, symbolMarker, referenceMarkers, true)
                        }
                    }
                }
            }
            if (config.resolveDeclarations) {
                tests("resolve declaration", declarationMarkers) {
                    val offset = it.start()
                    val symbols = getSymbols(file, symbolMarkers.filterSameHostAs(file, offset)).toSet()
                    val declaration = mySymbolHelper.findDeclaration(NixSymbolDeclaration::class.java, file, offset)
                    Assertions.assertEquals(it.range(), declaration.absoluteRange)
                    Assertions.assertTrue(declaration.symbol in symbols)
                }
            }
            if (config.resolveReferences) {
                tests("resolve reference", referenceMarkers) {
                    val symbols = getSymbols(file, symbolMarkers).toSet()
                    val reference = mySymbolHelper.findReference(NixSymbolReference::class.java, file, it.start())
                    Assertions.assertEquals(it.range(), reference.element.textRange.cutOut(reference.rangeInElement))
                    Assertions.assertEquals(symbols, reference.resolveReference().toSet())
                }
            }
        }
    }

    private fun getSymbols(file: PsiFile, symbolMarkers: Iterable<Markers.Marker>): Iterable<NixSymbol> {
        return symbolMarkers.map { getSymbol(file, it) }
    }

    private fun getSymbol(file: PsiFile, marker: Markers.Marker): NixSymbol {
        return when (marker.tagName()) {
            TAG_BUILTIN -> {
                val name = marker.range().substring(file.text)
                val instance = NixBuiltin.resolveBuiltin(name)
                NixSymbol.builtin(requireNotNull(instance))
            }

            TAG_SYMBOL -> {
                val attrPath = marker.range().substring(file.text)
                mySymbolHelper.findSymbol(file, attrPath, marker.start())
            }

            else -> throw IllegalStateException("Unknown tag name: " + marker.tagName())
        }
    }

    private fun Iterable<Markers.Marker>.filterSameHostAs(file: PsiFile, offset: Int): List<Markers.Marker> {
        val needle = PsiTreeUtil.findElementOfClassAtOffset(file, offset, NixDeclarationHost::class.java, false)
        return this.filter {
            val host = PsiTreeUtil.findElementOfClassAtOffset(file, it.start(), NixDeclarationHost::class.java, false)
            host == needle
        }
    }

    private fun testGoToDeclaration(
        file: PsiFile,
        symbolMarker: Markers.Marker,
        declarationMarkers: Markers,
        jumpToFirstDeclaration: Boolean
    ) {
        NixSymbolSettings.getInstance().jumpToFirstDeclaration = jumpToFirstDeclaration
        val symbol = getSymbol(file, symbolMarker)
        val navigationTargets = mySymbolHelper.findNavigationTargets(NixNavigationTarget::class.java, symbol)
        var expected = declarationMarkers
            .filterSameHostAs(file, symbolMarker.start())
            .map { it.range() }
        if (jumpToFirstDeclaration) {
            expected = expected.stream().limit(1).toList()
        }
        Assertions.assertEquals(
            Markers.create(
                declarationMarkers.unmarkedText(),
                TAG_DECL,
                expected
            ),
            Markers.create(
                declarationMarkers.unmarkedText(),
                TAG_DECL,
                navigationTargets.stream().map { it.rangeInFile }.toList()
            )
        )
    }

    private fun testFindUsages(
        file: PsiFile,
        symbolMarker: Markers.Marker,
        usageMarkers: Markers,
        ignoreDeclarations: Boolean
    ) {
        val symbol = getSymbol(file, symbolMarker)
        Assertions.assertEquals(
            usageMarkers,
            Markers.create(
                usageMarkers.unmarkedText(),
                mySymbolHelper.findUsages(symbol, file)
                    .filter { !it.declaration }
                    .flatMap {
                        val nixUsage = it as NixUsage
                        Assertions.assertEquals(file, nixUsage.file)
                        val isDeclaration = nixUsage.computeAccess() == UsageAccess.Write
                        when {
                            isDeclaration && ignoreDeclarations -> emptyList()
                            isDeclaration -> listOf(Markers.marker(TAG_DECL, nixUsage.range))
                            else -> listOf(Markers.marker(TAG_REF, nixUsage.range))
                        }
                    },
                TAG_DECL, TAG_REF
            )
        )
    }

    companion object {
        private val TAG_BUILTIN: Markers.TagName = Markers.tagName("builtin")
        private val TAG_SYMBOL: Markers.TagName = Markers.tagName("symbol")
        private val TAG_REF: Markers.TagName = Markers.tagName("ref")
        private val TAG_DECL: Markers.TagName = Markers.tagName("decl")
    }
}
