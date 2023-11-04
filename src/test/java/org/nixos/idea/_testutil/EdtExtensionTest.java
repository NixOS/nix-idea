package org.nixos.idea._testutil;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.ui.EDT;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EventConditions;
import org.junit.platform.testkit.engine.TestExecutionResultConditions;

import javax.swing.SwingUtilities;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

final class EdtExtensionTest {

    @Test
    void test_is_executed_in_event_dispatch_thread() throws Throwable {
        runTestLambda(() -> {
            assertTrue(SwingUtilities.isEventDispatchThread());
            assertTrue(EDT.isCurrentThreadEdt());
        });
    }

    @Test
    void fail_if_test_throws_exception() {
        assertThrows(MyException.class, () -> runTestLambda(() -> {
            throw new MyException();
        }));
    }

    @Test
    void fail_if_deferred_task_from_swing_throws_exception() {
        assertThrows(MyException.class, () -> runTestLambda(() -> {
            SwingUtilities.invokeLater(() -> {
                throw new MyException();
            });
        }));
    }

    @Test
    void fail_if_deferred_task_from_application_throws_exception() {
        assertThrows(MyException.class, () -> runTestLambda(() -> {
            ApplicationManager.getApplication().invokeLater(() -> {
                throw new MyException();
            });
        }));
    }

    @Test
    void fail_if_hyper_deferred_task_throws_exception() {
        assertThrows(MyException.class, () -> runTestLambda(() -> {
            SwingUtilities.invokeLater(() -> {
                ApplicationManager.getApplication().invokeLater(() -> {
                    SwingUtilities.invokeLater(() -> {
                        ApplicationManager.getApplication().invokeLater(() -> {
                            throw new MyException();
                        });
                    });
                });
            });
        }));
    }

    @Test
    void later_exceptions_are_added_as_suppressed_exceptions() {
        EngineExecutionResults results = ExtensionTestUtil.runTests(testLambda(() -> {
            SwingUtilities.invokeLater(() -> {
                SwingUtilities.invokeLater(() -> {
                    throw new MyException("deferred 2");
                });
                throw new MyException("deferred 1");
            });
            throw new MyException("first");
        }));
        results.testEvents().assertThatEvents().haveExactly(1, EventConditions.finishedWithFailure(
                TestExecutionResultConditions.instanceOf(MyException.class),
                TestExecutionResultConditions.message("first"),
                TestExecutionResultConditions.suppressed(0,
                        TestExecutionResultConditions.instanceOf(MyException.class),
                        TestExecutionResultConditions.message("deferred 1")
                ),
                TestExecutionResultConditions.suppressed(1,
                        TestExecutionResultConditions.instanceOf(MyException.class),
                        TestExecutionResultConditions.message("deferred 2")
                )
        ));
    }

    private static void runTestLambda(EdtMockTest lambda) throws Throwable {
        ExtensionTestUtil.runTest(testLambda(lambda));
    }

    private static DiscoverySelector testLambda(EdtMockTest lambda) {
        return selectClass(lambda.getClass());
    }

    @FunctionalInterface
    private interface EdtMockTest {

        void testInternal() throws Throwable;

        @ExtensionTestUtil.MockTest
        @WithIdeaPlatform.OnEdt
        @SuppressWarnings("unused")
        default void test() throws Throwable {
            testInternal();
        }
    }

    private static final class MyException extends RuntimeException {

        public MyException() {}

        private MyException(String message) {
            super(message);
        }
    }
}
