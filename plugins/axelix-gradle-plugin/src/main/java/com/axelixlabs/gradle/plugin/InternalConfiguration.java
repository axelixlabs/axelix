package com.axelixlabs.gradle.plugin;

import org.gradle.api.Project;

public class InternalConfiguration {
    private final TestClasspathDependencyApplier testClasspathDependencyApplier;
    private final CompileClasspathDependencyApplier compileClasspathDependencyApplier;

    public InternalConfiguration(final Project project) {
        this.testClasspathDependencyApplier = new TestClasspathDependencyApplier(project);
        this.compileClasspathDependencyApplier = new CompileClasspathDependencyApplier(project);
    }

    public TestClasspathDependencyApplier getTestClasspathDependencyApplier() {
        return testClasspathDependencyApplier;
    }

    public CompileClasspathDependencyApplier getCompileClasspathDependencyApplier() {
        return compileClasspathDependencyApplier;
    }
}
