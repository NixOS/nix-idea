package org.nixos.idea.util;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface StreamableIterable<T> extends Iterable<T> {
    default @NotNull Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}
