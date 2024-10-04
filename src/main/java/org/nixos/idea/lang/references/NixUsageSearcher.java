package org.nixos.idea.lang.references;

import com.intellij.find.usages.api.Usage;
import com.intellij.find.usages.api.UsageSearchParameters;
import com.intellij.find.usages.api.UsageSearcher;
import com.intellij.model.search.LeafOccurrence;
import com.intellij.model.search.LeafOccurrenceMapper;
import com.intellij.model.search.SearchContext;
import com.intellij.model.search.SearchService;
import com.intellij.psi.PsiElement;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.lang.NixLanguage;
import org.nixos.idea.lang.references.symbol.NixSymbol;
import org.nixos.idea.lang.references.symbol.NixUserSymbol;
import org.nixos.idea.psi.NixExpr;
import org.nixos.idea.psi.NixPsiElement;
import org.nixos.idea.psi.NixString;
import org.nixos.idea.settings.NixSymbolSettings;

import java.util.Collection;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public final class NixUsageSearcher implements UsageSearcher, LeafOccurrenceMapper.Parameterized<NixSymbol, Usage> {

    @Override
    public @NotNull Collection<? extends Usage> collectImmediateResults(@NotNull UsageSearchParameters parameters) {
        if (!NixSymbolSettings.getInstance().getEnabled()) {
            return List.of();
        } else if (parameters.getTarget() instanceof NixUserSymbol symbol) {
            return symbol.getDeclarations().stream().map(NixUsage::new).toList();
        } else {
            return List.of();
        }
    }

    @Override
    public @Nullable Query<? extends Usage> collectSearchRequest(@NotNull UsageSearchParameters parameters) {
        if (!NixSymbolSettings.getInstance().getEnabled()) {
            return null;
        } else if (parameters.getTarget() instanceof NixSymbol symbol) {
            String name = symbol.getName();
            return SearchService.getInstance()
                    .searchWord(parameters.getProject(), name)
                    .inContexts(SearchContext.IN_CODE_HOSTS, SearchContext.IN_CODE)
                    .inScope(parameters.getSearchScope())
                    .inFilesWithLanguage(NixLanguage.INSTANCE)
                    .buildQuery(LeafOccurrenceMapper.withPointer(symbol.createPointer(), this));
        } else {
            return null;
        }
    }

    @Override
    public @NotNull Collection<? extends Usage> mapOccurrence(@NotNull NixSymbol symbol, @NotNull LeafOccurrence occurrence) {
        int nextOffset = occurrence.getOffsetInStart();
        for (PsiElement element = occurrence.getStart(); element != null && element != occurrence.getScope(); element = element.getParent()) {
            int offset = nextOffset;
            nextOffset += element.getStartOffsetInParent();
            if (element instanceof NixPsiElement nixElement) {
                assert nixElement.getOwnReferences().stream().allMatch(ref -> ref.getElement() == nixElement);
                List<NixUsage> usages = nixElement.getOwnReferences().stream()
                        .filter(ref -> ref.getRangeInElement().contains(offset) && ref.resolvesTo(symbol))
                        .map(NixUsage::new)
                        .toList();
                if (!usages.isEmpty()) {
                    return usages;
                } else if (element instanceof NixExpr && !(element instanceof NixString)) {
                    return List.of(); // Performance optimization
                }
            }
        }
        return List.of();
    }
}
