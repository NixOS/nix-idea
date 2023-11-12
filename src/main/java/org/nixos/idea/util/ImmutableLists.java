package org.nixos.idea.util;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Utilities for immutable lists.
 */
public final class ImmutableLists {
    private ImmutableLists() {} // Cannot be instantiated

    @SafeVarargs
    public static <T> @NotNull List<T> append(@NotNull List<? extends T> base, @NotNull T... additionalItems) {
        return concat(base, Arrays.asList(additionalItems));
    }

    public static <T> @NotNull List<T> concat(@NotNull Collection<? extends T> list1, @NotNull Collection<? extends T> list2) {
        List<T> result = new ArrayList<>(list1.size() + list2.size());
        result.addAll(list1);
        result.addAll(list2);
        return List.copyOf(result);
    }

    @SafeVarargs
    public static <T> @NotNull List<T> concat(@NotNull Collection<? extends T>... lists) {
        int totalSize = Arrays.stream(lists).mapToInt(Collection::size).sum();
        List<T> result = new ArrayList<>(totalSize);
        for (Collection<? extends T> list : lists) {
            result.addAll(list);
        }
        return List.copyOf(result);
    }
}
