package org.nixos.idea.interpretation;

import com.google.errorprone.annotations.Immutable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Path of {@linkplain Attribute attributes} separated by dots. This class is immutable.
 * An attribute path cannot be empty, meaning there is always at least one attribute within this container.
 * Due to the existence of {@linkplain Attribute#isDynamic() dynamic attributes},
 * this class does not implement {@link #equals(Object)} or {@link #hashCode()}.
 *
 * @see AttributeMap
 */
@Immutable
public final class AttributePath {

    private final @NotNull Attribute @NotNull [] myPath;

    private AttributePath(@NotNull Attribute @NotNull [] path) {
        myPath = path.clone();
    }

    @Contract(value = "-> new", pure = true)
    public static @NotNull Builder builder() {
        return new Builder();
    }

    @Contract(pure = true)
    public static @NotNull AttributePath of(@NotNull Attribute @NotNull ... attrs) {
        if (attrs.length == 0) {
            throw new IllegalArgumentException("AttributePath cannot be empty");
        } else {
            return new AttributePath(attrs);
        }
    }

    @Contract(pure = true)
    public static @NotNull AttributePath of(@NotNull Collection<@NotNull Attribute> attrs) {
        if (attrs.isEmpty()) {
            throw new IllegalArgumentException("AttributePath cannot be empty");
        } else {
            return new AttributePath(attrs.toArray(Attribute[]::new));
        }
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
    public @NotNull Attribute last() {
        return myPath[myPath.length - 1];
    }

    @Contract(pure = true)
    public @NotNull Attribute get(int index) {
        return myPath[index];
    }

    @Contract(pure = true)
    public @NotNull AttributePath prefix(int index) {
        if (index >= myPath.length) {
            throw new IllegalArgumentException("index out of range: " + index + " , size: " + myPath.length);
        }
        return AttributePath.of(Arrays.copyOf(myPath, index + 1));
    }

    @Contract(pure = true)
    public @NotNull Attribute @NotNull [] toArray() {
        return myPath.clone();
    }

    @Contract(pure = true)
    public @NotNull List<Attribute> toList() {
        return List.of(myPath);
    }

    @Override
    @Contract(pure = true)
    public String toString() {
        return Arrays.stream(myPath).map(Attribute::toString).collect(Collectors.joining("."));
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
