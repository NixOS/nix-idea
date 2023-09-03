package org.nixos.idea.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TriStateTest {

    @ParameterizedTest(name = "[{index}] {0} && {1} = {2}")
    @CsvSource({
            "TRUE, TRUE, TRUE",
            "TRUE, MAYBE, MAYBE",
            "MAYBE, TRUE, MAYBE",
            "MAYBE, MAYBE, MAYBE",
            "TRUE, FALSE, FALSE",
            "FALSE, TRUE, FALSE",
            "MAYBE, FALSE, FALSE",
            "FALSE, MAYBE, FALSE",
            "FALSE, FALSE, FALSE",
    })
    void and(TriState a, TriState b, TriState result) {
        assertEquals(result, a.and(b));
    }

    @ParameterizedTest(name = "[{index}] {0} || {1} = {2}")
    @CsvSource({
            "TRUE, TRUE, TRUE",
            "TRUE, MAYBE, TRUE",
            "MAYBE, TRUE, TRUE",
            "MAYBE, MAYBE, MAYBE",
            "TRUE, FALSE, TRUE",
            "FALSE, TRUE, TRUE",
            "MAYBE, FALSE, MAYBE",
            "FALSE, MAYBE, MAYBE",
            "FALSE, FALSE, FALSE",
    })
    void or(TriState a, TriState b, TriState result) {
        assertEquals(result, a.or(b));
    }
}
