package org.nixos.idea.settings;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.icon.NixIcons;
import org.nixos.idea.lang.NixSyntaxHighlighter;

import javax.swing.Icon;
import java.util.Map;

public final class NixColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            descriptor("Keyword", NixSyntaxHighlighter.KEYWORD),
            descriptor("Identifier", NixSyntaxHighlighter.IDENTIFIER),
            descriptor("Operators//Semicolon", NixSyntaxHighlighter.SEMICOLON),
            descriptor("Operators//Comma", NixSyntaxHighlighter.COMMA),
            descriptor("Operators//Dot", NixSyntaxHighlighter.DOT),
            descriptor("Operators//Assignment operator", NixSyntaxHighlighter.ASSIGN),
            descriptor("Operators//Colon", NixSyntaxHighlighter.COLON),
            descriptor("Operators//At sign (@)", NixSyntaxHighlighter.AT),
            descriptor("Operators//Ellipsis", NixSyntaxHighlighter.ELLIPSIS),
            descriptor("Operators//Other operators", NixSyntaxHighlighter.OPERATION_SIGN),
            descriptor("Braces//Parentheses", NixSyntaxHighlighter.PARENTHESES),
            descriptor("Braces//Curly braces", NixSyntaxHighlighter.BRACES),
            descriptor("Braces//Brackets", NixSyntaxHighlighter.BRACKETS),
            descriptor("Literals and Values//Number", NixSyntaxHighlighter.NUMBER),
            descriptor("Literals and Values//String", NixSyntaxHighlighter.STRING),
            descriptor("Literals and Values//Escape sequence", NixSyntaxHighlighter.STRING_ESCAPE),
            descriptor("Literals and Values//Path", NixSyntaxHighlighter.PATH),
            descriptor("Literals and Values//URI", NixSyntaxHighlighter.URI),
            descriptor("Comments//Line comment", NixSyntaxHighlighter.LINE_COMMENT),
            descriptor("Comments//Block comment", NixSyntaxHighlighter.BLOCK_COMMENT),
    };

    @Override
    public @NotNull Icon getIcon() {
        return NixIcons.FILE;
    }

    @Override
    public @NotNull SyntaxHighlighter getHighlighter() {
        return new NixSyntaxHighlighter();
    }

    @Override
    public @NonNls @NotNull String getDemoText() {
        // language=Nix
        return "/* This code demonstrates the syntax highlighting for the Nix Expression Language */\n" +
                "let\n" +
                "    literals.number = 42;\n" +
                "    literals.string1 = \"This is a normal string\";\n" +
                "    literals.string2 = ''\n" +
                "        Broken escape sequence:  \\${literals.number}\n" +
                "        Escaped interpolation:   ''${literals.number}\n" +
                "        Generic escape sequence: $''\\{literals.number}\n" +
                "        '';\n" +
                "    literals.paths = [/etc/gitconfig ~/.gitconfig .git/config];\n" +
                "    # Note that unquoted URIs were deperecated by RFC 45\n" +
                "    literals.uri = https://github.com/NixOS/rfcs/pull/45;\n" +
                "in {\n" +
                "    inherit (literals) number string1 string2 paths uri;\n" +
                "    nixpkgs = import <nixpkgs>;\n" +
                "    baseNames = map baseNameOf literals.paths;\n" +
                "    f = { multiply ? 1, add ? 0, ... }@args:\n" +
                "        builtins.mapAttrs (name: value: multiply * value + add) args;\n" +
                "}";
    }

    @Override
    public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Nix";
    }

    private static @NotNull AttributesDescriptor descriptor(@NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String displayName, @NotNull TextAttributesKey key) {
        return new AttributesDescriptor(displayName, key);
    }
}
