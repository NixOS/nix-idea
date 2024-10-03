package org.nixos.idea._testutil;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy;
import com.intellij.testFramework.fixtures.IdeaTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstancePreConstructCallback;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;
import org.junit.platform.commons.support.AnnotationSupport;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

final class IdeaPlatformExtension implements ParameterResolver, TestInstancePreConstructCallback, TestInstancePreDestroyCallback {

    private static final Namespace NAMESPACE = Namespace.create(IdeaPlatformExtension.class);
    private static final Named<?> KEY_FIXTURE = Named.of("KEY_FIXTURE", new Object());
    private static final Named<?> KEY_FIXTURE_CLOSABLE = Named.of("KEY_FIXTURE_CLOSABLE", new Object());

    private static final Map<Class<?>, Function<ExtensionContext, ?>> PARAMETER_FACTORIES = Map.ofEntries(
            createResolver(CodeInsightTestFixture.class, IdeaPlatformExtension::resolveCodeInsightFixture),
            createResolver(IdeaProjectTestFixture.class, IdeaPlatformExtension::resolveFixture),
            createResolver(IdeaTestFixture.class, IdeaPlatformExtension::resolveFixture),
            createResolver(Project.class, IdeaPlatformExtension::resolveProject),
            createResolver(Module.class, IdeaPlatformExtension::resolveModule),
            createResolver(Disposable.class, IdeaPlatformExtension::resolveDisposable)
    );

    @Override
    public void preConstructTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext context) throws Exception {
        FixtureClosableWrapper existing = context.getStore(NAMESPACE).get(KEY_FIXTURE, FixtureClosableWrapper.class);
        if (existing == null) {
            context.getStore(NAMESPACE).put(KEY_FIXTURE, new FixtureClosableWrapper(context));
            context.getStore(NAMESPACE).put(KEY_FIXTURE_CLOSABLE, Boolean.TRUE);
        } else {
            context.getStore(NAMESPACE).put(KEY_FIXTURE, existing);
        }
    }

    @Override
    public void preDestroyTestInstance(ExtensionContext context) throws Exception {
        // Unfortunately, the ExtensionContext given to `preConstructTestInstance` is not scoped to the individual test.
        // We therefore must clean up the context manually.
        // https://github.com/junit-team/junit5/issues/3445
        FixtureClosableWrapper wrapper;
        while (context != null) {
            wrapper = context.getStore(NAMESPACE).remove(KEY_FIXTURE, FixtureClosableWrapper.class);
            if (wrapper != null) {
                if (context.getStore(NAMESPACE).remove(KEY_FIXTURE_CLOSABLE, Boolean.class) == Boolean.TRUE) {
                    wrapper.close();
                }
            }
            context = context.getParent().orElse(null);
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return PARAMETER_FACTORIES.containsKey(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return PARAMETER_FACTORIES.get(parameterContext.getParameter().getType()).apply(extensionContext);
    }

    private static CodeInsightTestFixture resolveCodeInsightFixture(ExtensionContext context) {
        if (resolveFixture(context) instanceof CodeInsightTestFixture fixture) {
            return fixture;
        } else {
            throw new ParameterResolutionException("cannot resolve CodeInsightTestFixture. @WithIdeaPlatform.CodeInsight is not specified for this test.");
        }
    }

    private static IdeaProjectTestFixture resolveFixture(ExtensionContext context) {
        return context.getStore(NAMESPACE).get(KEY_FIXTURE, FixtureClosableWrapper.class).myFixture;
    }

    private static Project resolveProject(ExtensionContext context) {
        return resolveFixture(context).getProject();
    }

    private static Module resolveModule(ExtensionContext context) {
        return resolveFixture(context).getModule();
    }

    private static Disposable resolveDisposable(ExtensionContext context) {
        return resolveFixture(context).getTestRootDisposable();
    }

    private static <T> Map.Entry<Class<T>, Function<ExtensionContext, T>> createResolver(Class<T> type, Function<ExtensionContext, T> resolver) {
        return Map.entry(type, resolver);
    }

    private static final class FixtureClosableWrapper implements CloseableResource {
        private final IdeaProjectTestFixture myFixture;

        private FixtureClosableWrapper(ExtensionContext context) throws Exception {
            String testName = PlatformTestUtil.getTestName(context.getDisplayName(), false);
            IdeaTestFixtureFactory factory = IdeaTestFixtureFactory.getFixtureFactory();
            IdeaProjectTestFixture baseFixture = factory.createLightFixtureBuilder(testName).getFixture();
            myFixture = AnnotationSupport.findAnnotation(context.getTestMethod(), WithIdeaPlatform.CodeInsight.class)
                    .or(() -> AnnotationSupport.findAnnotation(context.getTestClass(), WithIdeaPlatform.CodeInsight.class))
                    .<IdeaProjectTestFixture>map(annotation -> {
                        IdeaTestExecutionPolicy policy = Objects.requireNonNull(IdeaTestExecutionPolicy.current());
                        CodeInsightTestFixture fixture = factory.createCodeInsightFixture(baseFixture, policy.createTempDirTestFixture());
                        fixture.setTestDataPath(StringUtil.trimEnd(FileUtil.toSystemIndependentName(policy.getHomePath()), "/") + '/' +
                                StringUtil.trimStart(FileUtil.toSystemIndependentName(annotation.basePath()), "/"));
                        return fixture;
                    })
                    .orElse(baseFixture);
            myFixture.setUp();
        }

        @Override
        public void close() throws Exception {
            // IdeaTestFixture.tearDown() throws an exception when executed outside the event dispatch thread.
            // Interestingly, IdeaTestFixture.setUp() can also be called outside the event dispatch thread.
            EdtTestUtil.runInEdtAndWait(myFixture::tearDown);
        }
    }
}
