package org.nixos.idea._testutil;

import com.intellij.ide.IdeEventQueue;
import com.intellij.testFramework.EdtTestUtil;
import org.junit.jupiter.api.extension.DynamicTestInvocationContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;
import org.opentest4j.TestAbortedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

final class EdtExtension implements InvocationInterceptor {

    @Override
    public <T> T interceptTestClassConstructor(Invocation<T> invocation, ReflectiveInvocationContext<Constructor<T>> invocationContext, ExtensionContext extensionContext) throws Throwable {
        return runAndGet(invocation);
    }

    @Override
    public void interceptBeforeAllMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        runAndWait(invocation);
    }

    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        runAndWait(invocation);
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        runAndWait(invocation);
    }

    @Override
    public <T> T interceptTestFactoryMethod(Invocation<T> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        return runAndGet(invocation);
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        runAndWait(invocation);
    }

    @Override
    public void interceptDynamicTest(Invocation<Void> invocation, DynamicTestInvocationContext invocationContext, ExtensionContext extensionContext) throws Throwable {
        runAndWait(invocation);
    }

    @Override
    public void interceptAfterEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        runAndWait(invocation);
    }

    @Override
    public void interceptAfterAllMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        runAndWait(invocation);
    }

    private static <T> T runAndGet(Invocation<T> invocation) throws Throwable {
        @SuppressWarnings("unchecked")
        T[] result = (T[]) new Object[1];
        runAndWait(() -> result[0] = invocation.proceed());
        return result[0];
    }

    private static void runAndWait(Invocation<?> invocation) throws Throwable {
        ThrowableCollector uncaughtExceptions = new ThrowableCollector(TestAbortedException.class::isInstance);
        try (var ignored = wrap(uncaughtExceptions)) {
            // Call uncaughtExceptions.execute(...) inside EdtTestUtil.runInEdtAndWait(...)
            // to avoid race conditions when multiple exceptions are reported.
            EdtTestUtil.runInEdtAndWait(() -> uncaughtExceptions.execute(invocation::proceed));
        }
    }

    private static AutoCloseable wrap(ThrowableCollector uncaughtExceptions) {
        Thread edt = EdtTestUtil.runInEdtAndGet(Thread::currentThread);
        Thread.UncaughtExceptionHandler previousExceptionHandler = edt.getUncaughtExceptionHandler();
        edt.setUncaughtExceptionHandler((thread, exception) -> uncaughtExceptions.execute(() -> {throw exception;}));

        return () -> {
            try {
                // Make sure all deferred tasks have finished
                boolean incomplete;
                do {
                    incomplete = EdtTestUtil.runInEdtAndGet(() -> IdeEventQueue.getInstance().peekEvent() != null);
                } while (incomplete);
            } finally {
                edt.setUncaughtExceptionHandler(previousExceptionHandler);
            }
            // Re-throw any exception which might have been thrown by deferred tasks
            uncaughtExceptions.assertEmpty();
        };
    }
}
