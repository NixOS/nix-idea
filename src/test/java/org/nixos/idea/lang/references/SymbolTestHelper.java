package org.nixos.idea.lang.references;

import com.intellij.find.usages.api.SearchTarget;
import com.intellij.find.usages.api.Usage;
import com.intellij.find.usages.api.UsageSearchParameters;
import com.intellij.model.Symbol;
import com.intellij.model.psi.PsiSymbolDeclaration;
import com.intellij.model.psi.PsiSymbolReference;
import com.intellij.model.search.SearchService;
import com.intellij.navigation.SymbolNavigationService;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.platform.backend.navigation.NavigationTarget;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiTreeUtilKt;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.lang.references.symbol.NixSymbol;
import org.nixos.idea.lang.references.symbol.NixUserSymbol;
import org.nixos.idea.psi.NixDeclarationHost;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;

public final class SymbolTestHelper {

    private final @NotNull CodeInsightTestFixture myFixture;

    public SymbolTestHelper(@NotNull CodeInsightTestFixture fixture) {
        myFixture = fixture;
    }

    //region findSymbol

    public @NotNull NixSymbol findSymbol(@NotNull PsiFile file, @NotNull String name, int offset) {
        return findSymbol(file, List.of(name), offset);
    }

    public @NotNull NixSymbol findSymbol(@NotNull PsiFile file, @NotNull List<String> path, int offset) {
        NixDeclarationHost host = PsiTreeUtil.findElementOfClassAtOffset(file, offset, NixDeclarationHost.class, false);
        if (host == null) {
            throw new IllegalStateException("No NixDeclarationHost found on given location");
        }
        NixUserSymbol symbol = host.getSymbol(path);
        return Objects.requireNonNull(symbol, "Symbol not found: " + String.join(".", path));
    }

    //endregion
    //region findDeclarations

    @SuppressWarnings("UnstableApiUsage")
    public <T extends PsiSymbolDeclaration> @NotNull T findDeclaration(@NotNull Class<T> type, @NotNull PsiFile file, int offset) {
        Collection<PsiSymbolDeclaration> declarations = findDeclarations(file, offset);
        List<T> typedDeclarations = declarations.stream()
                .filter(type::isInstance).map(type::cast)
                .toList();
        if (declarations.isEmpty()) {
            return fail("No declaration found");
        } else if (typedDeclarations.isEmpty()) {
            return fail(String.format("No declaration of type %s found. Found: %s", type.getSimpleName(), declarations));
        } else if (typedDeclarations.size() > 1) {
            return fail("Multiple declarations found: " + declarations);
        } else {
            return typedDeclarations.get(0);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public @NotNull Collection<PsiSymbolDeclaration> findDeclarations(@NotNull PsiFile file, int offset) {
        return findDeclarations(PsiSymbolDeclaration.class, file, offset);
    }

    @SuppressWarnings({"UnstableApiUsage", "OverrideOnly"})
    public <T extends PsiSymbolDeclaration> @NotNull Collection<T> findDeclarations(@NotNull Class<T> type, @NotNull PsiFile file, int offset) {
        List<PsiSymbolDeclaration> allDeclarations = new ArrayList<>();
        PsiTreeUtilKt.elementsAtOffsetUp(file, offset)
                .forEachRemaining(element -> allDeclarations.addAll(element.getFirst().getOwnDeclarations()));
        return allDeclarations.stream()
                .filter(declaration -> declaration.getAbsoluteRange().contains(offset))
                .filter(type::isInstance).map(type::cast)
                .toList();
    }

    //endregion
    //region findReferences

    @SuppressWarnings("UnstableApiUsage")
    public <T extends PsiSymbolReference> @NotNull T findReference(@NotNull Class<T> type, @NotNull PsiFile file, int offset) {
        myFixture.openFileInEditor(file.getVirtualFile());
        myFixture.getEditor().getCaretModel().moveToOffset(offset);
        return assertInstanceOf(type, ReadAction.compute(myFixture::findSingleReferenceAtCaret));
    }

    //endregion
    //region findNavigationTargets

    @SuppressWarnings("UnstableApiUsage")
    public <T extends NavigationTarget> @NotNull Collection<T> findNavigationTargets(@NotNull Class<T> type, @NotNull Symbol symbol) {
        return SymbolNavigationService.getInstance().getNavigationTargets(myFixture.getProject(), symbol).stream()
                .filter(type::isInstance).map(type::cast)
                .toList();
    }

    //endregion
    //region findUsages

    @SuppressWarnings("UnstableApiUsage")
    public @NotNull Collection<Usage> findUsages(@NotNull SearchTarget target, @NotNull PsiFile file) {
        return findUsages(target, new LocalSearchScope(file));
    }

    @SuppressWarnings("UnstableApiUsage")
    public @NotNull Collection<Usage> findUsages(@NotNull SearchTarget target, @NotNull SearchScope scope) {
        return SearchService.getInstance().searchParameters(new UsageSearchParameters() {
            @Override
            public @NotNull SearchTarget getTarget() {
                return target;
            }

            @Override
            public @NotNull SearchScope getSearchScope() {
                return scope;
            }

            @Override
            public @NotNull Project getProject() {
                return myFixture.getProject();
            }

            @Override
            public boolean areValid() {
                return true;
            }
        }).findAll();
    }

    //endregion
}
