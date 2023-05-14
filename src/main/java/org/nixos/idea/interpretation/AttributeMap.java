package org.nixos.idea.interpretation;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.util.TriState;

import java.util.AbstractCollection;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Immutable data structure mapping attribute paths to values.
 *
 * @param <V> The data type of the values.
 */
public final class AttributeMap<V> {
    private static final AttributeMap<?> EMPTY = new AttributeMap<>(new Builder<>());

    private final @NotNull Map<Key, AttributeMap<V>> myChildLevels;
    private final @Nullable V myValue;
    private final int mySize;

    private AttributeMap(@NotNull Builder<V> builder) {
        myChildLevels = builder.myChildLevels.entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue().build()))
                .filter(entry -> entry.getValue() != EMPTY)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        myValue = builder.myValue;
        mySize = (myValue == null ? 0 : 1) + myChildLevels.values().stream().mapToInt(AttributeMap::size).sum();
    }

    @Contract(pure = true)
    public static <V> @NotNull AttributeMap<V> empty() {
        // Since the collection is empty AND immutable, we can safely cast the generic to anything we want.
        //noinspection unchecked
        return (AttributeMap<V>) EMPTY;
    }

    @Contract(value = "-> new", pure = true)
    public static <V> @NotNull Builder<V> builder() {
        return new Builder<>();
    }

    @Contract(pure = true)
    public boolean isEmpty() {
        return mySize == 0;
    }

    @Contract(pure = true)
    public int size() {
        return mySize;
    }

    @Contract(pure = true)
    public @NotNull TriState contains(@NotNull AttributePath path) {
        return contains(0, path);
    }

    private @NotNull TriState contains(int level, @NotNull AttributePath path) {
        Attribute attr = path.get(level);
        Key key = Key.of(attr);
        AttributeMap<V> child = myChildLevels.get(key);
        if (child != null) {
            return child.contains(level + 1, path);
        }
        else {
            TriState result = TriState.FALSE;
            for (Map.Entry<Key, AttributeMap<V>> entry : myChildLevels.entrySet()) {
                if (key instanceof Key.Wildcard || entry.getKey() instanceof Key.Wildcard) {
                    Attribute attrFromMap = entry.getKey().attribute;
                    TriState attrMatch = attrFromMap.matches(attr);
                    if (attrMatch.may()) {
                        result = result.or(attrMatch.and(entry.getValue().contains(level + 1, path)));
                    }
                }
            }
            return result;
        }
    }

    public void walk(@NotNull AttributePath path, @NotNull BiConsumer<AttributePath, V> visitor) {
        walk(new ArrayDeque<>(), path, visitor);
    }

    private void walk(@NotNull Deque<Attribute> attributeStack, @NotNull AttributePath path, @NotNull BiConsumer<AttributePath, V> visitor) {
        if (myValue != null) {
            visitor.accept(AttributePath.of(attributeStack), myValue);
        }

        Attribute attr = path.get(attributeStack.size());
        attributeStack.addLast(attr);
        for (Map.Entry<Key, AttributeMap<V>> entry : myChildLevels.entrySet()) {
            Attribute attrFromMap = entry.getKey().attribute;
            if (attrFromMap.matches(attr).may()) {
                entry.getValue().walk(attributeStack, path, visitor);
            }
        }
        attributeStack.removeLast();
    }

    @Contract(pure = true)
    public @NotNull Collection<Attribute> attributes() {
        return new AbstractCollection<>() {
            @Override
            public Iterator<Attribute> iterator() {
                return myChildLevels.keySet().stream().map(key -> key.attribute).iterator();
            }

            @Override
            public int size() {
                return myChildLevels.size();
            }
        };
    }

    @Contract(pure = true)
    public @NotNull Collection<V> values() {
        return new AbstractCollection<>() {
            @Override
            public Iterator<V> iterator() {
                return streamValues().iterator();
            }

            @Override
            public int size() {
                return mySize;
            }
        };
    }

    @Contract(pure = true)
    public @NotNull Stream<V> streamValues() {
        return Stream.concat(
                Stream.ofNullable(myValue),
                myChildLevels.values().stream().flatMap(AttributeMap::streamValues));
    }

    private static abstract class Key {
        protected final @NotNull Attribute attribute;

        private Key(@NotNull Attribute attribute) {
            this.attribute = attribute;
        }

        private static @NotNull Key of(@NotNull Attribute attribute) {
            Object key = attribute.getKey();
            if (key == null) {
                return new Wildcard(attribute);
            } else {
                return new Specific(key, attribute);
            }
        }

        private static final class Specific extends Key {
            private final @NotNull Object keyObject;

            private Specific(@NotNull Object keyObject, @NotNull Attribute attribute) {
                super(attribute);
                this.keyObject = keyObject;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Specific specific = (Specific) o;
                return Objects.equals(keyObject, specific.keyObject);
            }

            @Override
            public int hashCode() {
                return Objects.hash(keyObject);
            }
        }

        private static final class Wildcard extends Key {
            private Wildcard(@NotNull Attribute attribute) {
                super(attribute);
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Wildcard wildcard = (Wildcard) o;
                return Objects.equals(attribute, wildcard.attribute);
            }

            @Override
            public int hashCode() {
                return Objects.hash(attribute);
            }
        }
    }

    public static final class Builder<V> {
        private final @NotNull Map<Key, Builder<V>> myChildLevels = new HashMap<>();
        private @Nullable V myValue = null;

        private Builder() {
            // Use AttributeMap.builder()
        }

        @Contract("_, _ -> this")
        public Builder<V> set(@NotNull AttributePath path, V value) {
            getOrCreate(0, path).myValue = value;
            return this;
        }

        @Contract("_, _, _ -> this")
        public Builder<V> merge(@NotNull AttributePath path, V value, @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
            Builder<V> entry = getOrCreate(0, path);
            entry.myValue = entry.myValue == null ? value : remappingFunction.apply(entry.myValue, value);
            return this;
        }

        private @NotNull Builder<V> getOrCreate(int level, @NotNull AttributePath path) {
            if (level == path.size()) {
                return this;
            }
            Attribute attr = path.get(level);
            Key key = Key.of(attr);
            return myChildLevels.computeIfAbsent(key, builderFactory()).getOrCreate(level + 1, path);
        }

        @Contract("_ -> this")
        public Builder<V> update(@NotNull AttributeMap<? extends V> other) {
            if (other.myValue != null) {
                myValue = other.myValue;
            }
            for (var entry : other.myChildLevels.entrySet()) {
                myChildLevels.computeIfAbsent(entry.getKey(), builderFactory()).update(entry.getValue());
            }
            return this;
        }

        @Contract("_, _ -> this")
        public Builder<V> merge(@NotNull AttributeMap<? extends V> other, @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
            if (other.myValue != null) {
                myValue = myValue == null ? other.myValue : remappingFunction.apply(myValue, other.myValue);
            }
            for (var entry : other.myChildLevels.entrySet()) {
                myChildLevels.computeIfAbsent(entry.getKey(), builderFactory()).merge(entry.getValue(), remappingFunction);
            }
            return this;
        }

        @Contract(pure = true)
        public @NotNull AttributeMap<V> build() {
            if (myChildLevels.isEmpty() && myValue == null) {
                return empty();
            } else {
                return new AttributeMap<>(this);
            }
        }

        private static <V> Function<Object, Builder<V>> builderFactory() {
            return __ -> new Builder<>();
        }
    }
}
