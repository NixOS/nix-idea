package org.nixos.idea.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A value which may either be true, false or unknown.
 */
public enum TriState {
    TRUE(0b11),
    FALSE(0b00),
    MAYBE(0b10),

    ;

    private static final int FLAG_MAY = 0b10;
    private static final int FLAG_DEFINITELY = 0b01;
    private static final TriState[] LOOKUP = {
            FALSE, // 0: 0b00
            null,  // 1: 0b01 (impossible)
            MAYBE, // 2: 0b10
            TRUE,  // 3: 0b11
    };

    private final int myFlags;

    TriState(int flags) {
        this.myFlags = flags;
    }

    public boolean definitely() {
        return this == TRUE;
    }

    public boolean definitelyNot() {
        return this == FALSE;
    }

    public boolean may() {
        return (myFlags & FLAG_MAY) != 0;
    }

    public boolean mayNot() {
        return (myFlags & FLAG_DEFINITELY) == 0;
    }

    public @NotNull TriState and(@NotNull TriState other) {
        return Objects.requireNonNull(LOOKUP[myFlags & other.myFlags]);
    }

    public @NotNull TriState or(@NotNull TriState other) {
        return Objects.requireNonNull(LOOKUP[myFlags | other.myFlags]);
    }
}
