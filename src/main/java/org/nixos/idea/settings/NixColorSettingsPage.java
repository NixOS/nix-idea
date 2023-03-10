package org.nixos.idea.settings;

import com.intellij.lang.Language;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.RainbowColorSettingsPage;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.icon.NixIcons;
import org.nixos.idea.lang.NixLanguage;
import org.nixos.idea.lang.highlighter.NixRainbowVisitor;
import org.nixos.idea.lang.highlighter.NixSyntaxHighlighter;
import org.nixos.idea.lang.highlighter.NixTextAttributes;

import javax.swing.Icon;
import java.util.Map;

public final class NixColorSettingsPage implements RainbowColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            descriptor("Keyword", NixTextAttributes.KEYWORD),
            descriptor("Operators//Semicolon", NixTextAttributes.SEMICOLON),
            descriptor("Operators//Comma", NixTextAttributes.COMMA),
            descriptor("Operators//Dot", NixTextAttributes.DOT),
            descriptor("Operators//Assignment operator", NixTextAttributes.ASSIGN),
            descriptor("Operators//Colon", NixTextAttributes.COLON),
            descriptor("Operators//At sign (@)", NixTextAttributes.AT),
            descriptor("Operators//Ellipsis", NixTextAttributes.ELLIPSIS),
            descriptor("Operators//Other operators", NixTextAttributes.OPERATION_SIGN),
            descriptor("Braces//Parentheses", NixTextAttributes.PARENTHESES),
            descriptor("Braces//Curly braces", NixTextAttributes.BRACES),
            descriptor("Braces//Brackets", NixTextAttributes.BRACKETS),
            descriptor("Variables and Attributes//Other identifier", NixTextAttributes.IDENTIFIER),
            descriptor("Variables and Attributes//Local variable", NixTextAttributes.LOCAL_VARIABLE),
            descriptor("Variables and Attributes//Function parameter", NixTextAttributes.PARAMETER),
            descriptor("Built-in constants and functions//Literals", NixTextAttributes.LITERAL),
            descriptor("Built-in constants and functions//Import function", NixTextAttributes.IMPORT),
            descriptor("Built-in constants and functions//Other built-ins", NixTextAttributes.BUILTIN),
            descriptor("Literals and Values//Number", NixTextAttributes.NUMBER),
            descriptor("Literals and Values//String", NixTextAttributes.STRING),
            descriptor("Literals and Values//Escape sequence", NixTextAttributes.STRING_ESCAPE),
            descriptor("Literals and Values//Path", NixTextAttributes.PATH),
            descriptor("Literals and Values//URI", NixTextAttributes.URI),
            descriptor("Comments//Line comment", NixTextAttributes.LINE_COMMENT),
            descriptor("Comments//Block comment", NixTextAttributes.BLOCK_COMMENT),
    };

    private static final Map<String, TextAttributesKey> ADDITIONAL_HIGHLIGHTING_TAG = Map.of(
            "builtin", NixTextAttributes.BUILTIN,
            "import", NixTextAttributes.IMPORT,
            "literal", NixTextAttributes.LITERAL,
            "variable", NixTextAttributes.LOCAL_VARIABLE,
            "parameter", NixTextAttributes.PARAMETER);

    @Override
    public @NotNull Language getLanguage() {
        return NixLanguage.INSTANCE;
    }

    @Override
    public boolean isRainbowType(TextAttributesKey type) {
        // I think this method shall return true if the NixRainbowVisitor will always override the color of the given
        // key. Unfortunately this method is not documented, but I assume the semantic highlighting will avoid using
        // colors which correspond to attribute keys for which this method returns false.
        return NixRainbowVisitor.RAINBOW_ATTRIBUTES.contains(type);
    }

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
        return "/* This code demonstrates the syntax highlighting for the Nix Expression Language */\n" +
                "let\n" +
                "    <variable>literals</variable>.null = <literal>null</literal>;\n" +
                "    <variable>literals</variable>.boolean = <literal>true</literal>;\n" +
                "    <variable>literals</variable>.number = 42;\n" +
                "    <variable>literals</variable>.string1 = \"This is a normal string\";\n" +
                "    <variable>literals</variable>.string2 = ''\n" +
                "        Broken escape sequence:  \\${<variable>literals</variable>.number}\n" +
                "        Escaped interpolation:   ''${<variable>literals</variable>.number}\n" +
                "        Generic escape sequence: $''\\{<variable>literals</variable>.number}\n" +
                "        '';\n" +
                "    <variable>literals</variable>.paths = [/etc/gitconfig ~/.gitconfig .git/config];\n" +
                "    # Note that unquoted URIs were deperecated by RFC 45\n" +
                "    <variable>literals</variable>.uri = https://github.com/NixOS/rfcs/pull/45;\n" +
                "in {\n" +
                "    inherit (<variable>literals</variable>) number string1 string2 paths uri;\n" +
                "    nixpkgs = <import>import</import> <nixpkgs>;\n" +
                "    baseNames = <builtin>map</builtin> <builtin>baseNameOf</builtin> <variable>literals</variable>.paths;\n" +
                "    f = { <parameter>multiply</parameter> ? 1, <parameter>add</parameter> ? 0, ... }@<parameter>args</parameter>:\n" +
                "        <builtin>builtins</builtin>.<builtin>mapAttrs</builtin> (<parameter>name</parameter>: <parameter>value</parameter>: <parameter>multiply</parameter> * <parameter>value</parameter> + <parameter>add</parameter>) <parameter>args</parameter>;\n" +
                "}";
    }

    @Override
    public @NotNull Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return ADDITIONAL_HIGHLIGHTING_TAG;
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
