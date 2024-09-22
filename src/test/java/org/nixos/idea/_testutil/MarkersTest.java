package org.nixos.idea._testutil;

import com.intellij.openapi.util.TextRange;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MarkersTest {

    private static final Markers.TagName TAG_1 = Markers.tagName("tag1");
    private static final Markers.TagName TAG_2 = Markers.tagName("tag2");
    private static final Markers.TagName TAG_3 = Markers.tagName("tag3");
    private static final Markers.TagName TAG_POS = Markers.tagNameVoid("pos");

    @Test
    void testParse() {
        Markers markers = Markers.parse(
                "abc <tag1> def <pos> <tag2/> </tag1> ghi <tag2/>",
                TAG_1, TAG_2, TAG_POS);

        assertEquals("abc  def    ghi ", markers.unmarkedText());
        assertEquals(List.of(
                Markers.marker(TAG_1, 4, 11),
                Markers.marker(TAG_POS, 9, 9),
                Markers.marker(TAG_2, 10, 10),
                Markers.marker(TAG_2, 16, 16)
        ), markers.list());
    }

    @Test
    void testFilter() {
        Markers markers = Markers.parse(
                "abc <tag1> def <pos> <tag2/> </tag1> ghi <tag2/>",
                TAG_1, TAG_2, TAG_POS);

        assertEquals(List.of(
                Markers.marker(TAG_1, 4, 11)
        ), markers.markers(TAG_1).list());
        assertEquals(List.of(
                Markers.marker(TAG_2, 10, 10),
                Markers.marker(TAG_2, 16, 16)
        ), markers.markers(TAG_2).list());
        assertEquals(List.of(
                Markers.marker(TAG_POS, 9, 9)
        ), markers.markers(TAG_POS).list());
    }

    @Test
    void testToString() {
        Markers m = Markers.create("123456789", List.of(
                Markers.marker(TAG_1, 0, 5),
                Markers.marker(TAG_2, 0, 4),
                Markers.marker(TAG_3, 0, 6)
        ), TAG_1, TAG_2, TAG_3);

        assertEquals("<tag3><tag1><tag2>1234</tag2>5</tag1>6</tag3>789", m.toString());
    }

    @Test
    void testEqualsIgnoresOrder() {
        Markers m1 = Markers.create("0123456789", TAG_1, List.of(
                TextRange.create(0, 5),
                TextRange.create(0, 6)
        ));
        Markers m2 = Markers.create("0123456789", TAG_1, List.of(
                TextRange.create(0, 6),
                TextRange.create(0, 5)
        ));
        assertEquals(m1, m2);
    }
}
