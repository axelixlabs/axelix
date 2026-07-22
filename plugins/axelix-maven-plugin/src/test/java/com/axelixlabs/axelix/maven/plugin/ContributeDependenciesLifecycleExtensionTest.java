/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link ContributeDependenciesLifecycleExtension}
 *
 * @author Artemiy Degtyarev
 * @author Mikhail Polivakha
 */
class ContributeDependenciesLifecycleExtensionTest {

    public static final String CURRENT_DIR = new File("").getAbsolutePath();

    @Test
    void shouldAddProfilerDependencyIfNotPresent() throws VerificationException, IOException {
        // given.
        String baseDir = CURRENT_DIR + "/src/integrationTest/without-deps";
        Verifier verifier = new Verifier(baseDir);

        // when.
        verifier.executeGoal("dependency:list");
        verifier.verify(true);

        // then. thymeleaf is not added explicitly, only pulled in transitively by the profiler.
        String logOutput = readLogFile(baseDir);
        assertThat(logOutput).contains("digital.pragmatech.testing:spring-test-profiler:jar:0.1.2:test");
        assertThat(logOutput).contains("org.thymeleaf:thymeleaf:jar:3.1.3.RELEASE:test");
    }

    @Test
    void shouldNotAddProfilerDependencyIfPresent() throws VerificationException, IOException {
        // given.
        String baseDir = CURRENT_DIR + "/src/integrationTest/contains-deps";
        Verifier verifier = new Verifier(baseDir);

        // when.
        verifier.executeGoal("dependency:list");
        verifier.verify(true);

        // then.
        String logOutput = readLogFile(baseDir);
        assertThat(logOutput).contains("digital.pragmatech.testing:spring-test-profiler:jar:0.1.1:test");
        assertThat(logOutput).doesNotContain("org.thymeleaf:thymeleaf:3.1.5.RELEASE:test");
    }

    @Test
    void shouldAddProfilerAndNotAlterAnExistingThymeleafVersion() throws VerificationException, IOException {
        // given.
        String baseDir = CURRENT_DIR + "/src/integrationTest/lower-thymeleaf";
        Verifier verifier = new Verifier(baseDir);

        // when.
        verifier.executeGoal("dependency:list");
        verifier.verify(true);

        // then. thymeleaf compatibility is not policed: the pre-existing version is kept as-is
        // and the profiler is added regardless.
        String logOutput = readLogFile(baseDir);
        assertThat(logOutput).contains("org.thymeleaf:thymeleaf:jar:3.0.15.RELEASE");
        assertThat(logOutput).contains("digital.pragmatech.testing:spring-test-profiler:jar:0.1.2:test");
    }

    @Test
    void shouldRunAllGoalsAutomaticallyWithoutAnyExecutionDeclared() throws VerificationException, IOException {
        // given. the pom declares only <extensions>true</extensions>, no <executions> at all.
        String baseDir = CURRENT_DIR + "/src/integrationTest/auto-bind-goals";
        cleanTargetDir(baseDir);

        // when.
        Verifier dependencyVerifier = new Verifier(baseDir);
        dependencyVerifier.executeGoal("dependency:list");
        dependencyVerifier.verify(true);

        // then. the profiler dependency is contributed regardless of which goal is invoked.
        assertThat(readLogFile(baseDir)).contains("digital.pragmatech.testing:spring-test-profiler:jar:0.1.2:test");

        // when.
        Verifier installVerifier = new Verifier(baseDir);
        installVerifier.executeGoal("install");
        installVerifier.verify(true);

        // then. spring.factories generation and project-info generation both ran, automatically.
        assertThat(Paths.get(baseDir, "target/test-classes/META-INF/spring.factories"))
                .exists();
        assertThat(Paths.get(baseDir, "target/classes/META-INF/axelix-info.properties"))
                .exists();
    }

    @Test
    void shouldDisableProfilerEntirelyViaPropertyButKeepProjectInfoUnconditional()
            throws VerificationException, IOException {
        // given. axelix.copyProfilerReport=false, mirroring the gradle extension's flag.
        String baseDir = CURRENT_DIR + "/src/integrationTest/profiler-disabled";
        cleanTargetDir(baseDir);

        // when.
        Verifier dependencyVerifier = new Verifier(baseDir);
        dependencyVerifier.executeGoal("dependency:list");
        dependencyVerifier.verify(true);

        // then. no dependency contributed.
        assertThat(readLogFile(baseDir)).doesNotContain("digital.pragmatech.testing:spring-test-profiler");

        // when.
        Verifier installVerifier = new Verifier(baseDir);
        installVerifier.executeGoal("install");
        installVerifier.verify(true);

        // then. spring.factories/report-copy are skipped, but project-info is unconditional.
        assertThat(Paths.get(baseDir, "target/test-classes/META-INF/spring.factories"))
                .doesNotExist();
        assertThat(Paths.get(baseDir, "target/classes/META-INF/axelix-info.properties"))
                .exists();
    }

    @Test
    void shouldRollBackAndReportUnavailableWhenProfilerCannotBeResolved() {
        // given. the profiler is absent, and any resolution attempt after adding it fails -
        // simulating an environment (e.g. a closed network with no access to the profiler's
        // repository) where the artifact genuinely cannot be downloaded.
        MavenProject mavenProject = new MavenProject();
        mavenProject.setGroupId("com.example");
        mavenProject.setArtifactId("axelix-plugin-test");
        mavenProject.setVersion("1.0.0");

        ContributeDependenciesLifecycleExtension extension = new ContributeDependenciesLifecycleExtension();
        extension.dependencyResolver = new DependencyResolver() {
            @Override
            public List<Artifact> resolveDependencies(MavenProject project, RepositorySystemSession repoSession) {
                return List.of();
            }

            @Override
            public boolean isResolvable(MavenProject project, RepositorySystemSession repoSession) {
                return false;
            }
        };

        // when.
        boolean profilerAvailable = extension.contributeProfilerIfNecessary(mavenProject, null);

        // then. the tentatively-added dependency is rolled back, and the caller is told the
        // profiler is not available so it can skip binding the goals that depend on it.
        assertThat(profilerAvailable).isFalse();
        assertThat(mavenProject.getDependencies()).isEmpty();
    }

    /**
     * Read maven log file from test-project base directory
     * @param baseDir Project base directory
     * @return log content
     * @throws IOException
     */
    private static String readLogFile(String baseDir) throws IOException {
        Path logFilePath = Paths.get(baseDir + "/log.txt");

        return Files.readString(logFilePath);
    }

    private static void cleanTargetDir(String baseDir) throws IOException {
        Path targetDir = Paths.get(baseDir, "target");
        if (!Files.exists(targetDir)) {
            return;
        }

        try (Stream<Path> walk = Files.walk(targetDir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }
}
