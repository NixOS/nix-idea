package org.nixos.idea._testutil._benchmark;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.impl.PsiBuilderAdapter;
import com.intellij.lang.impl.PsiBuilderImpl;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.lang.parser.GeneratedParserUtilBase.Frame;
import com.intellij.openapi.util.text.HtmlBuilder;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.matcher.ElementMatchers;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.UnmodifiableClassException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

final class ParserInstrumentation {

    private static final ThreadLocal<ParserInstrumentation> CURRENT = new ThreadLocal<>();

    private final Map<PsiBuilderImpl, ParserRun> myRuns = new IdentityHashMap<>();

    ParserInstrumentation() {
        MyInstrumentation.install();
    }

    AutoCloseable attach() {
        Thread thread = Thread.currentThread();
        ParserInstrumentation parent = CURRENT.get();
        CURRENT.set(this);
        return () -> {
            assert Thread.currentThread() == thread;
            if (parent != null) {
                CURRENT.set(parent);
            } else {
                CURRENT.remove();
            }
        };
    }

    String htmlReport() {
        HtmlBuilder out = new HtmlBuilder().appendRaw("\n");
        myRuns.values().forEach(run -> out.append(run.htmlChunk()).appendRaw("\n"));
        out.append(styleResource()).appendRaw("\n");
        out.append(scriptResource()).appendRaw("\n");
        return out.wrapWithHtmlBody().toString();
    }

    private HtmlChunk styleResource() {
        String resourceName = "parser-runs.css";
        return HtmlChunk.styleTag(readResource(resourceName));
    }

    private HtmlChunk scriptResource() {
        String resourceName = "parser-runs.js";
        return HtmlChunk.tag("script").attr("type", "module").addRaw(readResource(resourceName));
    }

    private String readResource(String resourceName) {
        try (InputStream stream = ParserInstrumentation.class.getResourceAsStream(resourceName)) {
            Objects.requireNonNull(stream, "resource " + resourceName + " not found");
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unused")
    private static final class ParserRun {
        private final String myText;

        @JsonProperty("totalResets")
        private int myTotalResets;
        @JsonProperty("totalWeighedResets")
        private int myTotalWeighedResets;
        @JsonIgnore // is injected into TokenStats
        private final int[] myPasses;
        @JsonProperty("tokens")
        private final TokenStats[] myTokens;

        @JsonProperty("frames")
        private final Map<FrameStats.FrameKey, FrameStats> myFrames = new HashMap<>();
        @JsonProperty("rootFrame")
        private final FrameStats.FrameKey myRootFrameKey = FrameStats.FrameKey.of(null);

        private FrameStats myCurrentFrame = myFrames.computeIfAbsent(myRootFrameKey, FrameStats::new);
        private int myCurrentOngoingRollback = 0;

        public ParserRun(PsiBuilderImpl builder) {
            myText = builder.getOriginalText().toString();

            int tokenCount = builder.getLexemeCount();
            myPasses = new int[tokenCount];
            myTokens = new TokenStats[tokenCount + 1];
            for (int i = 0; i < tokenCount; i++) {
                myTokens[i] = new TokenStats(builder, i);
            }
            myTokens[tokenCount] = new TokenStats(builder, tokenCount);
        }

        private static @Nullable ParserRun currentRun(PsiBuilderImpl builder) {
            ParserInstrumentation generator = CURRENT.get();
            return generator == null ? null : generator.myRuns.computeIfAbsent(builder, ParserRun::new);
        }

        private void enterFrame(PsiBuilderImpl builder, Frame frame) {
            if (frame.elementType == null && frame.name == null) {
                return; // Filter out unnamed (i.e. internal) frames
            }
            if (myCurrentOngoingRollback != 0) {
                myCurrentFrame.recordResetEnd(myCurrentOngoingRollback);
                myCurrentOngoingRollback = 0;
            }
            ParserRun.FrameStats stats = myFrames.computeIfAbsent(ParserRun.FrameStats.FrameKey.of(frame), ParserRun.FrameStats::new);
            stats.enter(this, builder, frame);
        }

        private void exitFrame(PsiBuilderImpl builder, Frame frame, Frame newFrame) {
            if (frame.elementType == null && frame.name == null) {
                return; // Filter out unnamed (i.e. internal) frames
            }
            while (newFrame != null && newFrame.elementType == null && newFrame.name == null) {
                newFrame = newFrame.parentFrame; // Filter out unnamed (i.e. internal) frames
            }
            myCurrentFrame.exit(this, builder, frame, newFrame);
        }

        private void recordAdvance(int oldIndex, int newIndex) {
            assert newIndex > oldIndex :
                    "next index (" + newIndex + ") <= previous index (" + oldIndex + ")";

            myCurrentFrame.recordAdvance(newIndex);
            if (myCurrentOngoingRollback != 0) {
                myCurrentFrame.recordResetEnd(myCurrentOngoingRollback);
                myCurrentOngoingRollback = 0;
            }
            for (int i = oldIndex; i < newIndex; i++) {
                myPasses[i] += 1;
            }
        }

        private void recordReset(int oldIndex, int newIndex) {
            int diff = oldIndex - newIndex;
            assert diff >= 0 : "next index (" + newIndex + ") > previous index (" + oldIndex + ")";

            myTotalResets += 1;
            myTotalWeighedResets += diff;
            myTokens[oldIndex].recordReset(diff);
            myCurrentFrame.recordReset(diff);
            myCurrentOngoingRollback += diff;
        }

        private HtmlChunk htmlChunk() {
            String json;
            try {
                json = new ObjectMapper()
                        .setDefaultVisibility(JsonAutoDetect.Value.construct(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE))
                        .addMixIn(IElementType.class, IElementTypeJsonMixin.class)
                        .writeValueAsString(this);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(e);
            }

            List<HtmlChunk> chunks = new ArrayList<>();
            for (int i = 0; i < myPasses.length; i++) {
                TokenStats token = myTokens[i];
                TokenStats nextToken = myTokens[i + 1];
                String text = myText.substring(token.myOffset, nextToken.myOffset).replaceAll("(\\r?)\\n", "⏎$1\n");
                chunks.add(HtmlChunk.span().attr("data-token", i)
                        .addRaw(StringUtil.escapeXmlEntities(text)));
            }

            return HtmlChunk.div().setClass("parser-run").children(
                    HtmlChunk.raw("\n"),
                    HtmlChunk.tag("pre").setClass("code-preview")
                            .addRaw("\n").children(chunks).addRaw("\n"),
                    HtmlChunk.raw("\n"),
                    HtmlChunk.tag("script").attr("type", "application/json").setClass("data").addRaw(json),
                    HtmlChunk.raw("\n")
            );
        }

        private final class TokenStats {
            @JsonProperty("index")
            private final int myIndex;
            @JsonProperty("offset")
            private final int myOffset;
            @JsonProperty("type")
            private final @Nullable IElementType myType;

            @JsonProperty("totalResets")
            private int myTotalResets;
            @JsonProperty("totalWeighedResets")
            private int myTotalWeighedResets;

            private TokenStats(PsiBuilderImpl builder, int tokenIndex) {
                assert builder.rawTokenIndex() == 0;
                myIndex = tokenIndex;
                myOffset = builder.rawTokenTypeStart(tokenIndex);
                myType = builder.rawLookup(tokenIndex);
            }

            private void recordReset(int weight) {
                myTotalResets += 1;
                myTotalWeighedResets += weight;
            }

            @JsonProperty("totalPasses")
            private int getTotalPasses() {
                return myIndex < myPasses.length ? myPasses[myIndex] : 0;
            }
        }

        private static final class FrameStats implements Comparable<FrameStats> {
            @JsonProperty("key")
            private final FrameKey myKey;
            @JsonProperty("startToken")
            private final int myStartTokenIndex;

            @JsonProperty("parents")
            private final Set<FrameKey> myParents = new HashSet<>();
            @JsonProperty("children")
            private final Set<FrameKey> myChildren = new HashSet<>();

            private FrameStats myCurrentParent;
            private int myCurrentLevel;
            private int myCurrentResets;
            private int myCurrentWastedAdvances;
            private int myCurrentResetsFinished;
            private int myCurrentLastSeenToken;

            @JsonProperty("occurrences")
            private int myOccurrences;
            @JsonProperty("totalResets")
            private int myTotalResets;
            @JsonProperty("totalWastedAdvances")
            private int myTotalWastedAdvances;
            @JsonProperty("totalResetsFinished")
            private int myTotalResetsFinished;
            @JsonProperty("maxResets")
            private int myMaxResets;
            @JsonProperty("maxWastedAdvances")
            private int myMaxWastedAdvances;
            @JsonProperty("maxResetsFinished")
            private int myMaxResetsFinished;
            @JsonProperty("lastSeenToken")
            private int myLastSeenToken;
            @JsonProperty("lastSeenLocalToken")
            private int myLastSeenLocalToken;
            @JsonProperty("endTokenIndex")
            private int myEndTokenIndex;

            private FrameStats(FrameKey frame) {
                this.myKey = frame;
                this.myStartTokenIndex = frame.startTokenIndex;
            }

            private void enter(ParserRun run, PsiBuilderImpl builder, Frame frame) {
                assert frame.level > run.myCurrentFrame.myCurrentLevel :
                        "new level (" + frame.level + ") <= current level (" + run.myCurrentFrame.myCurrentLevel + ")";
                assert builder.rawTokenIndex() == myKey.startTokenIndex :
                        "token index (" + builder.rawTokenIndex() + ") != frame start (" + myKey.startTokenIndex + ")";
                assert frame.elementType != null || frame.name != null :
                        "unnamed frame";

                myCurrentParent = run.myCurrentFrame;
                myCurrentLevel = frame.level;
                myCurrentResets = 0;
                myCurrentWastedAdvances = 0;
                myCurrentResetsFinished = 0;
                myCurrentLastSeenToken = builder.rawTokenIndex();

                myOccurrences += 1;
                myLastSeenLocalToken = Math.max(myLastSeenLocalToken, builder.rawTokenIndex());
                myParents.add(myCurrentParent.myKey);
                myCurrentParent.myChildren.add(myKey);

                run.myCurrentFrame = this;
            }

            private void exit(ParserRun run, PsiBuilderImpl builder, Frame frame, Frame newFrame) {
                assert frame.level == myCurrentLevel :
                        "closed frame level (" + frame.level + ") != recorded frame level (" + myCurrentLevel + ")";
                assert frame.elementType != null || frame.name != null :
                        "unnamed frame";
                assert this == run.myFrames.get(FrameStats.FrameKey.of(frame)) :
                        "non-matching matching";

                int endTokenIndex = builder.rawTokenIndex();
                assert endTokenIndex <= myCurrentLastSeenToken :
                        "end token (" + endTokenIndex + ") > last seen token (" + myCurrentLastSeenToken + ")";

                myTotalResets += myCurrentResets;
                myTotalWastedAdvances += myCurrentWastedAdvances;
                myTotalResetsFinished += myCurrentResetsFinished;
                myMaxResets = Math.max(myMaxResets, myCurrentResets);
                myMaxWastedAdvances = Math.max(myMaxWastedAdvances, myCurrentWastedAdvances);
                myMaxResetsFinished = Math.max(myMaxResetsFinished, myCurrentResetsFinished);
                myLastSeenToken = Math.max(myLastSeenToken, myCurrentLastSeenToken);
                myEndTokenIndex = Math.max(myEndTokenIndex, endTokenIndex);

                FrameStats parent = myCurrentParent;
                assert parent == run.myFrames.get(FrameStats.FrameKey.of(newFrame));
                run.myCurrentFrame = parent;

                parent.myCurrentLastSeenToken = Math.max(parent.myCurrentLastSeenToken, myCurrentLastSeenToken);
                parent.myCurrentWastedAdvances += myCurrentLastSeenToken - endTokenIndex;
                parent.myLastSeenLocalToken = Math.max(parent.myLastSeenLocalToken, endTokenIndex);

                myCurrentParent = null;
                myCurrentLevel = -1;
                myCurrentResets = -1;
                myCurrentWastedAdvances = -1;
                myCurrentResetsFinished = -1;
                myCurrentLastSeenToken = -1;
            }

            private void recordAdvance(int newIndex) {
                assert myCurrentLevel != -1 : "non-initialized frame";
                myCurrentLastSeenToken = Math.max(myCurrentLastSeenToken, newIndex);
                myLastSeenLocalToken = Math.max(myLastSeenLocalToken, newIndex);
            }

            private void recordReset(int diff) {
                assert myCurrentLevel != -1 : "non-initialized frame";
                myCurrentResets += 1;
                myCurrentWastedAdvances += diff;
            }

            public void recordResetEnd(int length) {
                myCurrentResetsFinished += length;
            }

            @Override
            public int compareTo(FrameStats o) {
                int diff;
                diff = o.myMaxWastedAdvances - this.myMaxWastedAdvances;
                if (diff != 0) return diff;
                diff = o.myTotalWastedAdvances - this.myTotalWastedAdvances;
                if (diff != 0) return diff;
                return this.myKey.startTokenIndex - o.myKey.startTokenIndex;
            }

            @JsonSerialize(using = ToStringSerializer.class)
            private record FrameKey(
                    @Nullable IElementType type,
                    @Nullable String name,
                    int startTokenIndex
            ) {
                private FrameKey {
                    assert type != null || name != null : "unnamed frame";
                }

                private static FrameKey of(@Nullable Frame frame) {
                    if (frame == null) {
                        return new FrameKey(null, "root", 0);
                    } else {
                        return new FrameKey(frame.elementType, frame.name, frame.position);
                    }
                }

                @Override
                public @NonNull String toString() {
                    return type + "/" + name + "@" + startTokenIndex;
                }
            }
        }

        @JsonSerialize(using = ToStringSerializer.class)
        private static abstract class IElementTypeJsonMixin {}
    }

    @SuppressWarnings("resource")
    private static final class MyInstrumentation {

        private static void install() {
            // Calling this method will load the class, executing the `static` block below.
            // This is a simple pattern to avoid implementing "Double-Checked Locking".
        }

        static {
            try {
                ByteBuddy byteBuddy = new ByteBuddy();
                ByteBuddyAgent.install().redefineClasses(
                        new ClassDefinition(GeneratedParserUtilBase.class, byteBuddy
                                .decorate(GeneratedParserUtilBase.class)
                                .visit(new AsmVisitorWrapper.ForDeclaredMethods()
                                        .method(ElementMatchers.named("enter_section_impl_"), Advice.to(EnterFrame.class))
                                        .method(ElementMatchers.named("exit_section_impl_"), Advice.to(ExitFrame.class))
                                ).make().getBytes()
                        ),
                        new ClassDefinition(PsiBuilderImpl.class, byteBuddy
                                .decorate(PsiBuilderImpl.class)
                                .visit(new AsmVisitorWrapper.ForDeclaredMethods()
                                        .method(ElementMatchers.named("rollbackTo"), Advice.to(RecordRollback.class))
                                        .method(ElementMatchers.named("advanceLexer"), Advice.to(RecordAdvance.class))
                                        .method(ElementMatchers.named("rawAdvanceLexer"), Advice.to(RecordAdvance.class))
                                        .method(ElementMatchers.named("skipWhitespace"), Advice.to(RecordAdvance.class))
                                ).make().getBytes()
                        )
                );
            } catch (ClassNotFoundException | UnmodifiableClassException e) {
                throw new IllegalStateException(e);
            }
        }

        @SuppressWarnings("unused")
        public static final class EnterFrame {
            @Advice.OnMethodExit(inline = false)
            public static void exit(
                    @Advice.Argument(0) PsiBuilder builder,
                    @Advice.Argument(3) IElementType elementType,
                    @Advice.Argument(4) String frameName
            ) {
                PsiBuilder delegate = builder;
                while (delegate instanceof PsiBuilderAdapter adapter) {
                    delegate = adapter.getDelegate();
                }
                if (delegate instanceof PsiBuilderImpl impl) {
                    ParserRun run = ParserRun.currentRun(impl);
                    if (run != null) {
                        Frame frame = GeneratedParserUtilBase.ErrorState.get(builder).currentFrame;
                        assert frame.elementType == elementType :
                                "frame type (" + frame.elementType + ") != type argument (" + elementType + ")";
                        assert Objects.equals(frame.name, frameName) :
                                "frame name (" + frame.name + ") != name argument (" + frameName + ")";
                        run.enterFrame(impl, frame);
                    }
                }
            }
        }

        @SuppressWarnings("unused")
        public static final class ExitFrame {
            @Advice.OnMethodExit(inline = false)
            public static void exit(
                    @Advice.Argument(1) Frame frame,
                    @Advice.Argument(2) PsiBuilder builder,
                    @Advice.Argument(4) boolean result,
                    @Advice.Argument(5) boolean pinned
            ) {
                PsiBuilder delegate = builder;
                while (delegate instanceof PsiBuilderAdapter adapter) {
                    delegate = adapter.getDelegate();
                }
                if (delegate instanceof PsiBuilderImpl impl) {
                    ParserRun run = ParserRun.currentRun(impl);
                    if (run != null) {
                        Frame newFrame = GeneratedParserUtilBase.ErrorState.get(builder).currentFrame;
                        run.exitFrame(impl, frame, newFrame);
                    }
                }
            }
        }

        @SuppressWarnings("unused")
        public static final class RecordAdvance {
            @Advice.OnMethodEnter()
            public static int enter(
                    @Advice.This PsiBuilderImpl builder
            ) {
                return builder.rawTokenIndex();
            }

            @Advice.OnMethodExit(inline = false)
            public static void exit(
                    @Advice.This PsiBuilderImpl builder,
                    @Advice.Enter int oldIndex
            ) {
                ParserRun run = ParserRun.currentRun(builder);
                if (run != null) {
                    int newIndex = builder.rawTokenIndex();
                    if (newIndex == oldIndex) {
                        return;
                    }
                    run.recordAdvance(oldIndex, newIndex);
                }
            }
        }

        @SuppressWarnings("unused")
        public static final class RecordRollback {
            @Advice.OnMethodEnter(inline = false)
            public static void enter(
                    @Advice.This PsiBuilderImpl builder,
                    @Advice.Argument(0) PsiBuilderImpl.ProductionMarker marker
            ) {
                ParserRun run = ParserRun.currentRun(builder);
                if (run != null) {
                    int oldIndex = builder.rawTokenIndex();
                    int newIndex = marker.getStartIndex();
                    run.recordReset(oldIndex, newIndex);
                }
            }
        }
    }
}
