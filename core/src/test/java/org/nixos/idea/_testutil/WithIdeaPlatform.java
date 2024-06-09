package org.nixos.idea._testutil;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.lang.annotation.*;

/**
 * {@code @WithIdeaPlatform} can be used to set up an IDEA platform test environment.
 * This extension is an alternative for {@link BasePlatformTestCase}, but for JUnit 5.
 * The extension is rather simple and does not support all the features of {@code BasePlatformTestCase}.
 *
 * <h2>Supported Parameters</h2>
 * <ul>
 *     <li>{@link IdeaProjectTestFixture}
 *     <li>{@link Project} <small>– shortcut for {@link IdeaProjectTestFixture#getProject()}</small>
 *     <li>{@link Module} <small>– shortcut for {@link IdeaProjectTestFixture#getModule()}</small>
 *     <li>{@link Disposable} <small>– shortcut for {@link IdeaProjectTestFixture#getTestRootDisposable()}</small>
 * </ul>
 *
 * <h2><a id="edt"></a>Event Dispatch Thread</h2>
 * As documented at <a href="https://plugins.jetbrains.com/docs/intellij/general-threading-rules.html">
 * General Threading Rules</a> by JetBrains, many parts of the IDEA platform cannot be accessed from arbitrary threads.
 * <em>JUnit</em>'s worker thread is not allowed to access these parts of the platform without further effort.
 * While the worker thread could gain read access to the platform by starting a read action,
 * write access can only be obtained from the event dispatch thread.
 * For convenience, you may use {@link OnEdt @WithIdeaPlatform.OnEdt} and
 * JUnit will call all your test methods on the event dispatch thread.
 * Otherwise, you may use {@link Application#runReadAction(Runnable)} or {@link ReadAction} for starting a read action,
 * {@link EdtTestUtil} for running some code on the event dispatch thread, and
 * {@link Application#runWriteAction(Runnable)} or {@link WriteAction} for starting a write action.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE})
@Inherited
@ExtendWith(IdeaPlatformExtension.class)
@ResourceLock("com.intellij.IdeaPlatform")
public @interface WithIdeaPlatform {
    /**
     * Executes the test method and lifecycle methods on the
     * <a href="WithIdeaPlatform.html#edt">event dispatch thread</a>.
     * This annotation inherits {@link WithIdeaPlatform}, so you don't have to add it separately.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE})
    @Inherited
    @ExtendWith(EdtExtension.class)
    @WithIdeaPlatform
    @interface OnEdt {}
}
