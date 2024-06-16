package org.nixos.idea.util;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.psi.NixStringText;
import org.nixos.idea.psi.NixTypes;

/**
 * Utilities for strings in the Nix Expression Language.
 */
public final class NixStringUtil {

    private NixStringUtil() {} // Cannot be instantiated

    /**
     * Returns the source code for a string in the Nix Expression Language.
     * When the returned string is evaluated by a Nix interpreter, the result matches the sting given to this method.
     * The returned string expression is always a double-quoted string.
     *
     * <h4>Example</h4>
     * <pre>{@code System.out.println(quote("This should be escaped: ${}"));}</pre>
     * The code above prints the following:
     * <pre>"This should be escaped: \${}"</pre>
     *
     * @param unescaped The raw string which shall be the result when the expression is evaluated.
     * @return Source code for a Nix expression which evaluates to the given string.
     */
    @Contract(pure = true)
    public static @NotNull String quote(@NotNull CharSequence unescaped) {
        StringBuilder builder = new StringBuilder();
        builder.append('"');
        escape(builder, unescaped);
        builder.append('"');
        return builder.toString();
    }

    /**
     * Escapes the given string for use in a double-quoted string expression in the Nix Expression Language.
     * Note that it is not safe to combine the results of two method calls with arbitrary input.
     * For example, the following code would generate a broken result.
     * <pre>{@code
     *     StringBuilder b1 = new StringBuilder(), b2 = new StringBuilder();
     *     NixStringUtil.escape(b1, "$");
     *     NixStringUtil.escape(b2, "{''}");
     *     System.out.println(b1.toString() + b2.toString());
     * }</pre>
     * The result would be the following broken Nix code.
     * <pre>
     *     "${''}"
     * </pre>
     *
     * @param builder   The target string builder. The result will be appended to the given string builder.
     * @param unescaped The raw string which shall be escaped.
     */
    public static void escape(@NotNull StringBuilder builder, @NotNull CharSequence unescaped) {
        for (int charIndex = 0; charIndex < unescaped.length(); charIndex++) {
            char nextChar = unescaped.charAt(charIndex);
            switch (nextChar) {
                case '"':
                case '\\':
                    builder.append('\\').append(nextChar);
                    break;
                case '{':
                    if (builder.charAt(builder.length() - 1) == '$') {
                        builder.setCharAt(builder.length() - 1, '\\');
                        builder.append('$').append('{');
                    } else {
                        builder.append('{');
                    }
                    break;
                case '\n':
                    builder.append('\\').append('n');
                    break;
                case '\r':
                    builder.append('\\').append('r');
                    break;
                case '\t':
                    builder.append('\\').append('t');
                    break;
                default:
                    builder.append(nextChar);
                    break;
            }
        }
    }

    /**
     * Returns the content of the given part of a string in the Nix Expression Language.
     * All escape sequences are resolved.
     *
     * @param textNode A part of a string.
     * @return The resulting string after resolving all escape sequences.
     */
    public static @NotNull String parse(@NotNull NixStringText textNode) {
        StringBuilder builder = new StringBuilder();
        for (ASTNode child = textNode.getNode().getFirstChildNode(); child != null; child = child.getTreeNext()) {
            parse(builder, child);
        }
        return builder.toString();
    }

    private static void parse(@NotNull StringBuilder builder, @NotNull ASTNode token) {
        CharSequence text = token.getChars();
        IElementType type = token.getElementType();
        if (type == NixTypes.STR || type == NixTypes.IND_STR) {
            builder.append(text);
        } else if (type == NixTypes.STR_ESCAPE) {
            assert text.length() == 2 && text.charAt(0) == '\\' : text;
            char c = text.charAt(1);
            builder.append(unescape(c));
        } else if (type == NixTypes.IND_STR_ESCAPE) {
            assert text.length() == 3 && ("''$".contentEquals(text) || "'''".contentEquals(text)) ||
                    text.length() == 4 && "''\\".contentEquals(text.subSequence(0, 3)) : text;
            if ("'''".contentEquals(text)){
                builder.append("''");
                return;
            }
            char c = text.charAt(text.length() - 1);
            builder.append(unescape(c));
        } else {
            throw new IllegalStateException("Unexpected token in string: " + token);
        }
    }

    private static char unescape(char c) {
        return switch (c) {
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            default -> c;
        };
    }
}
