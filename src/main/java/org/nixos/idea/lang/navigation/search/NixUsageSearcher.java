package org.nixos.idea.lang.navigation.search;

import com.intellij.find.usages.api.PsiUsage;
import com.intellij.find.usages.api.SearchTarget;
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
import org.nixos.idea.lang.navigation.symbol.NixSymbol;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public final class NixUsageSearcher implements UsageSearcher, LeafOccurrenceMapper.Parameterized<NixSymbol, Usage> {
    @Override
    public @NotNull Collection<? extends Usage> collectImmediateResults(@NotNull UsageSearchParameters parameters) {
        if (parameters.getTarget() instanceof NixSymbol) {
            // TODO: Implement!
            return List.of();
        } else {
            return List.of();
        }
    }

    @Override
    public @Nullable Query<? extends Usage> collectSearchRequest(@NotNull UsageSearchParameters parameters) {
        SearchTarget target = parameters.getTarget();
        String name;
        if (target instanceof NixSymbol && (name = ((NixSymbol) target).getName().getName()) != null) {
            // TODO: Implement! Don't we have to build the index first?
            return SearchService.getInstance()
                    .searchWord(parameters.getProject(), name)
                    .inContexts(SearchContext.IN_CODE_HOSTS, SearchContext.IN_CODE, SearchContext.IN_STRINGS)
                    .inScope(parameters.getSearchScope())
                    .inFilesWithLanguage(NixLanguage.INSTANCE)
                    .buildQuery(LeafOccurrenceMapper.withPointer(((NixSymbol) target).createPointer(), this));
        } else {
            return null;
        }
    }

    @Override
    public @NotNull Collection<? extends Usage> mapOccurrence(NixSymbol symbol, @NotNull LeafOccurrence occurrence) {
        PsiElement element = occurrence.getStart();
        while (element != null && element != occurrence.getScope()) {
            List<PsiUsage> usages = Stream.concat(
                            element.getOwnDeclarations().stream()
                                    .filter(decl -> decl.getSymbol().equals(symbol))
                                    .map(PsiUsage::textUsage),
                            element.getOwnReferences().stream()
                                    .filter(ref -> ref.resolvesTo(symbol))
                                    .map(PsiUsage::textUsage))
                    .collect(Collectors.toUnmodifiableList());
            if (!usages.isEmpty()) {
                return usages;
            }
            element = element.getParent();
        }
        return List.of();
    }
}
