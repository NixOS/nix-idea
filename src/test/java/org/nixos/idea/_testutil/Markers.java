package org.nixos.idea._testutil;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.IntRef;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.util.DocumentUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Text with XML-style markers.
 * <pre>{@code
 * The <adjective>quick</adjective> <noun>fox</noun> jumps over the <adjective>lazy</adjective> <noun>dog</noun>.
 * }</pre>
 * Before you can create an instance of this class, you must declare the different types of markers.
 * <pre>{@code
 * TagName TAG_NOUN = Markers.tagName("noun");
 * TagName TAG_ADJECTIVE = Markers.tagName("adjective");
 * }</pre>
 * Afterward, you can parse the text from above using {@link #parse(String, TagName...)}.
 * You can also use {@link #create(String)} and {@link #withMarkers(Collection, Stream)} to create an instance from known ranges.
 * When you have created an instance of this class, you can use it to obtain the positions of the different markers,
 * or compare it with another instance using {@link #equals(Object)}.
 * <pre>{@code
 * void testNounDetection(String markedText) {
 *     Markers markers = Markers.parse(markedText, TAG_NOUN, TAG_ADJECTIVE);
 *     Collection<TextRange> detectedNouns = runNounDetection(markers.unmarkedText());
 *     assertEquals(markers.markers(TAG_NOUN), Markers.create(markers.unmarkedText(), TAG_NOUN, detectedNouns));
 * }}</pre>
 */
public final class Markers extends AbstractCollection<Markers.Marker> {

    private final @NotNull String myText;
    private final @NotNull Set<TagName> myKnownTagNames;
    private final @NotNull List<Marker> myMarkers;

    private Markers(@NotNull String text, @NotNull Set<TagName> tagNames, @NotNull Stream<Marker> markers) {
        myText = text;
        myKnownTagNames = Set.copyOf(tagNames);
        myMarkers = markers
                .sorted(Comparator.comparingInt(Marker::start)
                        .thenComparing(Comparator.comparingInt(Marker::end).reversed()))
                .toList();
    }

    //region Factory methods

    /**
     * Declares a new type of marker.
     *
     * @param tagName The name used for the tags of the marker.
     * @return The type of the marker.
     * @see #tagNameVoid(String)
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull TagName tagName(@NotNull String tagName) {
        return new TagName(tagName, true);
    }

    /**
     * Declares a new type of marker.
     * In contrast to {@link #tagName(String)}, this marker can not have any content.
     * They behave similar to void elements in HTML.
     * They must not have an end-tag and only represent a single offset instead of a range.
     *
     * @param tagName The name used for the tags of the marker.
     * @return The type of the marker.
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull TagName tagNameVoid(@NotNull String tagName) {
        return new TagName(tagName, false);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Marker marker(@NotNull TagName tagName, int offset) {
        return new Marker(tagName, offset, offset);
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    public static @NotNull Marker marker(@NotNull TagName tagName, int start, int end) {
        return new Marker(tagName, start, end);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Marker marker(@NotNull TagName tagName, @NotNull TextRange range) {
        return new Marker(tagName, range.getStartOffset(), range.getEndOffset());
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Markers create(@NotNull String text) {
        return new Markers(text, Set.of(), Stream.empty());
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    public static @NotNull Markers create(@NotNull String text, @NotNull TagName tagName, @NotNull Collection<TextRange> ranges) {
        return Markers.create(text).withMarkers(tagName, ranges);
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    public static @NotNull Markers create(@NotNull String text, @NotNull Collection<Marker> markers, @NotNull TagName... tagNames) {
        return create(text, markers.stream(), tagNames);
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    public static @NotNull Markers create(@NotNull String text, @NotNull Stream<Marker> markers, @NotNull TagName... tagNames) {
        return Markers.create(text).withMarkers(markers, tagNames);
    }

    @Contract(value = "_, _ -> new", pure = true)
    public static @NotNull Markers parse(@NotNull String input, @NotNull TagName... tagNames) {
        List<Token> tokens = tokenize(input, tagNames);
        return parse(tokens, tagNames);
    }

    public static @NotNull Markers extract(@NotNull CodeInsightTestFixture fixture, @NotNull PsiFile file, @NotNull TagName... tagNames) {
        return extract(fixture, fixture.getDocument(file), tagNames);
    }

    /**
     * Extracts the markers from the given document.
     * In contrast to {@link #parse(String, TagName...)},
     * this method modifies the given document by removing the markers.
     * After this method call, the given document will contain the same text as returned by {@link #unmarkedText()}.
     *
     * @param fixture  The test fixture which holds the given document. This instance is used to commit the document and update the PSI nodes.
     * @param document The document which contains the marked text.
     * @param tagNames The marker types which shall be extracted.
     * @return A new instance of this class representing the marked text provided by the given document.
     * @see #extract(CodeInsightTestFixture, PsiFile, TagName...)
     */
    public static @NotNull Markers extract(@NotNull CodeInsightTestFixture fixture, @NotNull Document document, @NotNull TagName... tagNames) {
        return WriteCommandAction.runWriteCommandAction(fixture.getProject(), (Computable<Markers>) () -> {
            List<Token> tokens = tokenize(document.getText(), tagNames);
            DocumentUtil.executeInBulk(document, () -> {
                int offset = 0;
                for (Token token : tokens) {
                    if (token instanceof Tag) {
                        document.deleteString(offset, offset + token.originalSize());
                    } else {
                        offset += token.originalSize();
                    }
                }
            });
            PsiDocumentManager.getInstance(fixture.getProject()).commitAllDocuments();
            return parse(tokens, tagNames);
        });
    }

    @Contract(value = "_, _ -> new", pure = true)
    private static @NotNull List<Token> tokenize(@NotNull String input, @NotNull TagName... tagNames) {
        Map<String, TagName> tags = Arrays.stream(tagNames)
                .collect(Collectors.toUnmodifiableMap(tagName -> tagName.myName, Function.identity()));
        List<Token> result = new ArrayList<>();
        Matcher matcher = Pattern.compile("<(?<close>/)?(?<name>" + TagName.PATTERN_NAME.pattern() + ")\\s*(?<selfClose>/)?>").matcher(input);
        int lastTagEnd = 0;
        for (int offset = 0; matcher.find(offset); offset = matcher.end()) {
            boolean close = matcher.group("close") != null;
            boolean selfClose = matcher.group("selfClose") != null;
            TagName tagName = tags.get(matcher.group("name"));
            if (tagName == null || close && selfClose) {
                continue;
            }

            if (matcher.start() != lastTagEnd) {
                assert matcher.start() > lastTagEnd;
                result.add(new Text(input.substring(lastTagEnd, matcher.start())));
            }

            Tag.Type type = close ? Tag.Type.CLOSE : selfClose ? Tag.Type.SINGLE : Tag.Type.OPEN;
            result.add(new Tag(tagName, type, matcher.end() - matcher.start()));
            lastTagEnd = matcher.end();
        }
        if (lastTagEnd != input.length()) {
            assert lastTagEnd < input.length();
            result.add(new Text(input.substring(lastTagEnd)));
        }
        return result;
    }

    @Contract(value = "_, _ -> new", pure = true)
    private static @NotNull Markers parse(@NotNull List<Token> tokens, @NotNull TagName[] tagNames) {
        // Empty containers for the result
        StringBuilder text = new StringBuilder();
        List<Marker> markers = new ArrayList<>();
        // Parse tokens
        Deque<Indexed<Tag>> tagStack = new ArrayDeque<>();
        int sourceOffset = 0;
        int textOffset = 0;
        for (Token token : tokens) {
            if (token instanceof Text textToken) {
                text.append(textToken);
                textOffset += textToken.originalSize();
            } else if (token instanceof Tag tag) {
                Tag.Type type = tag.effectiveType();
                switch (type) {
                    case SINGLE -> markers.add(new Marker(tag.name(), textOffset, textOffset));
                    case OPEN -> tagStack.add(new Indexed<>(textOffset, tag));
                    case CLOSE -> {
                        TagName name = tag.name();
                        Indexed<Tag> openingTag = tagStack.removeLast();
                        if (openingTag.item().name() == name) {
                            markers.add(new Marker(name, openingTag.index(), textOffset));
                        } else {
                            throw new IllegalStateException("Non-matching closing tag at offset " + sourceOffset + ": " + tag);
                        }
                    }
                }
            } else {
                throw new IllegalStateException("Unexpected token class: " + token.getClass());
            }
            sourceOffset += token.originalSize();
        }
        if (!tagStack.isEmpty()) {
            throw new IllegalStateException("The following tags have not been closed: " + tagStack);
        }
        return new Markers(text.toString(), Set.of(tagNames), markers.stream());
    }

    //endregion
    //region Result getter

    /**
     * Returns the text without and marker.
     *
     * @return the text without any of the markers.
     */
    public @NotNull String unmarkedText() {
        return myText;
    }

    /**
     * Returns a new instance with all other markers removed.
     * The new instance only preserves the markers of the given types.
     *
     * @param tagNames The marker types you are interested in.
     * @return Marked text containing only the given marker types.
     */
    @Contract(value = "_ -> new", pure = true)
    public @NotNull Markers markers(@NotNull TagName... tagNames) {
        Set<TagName> newTagNames = Set.of(tagNames);
        if (!myKnownTagNames.containsAll(newTagNames)) {
            throw new IllegalStateException("At least one unknown TagName: " + Arrays.toString(tagNames));
        }
        return new Markers(myText, newTagNames, myMarkers.stream().filter(marker -> newTagNames.contains(marker.myTagName)));
    }

    @Contract(value = "_, _ -> new", pure = true)
    public @NotNull Markers withMarkers(@NotNull TagName tagName, @NotNull Collection<TextRange> ranges) {
        return withMarkers(List.of(tagName), ranges.stream()
                .map(range -> new Marker(tagName, range.getStartOffset(), range.getEndOffset())));
    }

    @Contract(value = "_, _ -> new", pure = true)
    public @NotNull Markers withMarkers(@NotNull Stream<Marker> markers, @NotNull TagName... tagNames) {
        return withMarkers(Arrays.asList(tagNames), markers);
    }

    /**
     * Returns a new instance with the given markers added.
     * The new instance contains all the markers form this instance, and all the markers added.
     *
     * @param tagNames The types of the markers which may be added.
     * @param markers  The new markers.
     * @return Marked text containing only the given marker types.
     * @see #withMarkers(TagName, Collection)
     * @see #withMarkers(Stream, TagName...)
     */
    @Contract(value = "_, _ -> new", pure = true)
    public @NotNull Markers withMarkers(@NotNull Collection<TagName> tagNames, @NotNull Stream<Marker> markers) {
        Set<TagName> newTagNames = Stream.concat(myKnownTagNames.stream(), tagNames.stream())
                .collect(Collectors.toUnmodifiableSet());
        return new Markers(myText, newTagNames, Stream.concat(myMarkers.stream(), markers.peek(newMarker -> {
            if (!newTagNames.contains(newMarker.myTagName)) {
                throw new IllegalArgumentException("Unexpected marker type: " + newMarker);
            }
        })));
    }

    /**
     * Returns the only marker this object contains.
     * If there are more than one marker, this method throws a runtime exception.
     * If there is no marker, this method also throws a runtime exception.
     *
     * @return The only marker known by this instance.
     */
    public @NotNull Marker single() {
        return optional().orElseThrow(() -> new NoSuchElementException("No markers for " + myKnownTagNames));
    }

    /**
     * Returns the only marker this object contains.
     * If there is no marker, this method returns an empty optional.
     * If there are more than one marker, this method throws a runtime exception.
     *
     * @return The only marker known by this instance.
     */
    public @NotNull Optional<Marker> optional() {
        if (myMarkers.isEmpty()) {
            return Optional.empty();
        } else if (myMarkers.size() > 1) {
            throw new IllegalStateException("Multiple markers: " + myMarkers);
        } else {
            return Optional.of(myMarkers.get(0));
        }
    }

    public int singleOffset(@NotNull TagName... tag) {
        return markers(tag).single().offset();
    }

    public @NotNull TextRange singleRange(@NotNull TagName... tag) {
        return markers(tag).single().range();
    }

    public @NotNull List<TextRange> ranges() {
        return as(Marker::range);
    }

    public @NotNull List<TextRange> ranges(@NotNull TagName... tag) {
        return markers(tag).ranges();
    }

    public @NotNull List<Marker> list() {
        return as(Function.identity());
    }

    private <T> List<T> as(@NotNull Function<Marker, T> mapper) {
        return new AbstractList<T>() {
            @Override
            public T get(int index) {
                return mapper.apply(myMarkers.get(index));
            }

            @Override
            public int size() {
                return myMarkers.size();
            }
        };
    }

    @Override
    public @NotNull Iterator<Marker> iterator() {
        return myMarkers.iterator();
    }

    @Override
    public int size() {
        return myMarkers.size();
    }

    @Override
    public @NotNull String toString() {
        Deque<Indexed<Token>> tokens = new ArrayDeque<>();
        for (Marker marker : myMarkers) {
            if (marker.myStart == marker.myEnd) {
                tokens.add(new Indexed<>(marker.myStart, new Tag(marker.myTagName, Tag.Type.SINGLE)));
            } else {
                tokens.addLast(new Indexed<>(marker.myStart, new Tag(marker.myTagName, Tag.Type.OPEN)));
                tokens.addFirst(new Indexed<>(marker.myEnd, new Tag(marker.myTagName, Tag.Type.CLOSE)));
            }
        }
        StringBuilder result = new StringBuilder();
        IntRef textOffset = new IntRef();
        tokens.stream().sorted(Comparator.comparingInt(Indexed::index)).forEach(indexed -> {
            if (indexed.index() != textOffset.get()) {
                assert indexed.index() > textOffset.get();
                result.append(myText, textOffset.get(), indexed.index());
                textOffset.set(indexed.index());
            }
            result.append(indexed.item());
        });
        if (textOffset.get() != myText.length()) {
            assert textOffset.get() < myText.length();
            result.append(myText, textOffset.get(), myText.length());
        }
        return result.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Markers markers = (Markers) o;
        return Objects.equals(myText, markers.myText) && Objects.equals(myMarkers, markers.myMarkers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myText, myMarkers);
    }

    //endregion
    //region Inner classes

    public static final class TagName {

        private static final @NotNull Pattern PATTERN_NAME = Pattern.compile("\\w+");

        private final @NotNull String myName;
        private final boolean myIsRange;

        private TagName(@NotNull String name, boolean isRange) {
            if (!PATTERN_NAME.matcher(name).matches()) {
                throw new IllegalArgumentException("Invalid tag name: " + name);
            }
            this.myName = name;
            this.myIsRange = isRange;
        }

        @Override
        public String toString() {
            return myName;
        }
    }

    public static final class Marker {

        private final @NotNull TagName myTagName;
        private final int myStart;
        private final int myEnd;

        private Marker(@NotNull TagName tagName, int start, int end) {
            myTagName = tagName;
            myStart = start;
            myEnd = end;
        }

        /**
         * The {@link TagName} of this marker.
         *
         * @return tag name of this marker.
         */
        public @NotNull TagName tagName() {
            return myTagName;
        }

        /**
         * The offset where the marker starts in the unmarked text.
         *
         * @return Start offset of this marker.
         */
        public int start() {
            return myStart;
        }

        /**
         * The offset where the marker ends in the unmakred text.
         *
         * @return End offset of this marker.
         */
        public int end() {
            return myEnd;
        }

        /**
         * The {@linkplain #start() start offset} {@linkplain #end() end offset} as a {@link TextRange}.
         *
         * @return Location of this marker in unmarked text as {@link TextRange}.
         */
        public @NotNull TextRange range() {
            return TextRange.create(myStart, myEnd);
        }

        /**
         * Location of the marker in the unmarked text if the marker is empty.
         * This method throws a runtime exception if {@link #start()} and {@link #end()} are different.
         * This method is intended to be used for {@linkplain #tagNameVoid(String) void markers}.
         *
         * @return Location of this empty marker in unmarked text.
         */
        public int offset() {
            if (myStart != myEnd) throw new IllegalStateException("Cannot represent range as offset: " + this);
            return myStart;
        }

        @Override
        public String toString() {
            return "Marker{" + myTagName + ", start=" + myStart + ", end=" + myEnd + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Marker marker = (Marker) o;
            return myStart == marker.myStart && myEnd == marker.myEnd && Objects.equals(myTagName, marker.myTagName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(myTagName, myStart, myEnd);
        }
    }

    private record Indexed<T>(int index, @NotNull T item) {}

    private sealed interface Token {
        int originalSize();
    }

    private record Text(@NotNull String string) implements Token {
        @Override
        public int originalSize() {
            return string.length();
        }

        @Override
        public String toString() {
            return string;
        }
    }

    private record Tag(@NotNull TagName name, @NotNull Type type, int originalSize) implements Token {
        private enum Type {SINGLE, OPEN, CLOSE}

        private Tag(@NotNull TagName name, @NotNull Type type) {
            this(name, type, name.myName.length() + (type == Type.OPEN ? 2 : 3));
        }

        private @NotNull Type effectiveType() {
            return !name.myIsRange && type == Type.OPEN ? Type.SINGLE : type;
        }

        @Override
        public String toString() {
            return switch (type) {
                case OPEN -> "<" + name + ">";
                case SINGLE -> "<" + name + "/>";
                case CLOSE -> "</" + name + ">";
            };
        }
    }

    //endregion
}
