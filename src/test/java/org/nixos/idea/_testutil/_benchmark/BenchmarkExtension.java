package org.nixos.idea._testutil._benchmark;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;

@NullMarked
final class BenchmarkExtension implements TestTemplateInvocationContextProvider {

    private static final Namespace NAMESPACE = Namespace.create(BenchmarkExtension.class);
    private static final MediaType TEXT_HTML_UTF_8 = MediaType.create("text", "html", UTF_8);

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return isAnnotated(context.getTestMethod(), Benchmark.class);
    }

    @Override
    public Stream<? extends TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        Recorder recorder = new Recorder(context);
        context.getStore(NAMESPACE).put(Recorder.class, recorder);
        return Stream.generate(recorder::newRun).takeWhile(Objects::nonNull);
    }

    private static final class Recorder implements AutoCloseable {

        private final ExtensionContext myContext;
        private final Benchmark myAnnotation;
        private final ThreadMXBean myThreadMXBean;
        private final ParserInstrumentation myParserInstrumentation = new ParserInstrumentation();

        private final List<FinishedRun> myFinished = Collections.synchronizedList(new ArrayList<>());
        private int myCounter = 0;

        private Recorder(ExtensionContext context) {
            myContext = context;
            myAnnotation = findAnnotation(context.getRequiredTestMethod(), Benchmark.class).orElseThrow();
            myThreadMXBean = ManagementFactory.getThreadMXBean();
            myThreadMXBean.setThreadCpuTimeEnabled(true);
        }

        private @Nullable TestRun newRun() {
            int next = ++myCounter;
            if (next < 10) {
                return new TestRun(false);
            } else if (next == 10) {
                return new TestRun(true);
            } else {
                return null;
            }
        }

        @Override
        public void close() {
            long[] nanoTimes = myFinished.stream().mapToLong(FinishedRun::nanoTime).toArray();
            long[] cpuTimes = myFinished.stream().mapToLong(FinishedRun::cpuTime).toArray();

            long nanoTimeAvg = Arrays.stream(nanoTimes).sum() / nanoTimes.length;
            long nanoTimeMax = Arrays.stream(nanoTimes).max().orElse(-1);
            long nanoTimeMin = Arrays.stream(nanoTimes).min().orElse(-1);
            long nanoTimeMedian = median(nanoTimes);
            long cpuTimeAvg = Arrays.stream(cpuTimes).sum() / cpuTimes.length;
            long cpuTimeMax = Arrays.stream(cpuTimes).max().orElse(-1);
            long cpuTimeMin = Arrays.stream(cpuTimes).min().orElse(-1);
            long cpuTimeMinMedian = median(cpuTimes);

            long ms = nanoTimeMedian / 1_000_000;
            if (myAnnotation.ms() != -1 && ms > myAnnotation.ms()) {
                Assertions.fail("Took " + ms + " ms, but only " + myAnnotation.ms() + " ms are allowed");
            }

            new ReportEntry(myContext)
                    .put("nanoTimeMedian", String.valueOf(nanoTimeMedian))
                    .put("nanoTimeAvg", String.valueOf(nanoTimeAvg))
                    .put("nanoTimeMax", String.valueOf(nanoTimeMax))
                    .put("nanoTimeMin", String.valueOf(nanoTimeMin))
                    .put("cpuTimeMinMedian", String.valueOf(cpuTimeMinMedian))
                    .put("cpuTimeAvg", String.valueOf(cpuTimeAvg))
                    .put("cpuTimeMax", String.valueOf(cpuTimeMax))
                    .put("cpuTimeMin", String.valueOf(cpuTimeMin))
                    .publish();
        }

        private long median(long[] values) {
            if (values.length == 0) {
                return -1;
            } else {
                Arrays.sort(values);
                int size = values.length;
                return (values[(size - 1) / 2] + values[size / 2]) / 2;
            }
        }

        private record FinishedRun(long nanoTime, long cpuTime) {}

        private final class TestRun implements TestTemplateInvocationContext,
                BeforeTestExecutionCallback, AfterTestExecutionCallback,
                InvocationInterceptor {

            private final boolean myIsInstrumented;

            private long myStartCpuTime = -1;
            private long myStartNanoTime = -1;

            private TestRun(boolean instrumented) {
                myIsInstrumented = instrumented;
            }

            @Override
            public String getDisplayName(int invocationIndex) {
                if (myIsInstrumented) {
                    return "[" + invocationIndex + "] instrumented run";
                } else {
                    return "[" + invocationIndex + "] benchmark run";
                }
            }

            @Override
            public List<Extension> getAdditionalExtensions() {
                return List.of(this);
            }

            @Override
            public void beforeTestExecution(ExtensionContext context) {
                myStartCpuTime = myThreadMXBean.isCurrentThreadCpuTimeSupported() ? -1 : myThreadMXBean.getCurrentThreadCpuTime();
                myStartNanoTime = System.nanoTime();
            }

            @Override
            public void afterTestExecution(ExtensionContext context) {
                long nanoTime = System.nanoTime() - myStartNanoTime;
                long cpuTime = myStartCpuTime == -1 ? -1 : myThreadMXBean.getCurrentThreadCpuTime() - myStartCpuTime;
                new ReportEntry(context)
                        .put("nanoTime", String.valueOf(nanoTime))
                        .put("cpuTime", String.valueOf(cpuTime))
                        .publish();
                if (myIsInstrumented) {
                    myContext.publishFile("parser.html", TEXT_HTML_UTF_8, path ->
                            Files.writeString(path, myParserInstrumentation.htmlReport(), UTF_8));
                } else {
                    myFinished.add(new FinishedRun(nanoTime, cpuTime));
                }
            }

            @Override
            public void interceptTestTemplateMethod(
                    Invocation<@Nullable Void> invocation,
                    ReflectiveInvocationContext<Method> invocationContext,
                    ExtensionContext extensionContext
            ) throws Throwable {
                if (myIsInstrumented) {
                    try (var ignore = myParserInstrumentation.attach()) {
                        invocation.proceed();
                    }
                } else {
                    invocation.proceed();
                }
            }
        }
    }

    private static final class ReportEntry {
        private final ExtensionContext myContext;
        private final Map<String, String> myMap = new LinkedHashMap<>();

        private ReportEntry(ExtensionContext myContext) {
            this.myContext = myContext;
        }

        @CheckReturnValue
        @Contract(value = "_, _ -> this", mutates = "this")
        @SuppressWarnings("UnstableApiUsage")
        private ReportEntry put(String key, String value) {
            myMap.put(key, value);
            return this;
        }

        private void publish() {
            myContext.publishReportEntry(myMap);
        }
    }
}
