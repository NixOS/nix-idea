package org.nixos.idea.interpretation;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.psi.NixPsiElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Scope {
    public static final Scope EMPTY = new Scope(Map.of());

    private final @NotNull Map<Object, NixPsiElement> mySources;

    private Scope(@NotNull Map<Object, NixPsiElement> sources) {
        this.mySources = Map.copyOf(sources);
    }

    @Contract(pure = true)
    public @Nullable NixPsiElement getSource(@NotNull AttributePath path) {
        Attribute first = path.first();
        Object key = Objects.requireNonNull(first.getKey(), "attribute without key");
        return mySources.get(key);
    }

    @Contract(pure = true)
    public @NotNull Collection<Declaration> resolveDeclarations(@NotNull AttributePath path) {
        NixPsiElement source = getSource(path);
        if (source == null) {
            return List.of();
        }
        List<Declaration> result = new ArrayList<>();
        source.getDeclarations().walk(path, (__, declarations) -> result.addAll(declarations));
        return Collections.unmodifiableCollection(result);
    }

    @Contract(pure = true)
    public @NotNull Scope subScope(@NotNull NixPsiElement source, @NotNull AttributeMap<Collection<Declaration>> declarations) {
        assert declarations.values().stream().flatMap(Collection::stream).allMatch(declaration -> declaration.source() == source);
        if (declarations.isEmpty()) {
            return this;
        }
        else {
            Map<Object, NixPsiElement> newSources = new HashMap<>(mySources);
            for (Attribute attribute : declarations.attributes()) {
                newSources.put(Objects.requireNonNull(attribute.getKey()), source);
            }
            return new Scope(newSources);
        }
    }
}
