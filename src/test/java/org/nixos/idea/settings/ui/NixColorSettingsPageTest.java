package org.nixos.idea.settings.ui;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.nixos.idea._testutil.ReflectionUtils;
import org.nixos.idea.lang.highlighter.NixTextAttributes;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class NixColorSettingsPageTest {
    @ParameterizedTest
    @MethodSource
    void testNoKeyMissing(@NotNull TextAttributesKey textAttributesKey) {
        AttributesDescriptor[] descriptors = new NixColorSettingsPage().getAttributeDescriptors();
        assertTrue(Arrays.stream(descriptors)
                .map(AttributesDescriptor::getKey)
                .anyMatch(textAttributesKey::equals));
    }

    static @NotNull Stream<Named<TextAttributesKey>> testNoKeyMissing() {
        return ReflectionUtils.getPublicStaticFieldValues(NixTextAttributes.class, TextAttributesKey.class);
    }
}
