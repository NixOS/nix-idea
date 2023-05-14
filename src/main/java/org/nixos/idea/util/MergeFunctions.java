package org.nixos.idea.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class MergeFunctions {
    public static <V> @NotNull List<V> mergeLists(Collection<? extends V> first, Collection<? extends V> second) {
        ArrayList<V> result = new ArrayList<>(first.size() + second.size());
        result.addAll(first);
        result.addAll(second);
        return List.copyOf(result); // Make list immutable
    }
}
