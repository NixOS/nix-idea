package org.nixos.idea.interpretation;

import org.apache.commons.lang3.function.ToBooleanBiFunction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.psi.NixAntiquotation;
import org.nixos.idea.psi.NixAttr;
import org.nixos.idea.psi.NixIdentifier;
import org.nixos.idea.psi.NixStdAttr;
import org.nixos.idea.psi.NixStdString;
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

public final class Attribute implements Comparable<Attribute> {
    private static final char INTERPOLATION_PLACEHOLDER = '*';
    private static final int INTERPOLATION_PLACEHOLDER_LENGTH = 1;

    private final @NotNull String @NotNull [] myStringParts;
    private final @NotNull String @NotNull [] myRawStringParts;
    private final boolean myInQuotes;

    private Attribute(@NotNull Builder builder) {
        int size = builder.myStringParts.size();
        assert builder.myRawStringParts == null || builder.myRawStringParts.size() == size;
        myStringParts = new String[size + 1];
        Collections.copy(Arrays.asList(myStringParts), builder.myStringParts);
        myStringParts[size] = builder.myNextString;
        if (builder.myRawStringParts == null) {
            myInQuotes = false;
            myRawStringParts = myStringParts;
        } else {
            myInQuotes = true;
            myRawStringParts = new String[size + 1];
            Collections.copy(Arrays.asList(myRawStringParts), builder.myRawStringParts);
            myRawStringParts[size] = builder.myNextRawString;
        }
    }

    public static @NotNull Attribute of(@NotNull NixIdentifier element) {
        return new Builder(false).addString(element.getText()).build();
    }

    public static @NotNull Attribute of(@NotNull NixStdAttr element) {
        return of((NixIdentifier) element);
    }

    public static @NotNull Attribute of(@NotNull NixAttr element) {
        if (element instanceof NixStdAttr) {
            return of((NixStdAttr) element);
        }
        else if (element instanceof NixStringAttr) {
            return of((NixStringAttr) element);
        }
        throw new IllegalStateException("unknown attribute type");
    }

    public static @NotNull Attribute of(@NotNull NixStringAttr element) {
        NixAntiquotation antiquotation = element.getAntiquotation();
        if (antiquotation != null) {
            assert element.getStdString() == null;
            return new Builder(false).addInterpolation().build();
        }
        else {
            NixStdString string = Objects.requireNonNull(element.getStdString());
            Attribute.Builder builder = new Builder(true);
            for (NixStringPart part : string.getStringPartList()) {
                if (part instanceof NixStringText) {
                    builder.addString(NixStringUtil.parse((NixStringText) part), part.getText());
                }
                else {
                    assert part instanceof NixAntiquotation;
                    builder.addInterpolation();
                }
            }
            return builder.build();
        }
    }

    @Contract(pure = true)
    public boolean hasQuotes() {
        return myInQuotes;
    }

    @Contract(pure = true)
    public boolean hasInterpolation() {
        return myStringParts.length > 1;
    }

    @Contract(pure = true)
    public @Nullable Object getKey() {
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
                smallerOnLarger(myStringParts[myStringParts.length - 1], other.myStringParts[other.myStringParts.length - 1], String::startsWith)) {
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
            pos += match + currentPart.length();
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
    public int compareTo(@NotNull Attribute other) {
        for (int i = 0; ; i++) {
            if (i < myStringParts.length && i < other.myStringParts.length) {
                //noinspection StringEquality
                if (myStringParts[i] != other.myStringParts[i]) {
                    int stringComparison;
                    if (myStringParts[i] == null) {
                        return 1;
                    } else if (other.myStringParts[i] == null) {
                        return -1;
                    } else if ((stringComparison = myStringParts[i].compareTo(other.myStringParts[i])) != 0) {
                        return stringComparison;
                    }
                }
            } else {
                return Integer.compare(myStringParts.length, other.myStringParts.length);
            }
        }
    }

    @Override
    @Contract(pure = true)
    public String toString() {
        StringBuilder builder = new StringBuilder();
        assert myStringParts.length >= 1;
        if (myInQuotes) {
            builder.append('"');
            for (String rawPart : myRawStringParts) {
                builder.append(rawPart).append(INTERPOLATION_PLACEHOLDER);
            }
            builder.setLength(builder.length() - INTERPOLATION_PLACEHOLDER_LENGTH);
            builder.append('"');
        } else {
            for (String part : myStringParts) {
                builder.append(part);
                builder.append(INTERPOLATION_PLACEHOLDER);
            }
            builder.setLength(builder.length() - INTERPOLATION_PLACEHOLDER_LENGTH);
        }
        return builder.toString();
    }

    private static final class Builder {
        private static final String EMPTY_STRING = "";

        private final @NotNull List<String> myStringParts = new ArrayList<>();
        private final @Nullable List<String> myRawStringParts;
        private @NotNull String myNextString = EMPTY_STRING;
        private @NotNull String myNextRawString = EMPTY_STRING;

        private Builder(boolean inQuotes) {
            this.myRawStringParts = inQuotes ? new ArrayList<>() : null;
        }

        @Contract("-> this")
        private @NotNull Builder addInterpolation() {
            myStringParts.add(myNextString);
            if (myRawStringParts != null) {
                myRawStringParts.add(myNextRawString);
            }
            return this;
        }

        @Contract("_ -> this")
        private @NotNull Builder addString(@NotNull String str) {
            if (myRawStringParts != null) {
                throw new IllegalStateException("Attribute.Builder.addString(String) only available for unquoted attributes. " +
                        "Use Attribute.Builder.addString(String, String) instead.");
            }
            myNextString = concat(myNextString, str);
            return this;
        }

        @Contract("_, _ -> this")
        private @NotNull Builder addString(@NotNull String str, @NotNull String raw) {
            if (myRawStringParts == null) {
                throw new IllegalStateException("Attribute.Builder.addString(String, String) only available for quoted attributes. " +
                        "Use Attribute.Builder.addString(String) instead.");
            }
            myNextString = concat(myNextString, str);
            myNextRawString = concat(myNextString, raw);
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
