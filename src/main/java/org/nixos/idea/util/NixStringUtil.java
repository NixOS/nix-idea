package org.nixos.idea.util;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.psi.NixAntiquotation;
import org.nixos.idea.psi.NixIndString;
import org.nixos.idea.psi.NixStdString;
import org.nixos.idea.psi.NixString;
import org.nixos.idea.psi.NixStringPart;
import org.nixos.idea.psi.NixStringText;
import org.nixos.idea.psi.NixTypes;

/**
 * Utilities for encoding and decoding strings in the Nix Expression Language.
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
     * Detects the maximal amount of characters removed from the start of the lines.
     * May return {@link Integer#MAX_VALUE} if the content of the string is blank.
     *
     * @param string the string from which to get the indentation
     * @return the detected indentation, or {@link Integer#MAX_VALUE}
     */
    public static int detectMaxIndent(@NotNull NixString string) {
        if (string instanceof NixStdString) {
            return 0;
        } else if (string instanceof NixIndString) {
            int result = Integer.MAX_VALUE;
            int preliminary = 0;
            for (NixStringPart part : string.getStringParts()) {
                if (part instanceof NixStringText textNode) {
                    for (ASTNode token = textNode.getNode().getFirstChildNode(); token != null; token = token.getTreeNext()) {
                        IElementType type = token.getElementType();
                        if (type == NixTypes.IND_STR_INDENT) {
                            preliminary = Math.min(result, token.getTextLength());
                        } else if (type == NixTypes.IND_STR_LF) {
                            preliminary = 0;
                        } else {
                            assert type == NixTypes.IND_STR || type == NixTypes.IND_STR_ESCAPE : type;
                            result = preliminary;
                        }
                    }
                } else {
                    assert part instanceof NixAntiquotation : part.getClass();
                    result = preliminary;
                }
            }
            return result;
        } else {
            throw new IllegalStateException("Unexpected subclass of NixString: " + string.getClass());
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
        int maxIndent = detectMaxIndent((NixString) textNode.getParent());
        StringBuilder builder = new StringBuilder();
        visit(new StringVisitor() {
            @Override
            public boolean text(@NotNull CharSequence text, int offset) {
                builder.append(text);
                return true;
            }

            @Override
            public boolean escapeSequence(@NotNull String text, int offset, @NotNull CharSequence escapeSequence) {
                builder.append(text);
                return true;
            }
        }, textNode, maxIndent);
        return builder.toString();
    }

    public static void visit(@NotNull StringVisitor visitor, @NotNull NixStringText textNode, int maxIndent) {
        int offset = 0;
        for (ASTNode child = textNode.getNode().getFirstChildNode(); child != null; child = child.getTreeNext()) {
            if (!parse(visitor, child, offset, maxIndent)) {
                break;
            }
            offset += child.getTextLength();
        }
    }

    private static boolean parse(@NotNull StringVisitor visitor, @NotNull ASTNode token, int offset, int maxIndent) {
        CharSequence text = token.getChars();
        IElementType type = token.getElementType();
        if (type == NixTypes.STR || type == NixTypes.IND_STR || type == NixTypes.IND_STR_LF) {
            return visitor.text(text, offset);
        } else if (type == NixTypes.IND_STR_INDENT) {
            int end = text.length();
            if (end > maxIndent) {
                CharSequence remain = text.subSequence(maxIndent, end);
                return visitor.text(remain, offset + maxIndent);
            }
            return true;
        } else if (type == NixTypes.STR_ESCAPE) {
            assert text.length() == 2 && text.charAt(0) == '\\' : text;
            char c = text.charAt(1);
            return visitor.escapeSequence(unescape(c), offset, text);
        } else if (type == NixTypes.IND_STR_ESCAPE) {
            return switch (text.charAt(2)) {
                case '$' -> {
                    assert "''$".contentEquals(text) : text;
                    yield visitor.escapeSequence("$", offset, text);
                }
                case '\'' -> {
                    assert "'''".contentEquals(text) : text;
                    yield visitor.escapeSequence("''", offset, text);
                }
                case '\\' -> {
                    assert text.length() == 4 && "''\\".contentEquals(text.subSequence(0, 3)) : text;
                    char c = text.charAt(3);
                    yield visitor.escapeSequence(unescape(c), offset, text);
                }
                default -> throw new IllegalStateException("Unknown escape sequence: " + text);
            };
        } else {
            throw new IllegalStateException("Unexpected token in string: " + token);
        }
    }

    private static @NotNull String unescape(char c) {
        return switch (c) {
            case 'n' -> "\n";
            case 'r' -> "\r";
            case 't' -> "\t";
            default -> String.valueOf(c);
        };
    }

    public interface StringVisitor {
        boolean text(@NotNull CharSequence text, int offset);

        boolean escapeSequence(@NotNull String text, int offset, @NotNull CharSequence escapeSequence);
    }
}
