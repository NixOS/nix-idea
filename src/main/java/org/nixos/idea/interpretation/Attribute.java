package org.nixos.idea.interpretation;

import com.google.errorprone.annotations.Immutable;
import org.apache.commons.lang3.function.ToBooleanBiFunction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.psi.NixAntiquotation;
import org.nixos.idea.psi.NixAttr;
import org.nixos.idea.psi.NixExpr;
import org.nixos.idea.psi.NixIdentifier;
import org.nixos.idea.psi.NixIndString;
import org.nixos.idea.psi.NixStdAttr;
import org.nixos.idea.psi.NixStdString;
import org.nixos.idea.psi.NixString;
import org.nixos.idea.psi.NixStringAttr;
import org.nixos.idea.psi.NixStringPart;
import org.nixos.idea.psi.NixStringText;
import org.nixos.idea.util.NixStringUtil;
import org.nixos.idea.util.TriState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable attribute class. Attributes can be generated from {@link NixAttr} or {@link NixIdentifier}.
 * You can compare two attributes by calling {@link #matches(Attribute)}.
 * This class does not implement {@link #equals(Object)} or {@link #hashCode()} because
 * the match my not be conclusive for {@linkplain #isDynamic() dynamic attributes}.
 *
 * @see AttributePath
 * @see AttributeMap
 */
@Immutable
public final class Attribute {
    private static final char INTERPOLATION_PLACEHOLDER = '*';
    private static final int INTERPOLATION_PLACEHOLDER_LENGTH = 1;

    private final @NotNull Type myType;
    private final @NotNull String @NotNull [] myStringParts;
    private final @NotNull String @NotNull [] myRawStringParts;

    private Attribute(@NotNull Builder builder) {
        int size = builder.myStringParts.size();
        assert builder.myRawStringParts == null || builder.myRawStringParts.size() == size;
        myType = builder.myType;
        myStringParts = new String[size + 1];
        Collections.copy(Arrays.asList(myStringParts), builder.myStringParts);
        myStringParts[size] = builder.myNextString;
        if (builder.myRawStringParts == null) {
            myRawStringParts = myStringParts;
        } else {
            myRawStringParts = new String[size + 1];
            Collections.copy(Arrays.asList(myRawStringParts), builder.myRawStringParts);
            myRawStringParts[size] = builder.myNextRawString;
        }
    }

    public static @NotNull Attribute of(@NotNull NixIdentifier element) {
        return new Builder(Type.ID).addString(element.getText()).build();
    }

    public static @NotNull Attribute of(@NotNull NixStdAttr element) {
        return of((NixIdentifier) element);
    }

    public static @NotNull Attribute of(@NotNull NixAttr element) {
        if (element instanceof NixStdAttr) {
            return of((NixStdAttr) element);
        } else if (element instanceof NixStringAttr) {
            return of((NixStringAttr) element);
        }
        throw new IllegalStateException("unknown attribute type");
    }

    public static @NotNull Attribute of(@NotNull NixStringAttr element) {
        NixAntiquotation interpolation = element.getAntiquotation();
        if (interpolation != null) {
            assert element.getStdString() == null;
            NixExpr expression = interpolation.getExpr();
            if (expression instanceof NixString) {
                Type type = expression instanceof NixIndString ? Type.INTERPOLATION_STR_IND : Type.INTERPOLATION_STR_STD;
                return new Builder(type).addString((NixString) expression).build();
            } else {
                return new Builder(Type.INTERPOLATION).addInterpolation(interpolation).build();
            }
        } else {
            NixStdString string = Objects.requireNonNull(element.getStdString());
            Attribute.Builder builder = new Builder(Type.STR);
            builder.addString(string);
            return builder.build();
        }
    }

    /**
     * Returns whether the attribute is a simple identifier.
     * <table>
     *     <caption>Return value for various example attributes</caption>
     *     <tr>
     *         <th scope="col">Attribute</th>
     *         <th scope="col">Return value</th>
     *     </tr>
     *     <tr>
     *         <td>{@code abc}</td>
     *         <td>{@code true}</td>
     *     </tr>
     *     <tr>
     *         <td>{@code "abc"}</td>
     *         <td>{@code false}</td>
     *     </tr>
     *     <tr>
     *         <td>{@code ${"abc"}}</td>
     *         <td>{@code false}</td>
     *     </tr>
     *     <tr>
     *         <td>{@code ${''abc''}}</td>
     *         <td>{@code false}</td>
     *     </tr>
     *     <tr>
     *         <td>{@code ${x}}</td>
     *         <td>{@code false}</td>
     *     </tr>
     * </table>
     *
     * @return {@code true} if the attribute is a simple identifier, {@code false} otherwise.
     */
    @Contract(pure = true)
    public boolean isIdentifier() {
        return myType == Type.ID;
    }

    /**
     * Returns whether the attribute name is enclosed in quotes.
     * There is a special case for non-dynamic attributes using an interpolation.
     * In such case, the attribute is treated as if the surrounding interpolation does not exist,
     * meaning {@code ${"…"}} is effectively equivalent to {@code "…"}
     * <table>
     *     <caption>Return value for various example attributes</caption>
     *     <tr>
     *         <th scope="col">Attribute</th>
     *         <th scope="col">Return value</th>
     *     </tr>
     *     <tr>
     *         <td>{@code xyz}</td>
     *         <td>{@code false}</td>
     *     </tr>
     *     <tr>
     *         <td>{@code ""}</td>
     *         <td>{@code true}</td>
     *     </tr>
     *     <tr>
     *         <td>{@code "42"}</td>
     *         <td>{@code true}</td>
     *     </tr>
     *     <tr>
     *         <td>{@code "a"}</td>
     *         <td>{@code true}</td>
     *     </tr>
     *     <tr>
     *         <td>{@code ${"a"}}</td>
     *         <td>{@code true} <small>(special case described above)</small></td>
     *     </tr>
     *     <tr>
     *         <td>{@code ${''a''}}</td>
     *         <td>{@code true} <small>(special case described above)</small></td>
     *     </tr>
     *     <tr>
     *         <td>{@code ${"a" + "b"}}</td>
     *         <td>{@code false}</td>
     *     </tr>
     * </table>
     *
     * @return {@code true} if the attribute name is enclosed in quotes, {@code false} otherwise.
     */
    @Contract(pure = true)
    public boolean hasQuotes() {
        return myType == Type.STR || myType == Type.INTERPOLATION_STR_STD || myType == Type.INTERPOLATION_STR_IND;
    }

    /**
     * Returns whether the attribute is dynamic.
     * Dynamic attributes do not have a {@linkplain #getName() plain name}.
     * Obtaining the actual name of the attribute would require a generic evaluation of the expression.
     * Attributes become dynamic if they contain an interpolation with any expression except a string literal.
     * <table>
     *     <caption>Return value for various example attributes</caption>
     *     <tr>
     *         <th scope="col">Attribute</th>
     *         <th scope="col">Return value</th>
     *     </tr>
     *     <tr>
     *         <td>{@code abc}</td>
     *         <td>{@code false}</td>
     *     </tr>
     *     <tr>
     *         <td>{@code "abc"}</td>
     *         <td>{@code false}</td>
     *     </tr>
     *     <tr>
     *         <td>{@code ${abc}}</td>
     *         <td>{@code true}</td>
     *     </tr>
     *     <tr>
     *         <td>{@code ${"abc"}}</td>
     *         <td>{@code false}</td>
     *     </tr>
     *     <tr>
     *         <td>{@code ${''abc''}}</td>
     *         <td>{@code false}</td>
     *     </tr>
     *     <tr>
     *         <td>{@code "${"abc"}"}</td>
     *         <td>{@code true}</td>
     *     </tr>
     *     <tr>
     *         <td>{@code ${"a${"b"}c"}}</td>
     *         <td>{@code true}</td>
     *     </tr>
     *     <tr>
     *         <td>{@code ${"ab" + "c"}}</td>
     *         <td>{@code true}</td>
     *     </tr>
     * </table>
     * The most significant limitation of dynamic attributes in Nix is that
     * you cannot use dynamic attributes when defining variables in {@code let}-expressions.
     *
     * @return {@code true} if the attribute is dynamic, {@code false} otherwise.
     */
    @Contract(pure = true)
    public boolean isDynamic() {
        return myStringParts.length > 1;
    }

    /**
     * Returns the plain name of the attribute.
     * The name is {@code null} if and only if this is a {@linkplain #isDynamic() dynamic attribute}.
     *
     * @return the name of the attribute, or {@code null}.
     */
    @Contract(pure = true)
    public @Nullable String getName() {
        return myStringParts.length == 1 ? myStringParts[0] : null;
    }

    @Contract(pure = true)
    public @NotNull TriState matches(@NotNull Attribute other) {
        if (this == other) {
            return TriState.TRUE;
        } else if (myStringParts.length == 1 && other.myStringParts.length == 1) {
            return myStringParts[0].equals(other.myStringParts[0]) ? TriState.TRUE : TriState.FALSE;
        } else if (myStringParts.length == 1) {
            return matches0(myStringParts[0], other.myStringParts);
        } else if (other.myStringParts.length == 1) {
            return matches0(other.myStringParts[0], myStringParts);
        } else if (smallerOnLarger(myStringParts[0], other.myStringParts[0], String::startsWith) &&
                smallerOnLarger(myStringParts[myStringParts.length - 1], other.myStringParts[other.myStringParts.length - 1], String::endsWith)) {
            return TriState.MAYBE;
        } else {
            return TriState.FALSE;
        }
    }

    private static @NotNull TriState matches0(@NotNull String fullString, @NotNull String @NotNull [] parts) {
        if (!fullString.startsWith(parts[0])) {
            return TriState.FALSE;
        }
        int pos = parts[0].length();
        for (int i = 1; i < parts.length; i++) {
            String currentPart = parts[i];
            int match = fullString.indexOf(currentPart, pos);
            if (match == -1) {
                return TriState.FALSE;
            }
            pos = match + currentPart.length();
        }
        assert pos <= fullString.length();
        if (pos == fullString.length() || fullString.endsWith(parts[parts.length - 1])) {
            return TriState.MAYBE;
        }
        return TriState.FALSE;
    }

    private static boolean smallerOnLarger(@NotNull String first, @NotNull String second, @NotNull ToBooleanBiFunction<String, String> operation) {
        if (first.length() < second.length()) {
            return operation.applyAsBoolean(second, first);
        } else {
            return operation.applyAsBoolean(first, second);
        }
    }

    @Override
    @Contract(pure = true)
    public String toString() {
        StringBuilder builder = new StringBuilder();
        assert myStringParts.length >= 1;
        builder.append(myType.prefix);
        for (String part : myRawStringParts) {
            builder.append(part).append(INTERPOLATION_PLACEHOLDER);
        }
        builder.setLength(builder.length() - INTERPOLATION_PLACEHOLDER_LENGTH);
        builder.append(myType.suffix);
        return builder.toString();
    }

    private enum Type {
        ID("", ""),
        STR("\"", "\""),
        INTERPOLATION("", ""),
        INTERPOLATION_STR_STD("${\"", "\"}"),
        INTERPOLATION_STR_IND("${''", "''}"),

        ;

        private final String prefix;
        private final String suffix;

        Type(String prefix, String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
        }
    }

    private static final class Builder {
        private static final String EMPTY_STRING = "";

        private final @NotNull Type myType;
        private final @NotNull List<String> myStringParts = new ArrayList<>();
        private final @Nullable List<String> myRawStringParts;
        private @NotNull String myNextString = EMPTY_STRING;
        private @NotNull String myNextRawString = EMPTY_STRING;

        private Builder(@NotNull Type type) {
            myType = type;
            myRawStringParts = type == Type.ID ? null : new ArrayList<>();
        }

        @Contract("_ -> this")
        private @NotNull Builder addInterpolation(@NotNull NixAntiquotation ignore) {
            myStringParts.add(myNextString);
            myNextString = EMPTY_STRING;
            if (myRawStringParts != null) {
                myRawStringParts.add(myNextRawString);
                myNextRawString = EMPTY_STRING;
            }
            return this;
        }

        @Contract("_ -> this")
        private @NotNull Builder addString(@NotNull NixString string) {
            if (myRawStringParts == null) {
                throw new IllegalStateException("Attribute.Builder.addString(NixString) not available for Type.ID. " +
                        "Use Attribute.Builder.addString(String) instead.");
            }
            for (NixStringPart part : string.getStringParts()) {
                if (part instanceof NixStringText) {
                    myNextString = concat(myNextString, NixStringUtil.parse((NixStringText) part));
                    myNextRawString = concat(myNextRawString, part.getText());
                } else {
                    addInterpolation((NixAntiquotation) part);
                }
            }
            return this;
        }

        @Contract("_ -> this")
        private @NotNull Builder addString(@NotNull String str) {
            if (myRawStringParts != null) {
                throw new IllegalStateException("Attribute.Builder.addString(String) only available for Type.ID. " +
                        "Use Attribute.Builder.addString(NixString) instead.");
            }
            myNextString = concat(myNextString, str);
            return this;
        }

        private @NotNull String concat(@NotNull String first, @NotNull String second) {
            return first.equals(EMPTY_STRING) ? second : first + second;
        }

        @Contract(pure = true)
        private @NotNull Attribute build() {
            return new Attribute(this);
        }
    }
}
