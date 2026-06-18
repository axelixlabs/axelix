package com.axelixlabs.gradle.plugin;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.DependencySet;

public class CompileClasspathDependencyApplier implements Action<DependencySet> {
    private final Project project;

    public CompileClasspathDependencyApplier(Project project) {
        this.project = project;
    }

    @Override
    public void execute(DependencySet dependencies) {
        TestDependencyContributor.contributeCompileClasspathDependencies(project, dependencies);
    }
}
