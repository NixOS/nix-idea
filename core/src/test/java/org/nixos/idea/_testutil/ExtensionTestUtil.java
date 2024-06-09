package org.nixos.idea._testutil;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Execution;
import org.junit.platform.testkit.engine.TerminationInfo;

import java.lang.annotation.*;
import java.util.List;

final class ExtensionTestUtil {
    private static final String TEST_KIT_MARKER = "nix-idea.run-mocks";

    private ExtensionTestUtil() {} // Cannot be instantiated

    static void runTest(DiscoverySelector selector) throws Throwable {
        List<Execution> executions = runTests(selector).testEvents().executions().list();
        if (executions.size() != 1) {
            throw new IllegalStateException("Expected exactly one test execution, got: " + executions);
        }
        TerminationInfo terminationInfo = executions.get(0).getTerminationInfo();
        TestExecutionResult executionResult = terminationInfo.getExecutionResult();
        if (executionResult.getStatus() != TestExecutionResult.Status.SUCCESSFUL) {
            throw executionResult.getThrowable().orElseThrow();
        }
    }

    static EngineExecutionResults runTests(DiscoverySelector... selectors) {
        return EngineTestKit.engine("junit-jupiter")
                .configurationParameter(TEST_KIT_MARKER, "true")
                .selectors(selectors)
                .execute();
    }

    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @Test
    @Tag("mock")
    @ExtendWith(DisableOutsideTestKit.class)
    @interface MockTest {}

    private static final class DisableOutsideTestKit implements ExecutionCondition {
        @Override
        public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
            return context.getConfigurationParameter(TEST_KIT_MARKER).isPresent()
                    ? ConditionEvaluationResult.enabled("Test executed by ExtensionTestUtil")
                    : ConditionEvaluationResult.disabled("@MockTest triggered outside ExtensionTestUtil");
        }
    }
}
