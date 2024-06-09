package org.nixos.idea._testutil;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Named;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public final class ReflectionUtils {

    private ReflectionUtils() {
        // Cannot be instantiated.
    }

    public static <T> @NotNull Stream<Named<T>> getPublicStaticFieldValues(@NotNull Class<?> owner, @NotNull Class<? extends T> valueType) {
        return Arrays.stream(owner.getDeclaredFields())
                .filter(field -> Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()))
                .map(field -> {
                    try {
                        Object value = field.get(null);
                        if (valueType.isInstance(value)) {
                            String name = owner.getSimpleName() + "." + field.getName();
                            return Named.<T>of(name, valueType.cast(value));
                        }
                        return null;
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    }
                }).filter(Objects::nonNull);
    }
}
