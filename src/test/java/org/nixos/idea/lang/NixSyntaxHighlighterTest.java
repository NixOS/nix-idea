package org.nixos.idea.lang;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.nixos.idea._testutil.ReflectionUtils;
import org.nixos.idea.psi.NixTokenType;
import org.nixos.idea.psi.NixTypes;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

final class NixSyntaxHighlighterTest {
    @Test
    void testAttributesKeysForUnknownTokenType() {
        TextAttributesKey[] tokenHighlights = new NixSyntaxHighlighter().getTokenHighlights(TokenType.CODE_FRAGMENT);
        assertNotNull(tokenHighlights, "tokenHighlights");
        assertEquals(0, tokenHighlights.length, "tokenHighlights.length");
    }

    @ParameterizedTest
    @MethodSource
    void testAttributesKeysForKnownTokenTypes(@NotNull IElementType tokenType) {
        TextAttributesKey[] tokenHighlights = new NixSyntaxHighlighter().getTokenHighlights(tokenType);
        assertNotNull(tokenHighlights, "tokenHighlights");
        assertNotEquals(0, tokenHighlights.length, "tokenHighlights.length");
        assertAll(IntStream.range(0, tokenHighlights.length).mapToObj(index ->
                () -> assertNotNull(tokenHighlights[index], String.format("tokenHighlights[%d]", index))));
    }

    static @NotNull Stream<Named<IElementType>> testAttributesKeysForKnownTokenTypes() {
        return Stream.concat(
                Stream.of(Named.of("TokenType.BAD_CHARACTER", TokenType.BAD_CHARACTER)),
                ReflectionUtils.getPublicStaticFieldValues(NixTypes.class, NixTokenType.class));
    }

    @ParameterizedTest
    @MethodSource
    void testKeyNamesHaveNixPrefix(@NotNull TextAttributesKey key) {
        assertTrue(key.getExternalName().startsWith("NIX_"));
    }

    static @NotNull Stream<Named<TextAttributesKey>> testKeyNamesHaveNixPrefix() {
        return ReflectionUtils.getPublicStaticFieldValues(NixSyntaxHighlighter.class, TextAttributesKey.class);
    }

    @ParameterizedTest
    @MethodSource
    void testNoDuplicateKeyNames(@NotNull TextAttributesKey key) {
        Set<String> duplicates = ReflectionUtils.getPublicStaticFieldValues(NixSyntaxHighlighter.class, TextAttributesKey.class)
                .filter(other -> other.getPayload().getExternalName().equals(key.getExternalName()))
                .map(Named::getName)
                .collect(Collectors.toSet());
        if (duplicates.size() != 1) {
            fail("Duplicates: " + duplicates);
        }
    }

    static @NotNull Stream<Named<TextAttributesKey>> testNoDuplicateKeyNames() {
        return ReflectionUtils.getPublicStaticFieldValues(NixSyntaxHighlighter.class, TextAttributesKey.class);
    }
}
