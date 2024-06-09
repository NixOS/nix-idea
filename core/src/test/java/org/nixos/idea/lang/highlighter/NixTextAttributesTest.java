package org.nixos.idea.lang.highlighter;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.nixos.idea._testutil.ReflectionUtils;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

final class NixTextAttributesTest {
    @ParameterizedTest
    @MethodSource
    void testKeyNamesHaveNixPrefix(@NotNull TextAttributesKey key) {
        assertTrue(key.getExternalName().startsWith("NIX_"));
    }

    static @NotNull Stream<Named<TextAttributesKey>> testKeyNamesHaveNixPrefix() {
        return ReflectionUtils.getPublicStaticFieldValues(NixTextAttributes.class, TextAttributesKey.class);
    }

    @ParameterizedTest
    @MethodSource
    void testNoDuplicateKeyNames(@NotNull TextAttributesKey key) {
        Set<String> duplicates = ReflectionUtils.getPublicStaticFieldValues(NixTextAttributes.class, TextAttributesKey.class)
                .filter(other -> other.getPayload().getExternalName().equals(key.getExternalName()))
                .map(Named::getName)
                .collect(Collectors.toSet());
        if (duplicates.size() != 1) {
            fail("Duplicates: " + duplicates);
        }
    }

    static @NotNull Stream<Named<TextAttributesKey>> testNoDuplicateKeyNames() {
        return ReflectionUtils.getPublicStaticFieldValues(NixTextAttributes.class, TextAttributesKey.class);
    }
}
