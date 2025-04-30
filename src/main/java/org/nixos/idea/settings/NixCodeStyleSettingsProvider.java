package org.nixos.idea.settings;

import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.application.options.SmartIndentOptionsEditor;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleConfigurable;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizableOptions;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings.IndentOptions;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.lang.NixLanguage;
import org.nixos.idea.settings.ui.NixCodeStyleConfigurable;

final class NixCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {

    @Override
    public @NotNull CustomCodeStyleSettings createCustomSettings(@NotNull CodeStyleSettings settings) {
        return new NixCodeStyleSettings(settings);
    }

    @Override
    public @NotNull Language getLanguage() {
        return NixLanguage.INSTANCE;
    }

    @Override
    public @NotNull IndentOptionsEditor getIndentOptionsEditor() {
        return new SmartIndentOptionsEditor(this);
    }

    @Override
    protected void customizeDefaults(@NotNull CommonCodeStyleSettings commonSettings, @NotNull IndentOptions indentOptions) {
        // Defaults based on RFC
        // https://github.com/NixOS/rfcs/blob/master/rfcs/0166-nix-formatting.md#initial-standard-nix-format
        indentOptions.INDENT_SIZE = 2;
        indentOptions.CONTINUATION_INDENT_SIZE = 2;
        commonSettings.KEEP_BLANK_LINES_IN_CODE = 1;
        commonSettings.KEEP_LINE_BREAKS = false; // TODO Should I keep this enabled?
        commonSettings.SPACE_WITHIN_BRACKETS = true;
        commonSettings.SPACE_WITHIN_BRACES = true;
    }

    @Override
    public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer, @NotNull SettingsType settingsType) {
        CodeStyleSettingsCustomizableOptions constants = CodeStyleSettingsCustomizableOptions.getInstance();
        // Note: Standard options are defined in class CodeStyleSettingsPresentations
        switch (settingsType) {
            case INDENT_SETTINGS -> {
                // CodeStyleSettingsCustomizable.IndentOption
                consumer.showStandardOptions(
                        "INDENT_SIZE",
                        "CONTINUATION_INDENT_SIZE",
                        "KEEP_INDENTS_ON_EMPTY_LINES"
                );
            }
            case SPACING_SETTINGS -> {
                // CodeStyleSettingsCustomizable.SpacingOption
                consumer.showStandardOptions(
                        // Braces {}, brackets [], and parentheses ()
                        "SPACE_WITHIN_BRACKETS",
                        "SPACE_WITHIN_BRACES",
                        "SPACE_WITHIN_PARENTHESES",
                        // Other non-operator signs
                        "SPACE_AROUND_ASSIGNMENT_OPERATORS",
//                        "SPACE_AFTER_SEMICOLON",
//                        "SPACE_BEFORE_SEMICOLON",
                        "SPACE_AFTER_COMMA",
                        "SPACE_BEFORE_COMMA",
                        // Operators
                        "SPACE_AROUND_ADDITIVE_OPERATORS",
                        "SPACE_AROUND_EQUALITY_OPERATORS",
                        "SPACE_AROUND_LOGICAL_OPERATORS",
                        "SPACE_AROUND_MULTIPLICATIVE_OPERATORS",
                        "SPACE_AROUND_RELATIONAL_OPERATORS",
                        "SPACE_AROUND_UNARY_OPERATOR"
                );

                consumer.renameStandardOption("SPACE_AROUND_ASSIGNMENT_OPERATORS", "Assignment operator (=)");
                consumer.renameStandardOption("SPACE_AROUND_MULTIPLICATIVE_OPERATORS", "Multiplicative operators (*, /)");
                consumer.renameStandardOption("SPACE_AROUND_UNARY_OPERATOR", "Unary operators (!, -)");

                consumer.showCustomOption(
                        NixCodeStyleSettings.class,
                        "SPACE_AFTER_SET_MODIFIER",
                        "After set modifier (rec, let)",
                        constants.SPACES_OTHER
                );
                consumer.showCustomOption(
                        NixCodeStyleSettings.class,
                        "SPACE_BEFORE_COLON_IN_LAMBDA",
                        "Before ':'",
                        "In function declaration"
                );
                consumer.showCustomOption(
                        NixCodeStyleSettings.class,
                        "SPACE_AFTER_COLON_IN_LAMBDA",
                        "After ':'",
                        "In function declaration"
                );
                consumer.showCustomOption(
                        NixCodeStyleSettings.class,
                        "SPACE_BEFORE_AT_SIGN_IN_LAMBDA",
                        "Before '@'",
                        "In function declaration"
                );
                consumer.showCustomOption(
                        NixCodeStyleSettings.class,
                        "SPACE_AFTER_AT_SIGN_IN_LAMBDA",
                        "After '@'",
                        "In function declaration"
                );
                consumer.showCustomOption(
                        NixCodeStyleSettings.class,
                        "SPACE_AROUND_CONCAT_OPERATOR",
                        "List concatenation operator (++)",
                        constants.SPACES_AROUND_OPERATORS
                );
                consumer.showCustomOption(
                        NixCodeStyleSettings.class,
                        "SPACE_AROUND_HAS_ATTR_OPERATOR",
                        "Has attribute operator (?)",
                        constants.SPACES_AROUND_OPERATORS
                );
                consumer.showCustomOption(
                        NixCodeStyleSettings.class,
                        "SPACE_AROUND_IMPLICATION_OPERATOR",
                        "Logical implication operator (->)",
                        constants.SPACES_AROUND_OPERATORS
                );
                consumer.showCustomOption(
                        NixCodeStyleSettings.class,
                        "SPACE_AROUND_UPDATE_ATTRS_OPERATOR",
                        "Update operator (//)",
                        constants.SPACES_AROUND_OPERATORS
                );
            }
            case BLANK_LINES_SETTINGS -> {
                consumer.showStandardOptions(
                        ""
                );
            }
            case WRAPPING_AND_BRACES_SETTINGS -> {
                consumer.showStandardOptions();

                consumer.showCustomOption(
                        NixCodeStyleSettings.class,
                        "ALIGN_ASSIGNMENTS",
                        "Align attributes in columns",
                        constants.WRAPPING_FIELDS_VARIABLES_GROUPS,
                        new String[]{
                                "Do not align",
                                "Align consecutive",
                                "Align siblings",
                                "Align nested",
                        },
                        new int[]{
                                NixCodeStyleSettings.AttributeAlignment.DO_NOT_ALIGN,
                                NixCodeStyleSettings.AttributeAlignment.ALIGN_CONSECUTIVE,
                                NixCodeStyleSettings.AttributeAlignment.ALIGN_SIBLINGS,
                                NixCodeStyleSettings.AttributeAlignment.ALIGN_NESTED,
                        }
                );
            }
            case COMMENTER_SETTINGS -> {
                consumer.showStandardOptions(
                        "LINE_COMMENT_ADD_SPACE",
                        "LINE_COMMENT_ADD_SPACE_ON_REFORMAT",
                        "LINE_COMMENT_AT_FIRST_COLUMN",
                        "BLOCK_COMMENT_AT_FIRST_COLUMN",
                        "BLOCK_COMMENT_ADD_SPACE"
                );
            }
        }
    }

    @Override
    public @NotNull String getCodeSample(@NotNull SettingsType settingsType) {
        // TODO Add example code
        return """
                {}
                """;
    }

    @Override
    public @NotNull CodeStyleConfigurable createConfigurable(
            @NotNull CodeStyleSettings settings,
            @NotNull CodeStyleSettings modelSettings
    ) {
        return new NixCodeStyleConfigurable(settings, modelSettings, getConfigurableDisplayName());
    }
}
