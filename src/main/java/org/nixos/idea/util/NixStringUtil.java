package org.nixos.idea.util;

import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.nixos.idea.psi.NixAntiquotation;
import org.nixos.idea.psi.NixIndString;
import org.nixos.idea.psi.NixPsiUtil;
import org.nixos.idea.psi.NixStdString;
import org.nixos.idea.psi.NixString;
import org.nixos.idea.psi.NixStringPart;
import org.nixos.idea.psi.NixStringText;
import org.nixos.idea.psi.NixTypes;

import java.util.function.IntSupplier;

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
        escapeStd(builder, unescaped, 0);
        builder.append('"');
        return builder.toString();
    }

    /**
     * Escapes the given string for use in a double-quoted string expression in the Nix Expression Language.
     * Note that it is not safe to combine the results of two method calls with arbitrary input.
     * For example, the following code would generate a broken result.
     * <pre>{@code
     *     StringBuilder b1 = new StringBuilder(), b2 = new StringBuilder();
     *     NixStringUtil.escapeStd(b1, "$");
     *     NixStringUtil.escapeStd(b2, "{''}");
     *     System.out.println(b1.toString() + b2.toString());
     * }</pre>
     * The result would be the following broken Nix code.
     * <pre>
     *     ${''}
     * </pre>
     *
     * @param builder   The target string builder. The result will be appended to the given string builder.
     * @param unescaped The raw string which shall be escaped.
     */
    public static void escapeStd(@NotNull StringBuilder builder, @NotNull CharSequence unescaped, int lookback) {
        boolean potentialInterpolation = false;
        for (int i = builder.length() - 1; lookback-- > 0 && builder.charAt(i) == '$'; i--) {
            potentialInterpolation = !potentialInterpolation;
        }
        for (int charIndex = 0; charIndex < unescaped.length(); charIndex++) {
            char nextChar = unescaped.charAt(charIndex);
            switch (nextChar) {
                case '"':
                case '\\':
                    builder.append('\\').append(nextChar);
                    break;
                case '{':
                    if (potentialInterpolation) {
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
            potentialInterpolation = nextChar == '$' && !potentialInterpolation;
        }
    }

    /**
     * Escapes the given string for use in an indented string expression in the Nix Expression Language.
     * Note that it is not safe to concat the result of two calls of this method.
     *
     * @param builder     The target string builder. The result will be appended to the given string builder.
     * @param unescaped   The raw string which shall be escaped.
     * @param indent      The number as spaces used for indentation
     * @param indentStart Whether the start of the string needs to be indented
     * @param indentEnd   The number as spaces used for indentation in the last line
     */
    public static void escapeInd(@NotNull StringBuilder builder, @NotNull CharSequence unescaped, int indent, boolean indentStart, int indentEnd, int lookback) {
        // TODO Document lookback. Highlight that it does not support ''' and other escape sequences
        String indentStr = " ".repeat(indent);
        boolean potentialInterpolation = false;
        boolean potentialClosing = lookback > 0 && builder.charAt(builder.length() - 1) == '\'';
        boolean quoteBeforeDollar = lookback > 1 && builder.charAt(builder.length() - 1) == '$' && builder.charAt(builder.length() - 2) == '\'';
        for (int i = builder.length() - 1; lookback-- > 0 && builder.charAt(i) == '$'; i--) {
            potentialInterpolation = !potentialInterpolation;
        }
        for (int charIndex = 0; charIndex < unescaped.length(); charIndex++) {
            char nextChar = unescaped.charAt(charIndex);
            if (indentStart && nextChar != '\n') {
                builder.append(indentStr);
                indentStart = false;
            }
            switch (nextChar) {
                case '\'':
                    // Convert `''` to `'''`
                    if (potentialClosing) {
                        builder.append('\'');
                    }
                    builder.append('\'');
                    break;
                case '{':
                    if (quoteBeforeDollar) {
                        // Convert `'${` to `'$''\{`
                        builder.append("''\\{");
                    } else if (potentialInterpolation) {
                        // Convert `${` to `''${`
                        builder.setLength(builder.length() - 1);
                        builder.append("''${");
                    } else {
                        // Leave `$${` untouched
                        builder.append('{');
                    }
                    break;
                case '\r':
                    if (potentialClosing) {
                        builder.append("'\\'");
                    }
                    builder.append("''\\r");
                    break;
                case '\t':
                    if (potentialClosing) {
                        builder.append("'\\'");
                    }
                    builder.append("''\\t");
                    break;
                case '\n':
                    indentStart = true;
                    builder.append('\n');
                    break;
                default:
                    builder.append(nextChar);
                    break;
            }
            quoteBeforeDollar = nextChar == '$' && potentialClosing;
            potentialInterpolation = nextChar == '$' && !potentialInterpolation;
            potentialClosing = nextChar == '\'' && !potentialClosing;
        }
        if (indentStart) {
            builder.repeat(" ", Math.min(indent, indentEnd));
        }
    }

    /**
     * Detects the indent of the given string.
     * The indent represents the amount of characters removed from the start of each line.
     * If all lines are blank, the indent is ambiguous.
     * In such case, the method returns the smallest number greater or equal to {@code fallback},
     * which also satisfies the language semantics of Nix.
     * <p>
     * You should probably use {@link NixPsiUtil#getIndent(NixString)},
     * instead of calling this method directly.
     *
     * @param string   the string from which to get the indentation
     * @param fallback supplier for the minimal indent used when no unambiguous indent was detected
     * @return the detected indentation, or {@link Integer#MAX_VALUE}
     */
    public static @Range(from = 0, to = Integer.MAX_VALUE) int detectIndent(@NotNull NixString string, @NotNull IntSupplier fallback) {
        if (string instanceof NixStdString) {
            return 0;
        } else if (string instanceof NixIndString) {
            int maxIndent = 0;
            int result = Integer.MAX_VALUE;
            int currentIndent = 0;
            for (NixStringPart part : string.getStringParts()) {
                if (part instanceof NixStringText textNode) {
                    for (ASTNode token = textNode.getNode().getFirstChildNode(); token != null; token = token.getTreeNext()) {
                        IElementType type = token.getElementType();
                        if (type == NixTypes.IND_STR_INDENT) {
                            currentIndent = token.getTextLength();
                            maxIndent = Math.max(maxIndent, currentIndent);
                        } else if (type == NixTypes.IND_STR_LF) {
                            currentIndent = 0;
                        } else {
                            assert type == NixTypes.IND_STR || type == NixTypes.IND_STR_ESCAPE : type;
                            result = Math.min(result, currentIndent);
                        }
                    }
                } else {
                    assert part instanceof NixAntiquotation : part.getClass();
                    result = Math.min(result, currentIndent);
                }
            }
            if (result == Integer.MAX_VALUE) {
                // We need to choose an indent which is large enough to strip all spaces.
                result = Math.max(fallback.getAsInt(), maxIndent);
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
        int indent = NixPsiUtil.getIndent((NixString) textNode.getParent());
        StringBuilder builder = new StringBuilder();
        for (ASTNode token = textNode.getNode().getFirstChildNode(); token != null; token = token.getTreeNext()) {
            builder.append(parse(token, indent));
        }
        return builder.toString();
    }

    public static @NotNull CharSequence parse(@NotNull ASTNode token, int indent) {
        CharSequence text = token.getChars();
        IElementType type = token.getElementType();
        if (type == NixTypes.STR || type == NixTypes.IND_STR || type == NixTypes.IND_STR_LF) {
            return text;
        } else if (type == NixTypes.IND_STR_INDENT) {
            int end = text.length();
            if (end > indent) {
                return text.subSequence(indent, end);
            }
            return "";
        } else if (type == NixTypes.STR_ESCAPE) {
            assert text.length() == 2 && text.charAt(0) == '\\' : text;
            char c = text.charAt(1);
            return unescape(c);
        } else if (type == NixTypes.IND_STR_ESCAPE) {
            return switch (text.charAt(2)) {
                case '$' -> {
                    assert "''$".contentEquals(text) : text;
                    yield "$";
                }
                case '\'' -> {
                    assert "'''".contentEquals(text) : text;
                    yield "''";
                }
                case '\\' -> {
                    assert text.length() == 4 && "''\\".contentEquals(text.subSequence(0, 3)) : text;
                    char c = text.charAt(3);
                    yield unescape(c);
                }
                default -> throw new IllegalStateException("Unknown escape sequence: " + text);
            };
        } else {
            throw new IllegalArgumentException("Unexpected string token: " + token);
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
}
