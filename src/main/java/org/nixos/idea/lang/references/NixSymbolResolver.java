package org.nixos.idea.lang.references;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.lang.references.symbol.NixSymbol;
import org.nixos.idea.psi.NixAttrPath;
import org.nixos.idea.psi.NixExpr;
import org.nixos.idea.psi.NixExprAttrs;
import org.nixos.idea.psi.NixExprSelect;
import org.nixos.idea.psi.NixExprVar;
import org.nixos.idea.psi.NixPsiUtil;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public interface NixSymbolResolver {

    Logger LOG = Logger.getInstance(NixSymbolResolver.class);

    @NotNull
    Collection<NixSymbol> resolve(@NotNull String attributeName);

    static @NotNull NixSymbolResolver noop() {
        return new NixSymbolResolver() {
            @Override
            public @NotNull Collection<NixSymbol> resolve(@NotNull String attributeName) {
                return List.of();
            }
        };
    }

    static @NotNull NixSymbolResolver of(@Nullable NixExpr expr) {
        if (expr == null) {
            return noop();
        }
        return new NixSymbolResolver() {
            @Override
            public @NotNull Collection<NixSymbol> resolve(@NotNull String attributeName) {
                // TODO: We may want to replace this with some more elaborate interpreter
                //  which also supports other elements
                if (expr instanceof NixExprVar var) {
                    Collection<? extends NixSymbolReference> references = var.getOwnReferences();
                    if (references.size() > 1) {
                        LOG.error("Unexpected references for variable: " + references);
                    }
                    if (!references.isEmpty()) {
                        return references.iterator().next().resolveReference().stream()
                                .flatMap(symbol -> symbol.resolve(attributeName).stream())
                                .toList();
                    } else {
                        return List.of();
                    }
                } else if (expr instanceof NixExprSelect select) {
                    NixAttrPath attrPath = select.getAttrPath();
                    List<? extends NixSymbolReference> references = select.getOwnReferences();
                    Stream<? extends NixSymbolResolver> valueResolver = Stream.empty();
                    if (attrPath != null && references.size() == attrPath.getAttrList().size()) {
                        valueResolver = references.get(references.size() - 1).resolveReference().stream();
                    }
                    return Stream.concat(valueResolver, Stream.of(NixSymbolResolver.of(select.getDefault())))
                            .flatMap(symbol -> symbol.resolve(attributeName).stream())
                            .toList();
                } else if (expr instanceof NixExprAttrs attrs) {
                    if (NixPsiUtil.isLegacyLet(attrs)) {
                        return ContainerUtil.createMaybeSingletonList(attrs.getSymbol(List.of("body", attributeName)));
                    } else {
                        return ContainerUtil.createMaybeSingletonList(attrs.getSymbol(List.of(attributeName)));
                    }
                } else {
                    return List.of();
                }
            }
        };
    }

    static @NotNull NixSymbolResolver of(@NotNull NixSymbolResolver delegate, @NotNull String... prefix) {
        return new NixSymbolResolver() {
            private @Nullable Collection<NixSymbolResolver> myCache;

            @Override
            public @NotNull Collection<NixSymbol> resolve(@NotNull String attributeName) {
                return resolveResolvers().stream()
                        .flatMap(resolver -> resolver.resolve(attributeName).stream())
                        .toList();
            }

            private @NotNull Collection<NixSymbolResolver> resolveResolvers() {
                if (myCache == null) {
                    Stream<NixSymbolResolver> resolvers = Stream.of(delegate);
                    for (String attributeName : prefix) {
                        resolvers = resolvers.flatMap(resolver -> resolver.resolve(attributeName).stream());
                    }
                    myCache = resolvers.toList();
                }
                return myCache;
            }
        };
    }
}
