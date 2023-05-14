package org.nixos.idea.interpretation;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class AttributePath {
    private static final AttributePath EMPTY = new AttributePath(new Attribute[0]);

    private final @NotNull Attribute @NotNull [] myPath;

    private AttributePath(@NotNull Attribute @NotNull [] path) {
        myPath = path.clone();
    }

    @Contract(value = "-> new", pure = true)
    public static @NotNull Builder builder() {
        return new Builder();
    }

    @Contract(pure = true)
    public static @NotNull AttributePath empty() {
        return EMPTY;
    }

    @Contract(pure = true)
    public static @NotNull AttributePath of(@NotNull Attribute @NotNull ... attrs) {
        if (attrs.length == 0) {
            return EMPTY;
        } else {
            return new AttributePath(attrs);
        }
    }

    @Contract(pure = true)
    public static @NotNull AttributePath of(@NotNull Collection<@NotNull Attribute> attrs) {
        if (attrs.isEmpty()) {
            return EMPTY;
        } else {
            return new AttributePath(attrs.toArray(Attribute[]::new));
        }
    }

    @Contract(pure = true)
    public boolean isEmpty() {
        return myPath.length == 0;
    }

    @Contract(pure = true)
    public int size() {
        return myPath.length;
    }

    @Contract(pure = true)
    public @NotNull Attribute first() {
        return myPath[0];
    }

    @Contract(pure = true)
    public @NotNull Attribute get(int index) {
        return myPath[index];
    }

    @Contract(pure = true)
    public @NotNull Attribute @NotNull [] attributesAsArray() {
        return myPath.clone();
    }

    @Contract(pure = true)
    public @NotNull List<Attribute> attributes() {
        return List.of(myPath);
    }

    @Override
    @Contract(pure = true)
    public String toString() {
        return Arrays.stream(myPath).map(Attribute::toString).collect(Collectors.joining("."));
    }

    @Override
    @Contract(pure = true)
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttributePath that = (AttributePath) o;
        return Arrays.equals(myPath, that.myPath);
    }

    @Override
    @Contract(pure = true)
    public int hashCode() {
        return Arrays.hashCode(myPath);
    }

    public static final class Builder {
        private final @NotNull List<Attribute> myPath = new ArrayList<>();

        private Builder() {
            // Only called by AttributePath.builder()
        }

        @Contract(value = "_ -> this")
        public @NotNull Builder add(@NotNull Attribute... attrs) {
            myPath.addAll(Arrays.asList(attrs));
            return this;
        }

        @Contract(pure = true)
        public @NotNull AttributePath build() {
            return of(myPath);
        }
    }
}
