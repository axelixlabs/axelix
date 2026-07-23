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
package com.axelixlabs.axelix.gradle.plugin.profiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.axelixlabs.axelix.gradle.plugin.AbstractAxelixPluginFunctionalTest;
import com.axelixlabs.axelix.gradle.plugin.GradleProjectFixtures;

import static com.axelixlabs.axelix.gradle.plugin.profiler.SpringTestProfilerReportCopy.COPY_PROFILER_REPORT_TASK_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Functional tests for {@link SpringTestProfilerConfigurer}.
 *
 * @author Artemiy Degtyarev
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
class SpringTestProfilerConfigurerFunctionalTest extends AbstractAxelixPluginFunctionalTest {

    private static final String PREEXISTING_SPRING_FACTORIES_CONTENT =
            "com.example.CustomFactory=com.example.CustomFactoryImpl\n";

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void addsProfilerAndRelyOnTransitiveThymeleafWhenNothingPresent(String gradleVersion) throws IOException {
        // given.
        setupProject("no-thymeleaf-and-profiler.gradle.kts");

        // when.
        BuildResult result = createRunner(gradleVersion, "printTestRuntimeClasspath", "--stacktrace", "-i")
                .build();

        // then.
        assertThat(result.getOutput())
                // profiler is declared explicitly by the plugin.
                .contains("RESOLVED>> " + SpringTestProfilerConfigurer.PROFILER_DEPENDENCY)
                // thymeleaf is NOT declared by the plugin, only pulled in transitively by the profiler.
                .doesNotContain("DEP>> " + SpringTestProfilerConfigurer.THYMELEAF_GROUP + ":"
                        + SpringTestProfilerConfigurer.THYMELEAF_NAME)
                .contains("RESOLVED>> " + SpringTestProfilerConfigurer.THYMELEAF_GROUP + ":"
                        + SpringTestProfilerConfigurer.THYMELEAF_NAME);

        // and.
        assertThat(result.task(":printTestRuntimeClasspath").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.task(":" + SpringFactoriesGenerator.GENERATE_TASK_NAME)
                        .getOutcome())
                .isEqualTo(TaskOutcome.SUCCESS);
        assertGeneratedSpringFactoriesContainOnlyProfilerRegistration();
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void appendsProfilerRegistrationWithoutOverridingPreexistingSpringFactories(String gradleVersion)
            throws IOException {
        // given.
        setupProject("preexisting-spring-factories.gradle");

        // when.
        BuildResult result = createRunner(gradleVersion, SpringFactoriesGenerator.GENERATE_TASK_NAME, "--stacktrace")
                .build();

        // then.
        assertThat(result.task(":" + SpringFactoriesGenerator.GENERATE_TASK_NAME)
                        .getOutcome())
                .isEqualTo(TaskOutcome.SUCCESS);
        Path springFactories = projectDir.resolve("build/generated/axelix/META-INF/spring.factories");
        String actual = new String(Files.readAllBytes(springFactories), UTF_8);
        assertThat(actual)
                .isEqualTo(PREEXISTING_SPRING_FACTORIES_CONTENT + SpringFactoriesGenerator.SPRING_FACTORIES_CONTENT);
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void doesNotContributeAnythingWhenProfilerAndThymeleafAreAlreadyInClassPath(String gradleVersion)
            throws IOException {
        // given.
        setupProject("preexisting-thymeleaf-and-profiler.gradle");

        // when.
        BuildResult result =
                createRunner(gradleVersion, "printDeclaredDeps", "--stacktrace").build();

        // then.
        assertThat(result.task(":printDeclaredDeps").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.getOutput())
                .contains(SpringTestProfilerConfigurer.PROFILER_NAME + ":0.1.1")
                .contains(SpringTestProfilerConfigurer.THYMELEAF_NAME + ":3.1.5.RELEASE")
                // dependency that we want to add
                .doesNotContain(SpringTestProfilerConfigurer.PROFILER_DEPENDENCY);
        assertSpringFactoriesAreNotGenerated(result);
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void shouldOnlyAddProfilerAndNotAlterAnExistingThymeleafInTheClassPath(String gradleVersion) throws IOException {
        // given.
        setupProject("preexisitng-compatible-thymeleaf.gradle");

        // when.
        BuildResult result = createRunner(
                        gradleVersion, "printDeclaredDeps", SpringFactoriesGenerator.GENERATE_TASK_NAME, "--stacktrace")
                .build();

        // then.
        assertThat(result.task(":printDeclaredDeps").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.getOutput())
                .contains(SpringTestProfilerConfigurer.THYMELEAF_NAME + ":3.1.4.RELEASE")
                .contains(SpringTestProfilerConfigurer.PROFILER_DEPENDENCY);
        assertThat(result.task(":" + SpringFactoriesGenerator.GENERATE_TASK_NAME)
                        .getOutcome())
                .isEqualTo(TaskOutcome.SUCCESS);
        assertGeneratedSpringFactoriesContainOnlyProfilerRegistration();
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void shouldCopyProfilerReport(String gradleVersion) throws IOException {
        // given.
        setupProject("no-thymeleaf-and-profiler.gradle.kts");
        writeFile("build/spring-test-profiler/latest.html", "some-content");

        // when.
        BuildResult result =
                createRunner(gradleVersion, COPY_PROFILER_REPORT_TASK_NAME).build();

        // then. The fixture declares no explicit sourceCompatibility, so it defaults to the JVM
        // running this build (the same JVM TestKit hands to the Gradle daemon under test).
        Path path = projectDir.resolve("build/generated/axelix-report/META-INF/axelix/latest.html");
        if (Runtime.version().feature() >= 17) {
            assertThat(result.task(":copyProfilerReport").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
            assertThat(path).exists();
        } else {
            assertThat(result.task(":copyProfilerReport").getOutcome()).isEqualTo(TaskOutcome.SKIPPED);
            assertThat(path).doesNotExist();
        }
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void doesNothingWhenDisabledViaExtension(String gradleVersion) throws IOException {
        // given.
        setupProject("copy-profiler-report-disabled.gradle.kts");
        writeFile("build/spring-test-profiler/latest.html", "some-content");

        // when. the copyProfilerReport task must not be registered at all, not merely skipped,
        // so a disabled extension can never conflict with the user's own task graph.
        BuildResult taskNotFound =
                createRunner(gradleVersion, COPY_PROFILER_REPORT_TASK_NAME).buildAndFail();

        // then.
        assertThat(taskNotFound.getOutput()).contains("Task '" + COPY_PROFILER_REPORT_TASK_NAME + "' not found");

        // and. no dependency was added and no spring.factories was generated either.
        BuildResult declaredDeps =
                createRunner(gradleVersion, "printDeclaredDeps", "--stacktrace").build();
        assertThat(declaredDeps.getOutput()).doesNotContain(SpringTestProfilerConfigurer.PROFILER_NAME);
        assertSpringFactoriesAreNotGenerated(declaredDeps);
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void skipsProfilerWithoutFailingTheBuildWhenItCannotBeResolved(String gradleVersion) throws IOException {
        // given. no repositories are configured at all, so the profiler genuinely cannot be
        // resolved - simulating e.g. a closed network with no access to its repository.
        setupProject("no-repositories.gradle.kts");

        // when.
        BuildResult result =
                createRunner(gradleVersion, "printDeclaredDeps", "--stacktrace").build();

        // then. the build succeeds without the profiler, instead of failing on an unresolvable dependency.
        assertThat(result.task(":printDeclaredDeps").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.getOutput()).doesNotContain(SpringTestProfilerConfigurer.PROFILER_DEPENDENCY);
        // generateAxelixSpringFactories still runs (it's always wired onto the test runtime classpath),
        // it just skips writing the registration since the profiler never made it in.
        assertThat(projectDir.resolve("build/generated/axelix/META-INF/spring.factories"))
                .doesNotExist();
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void skipsCopyingProfilerReportWhenProjectTargetsJavaBelow17(String gradleVersion) throws IOException {
        // given.
        setupProject("java-11-target.gradle.kts");
        writeFile("build/spring-test-profiler/latest.html", "some-content");

        // when.
        BuildResult result =
                createRunner(gradleVersion, COPY_PROFILER_REPORT_TASK_NAME).build();

        // then.
        assertThat(result.task(":copyProfilerReport").getOutcome()).isEqualTo(TaskOutcome.SKIPPED);
        assertThat(projectDir.resolve("build/generated/axelix-report/META-INF/axelix/latest.html"))
                .doesNotExist();
    }

    @ParameterizedTest
    @MethodSource("gradleVersionsUnderTest")
    void ordersTestThenCopyProfilerReportThenBootJar(String gradleVersion) throws IOException {
        // given.
        setupProject("bootjar-ordering.gradle.kts");

        // when.
        BuildResult result =
                createRunner(gradleVersion, "bootJar", "--stacktrace").build();

        // then.
        List<String> executionOrder =
                result.getTasks().stream().map(task -> task.getPath()).collect(Collectors.toList());
        int testIndex = executionOrder.indexOf(":test");
        int copyReportIndex = executionOrder.indexOf(":" + COPY_PROFILER_REPORT_TASK_NAME);
        int bootJarIndex = executionOrder.indexOf(":bootJar");

        assertThat(testIndex).isNotNegative();
        assertThat(copyReportIndex).isNotNegative();
        assertThat(bootJarIndex).isNotNegative();
        assertThat(testIndex).isLessThan(copyReportIndex);
        assertThat(copyReportIndex).isLessThan(bootJarIndex);
    }

    private void setupProject(String buildGradleFixture) throws IOException {
        writeFile("settings.gradle", "rootProject.name = 'axelix-plugin-test'\n");
        String buildFileName = buildGradleFixture.endsWith(".kts") ? "build.gradle.kts" : "build.gradle";
        writeFile(buildFileName, GradleProjectFixtures.loadContent(buildGradleFixture));
    }

    private void assertGeneratedSpringFactoriesContainOnlyProfilerRegistration() throws IOException {
        Path springFactories = projectDir.resolve("build/generated/axelix/META-INF/spring.factories");
        assertThat(springFactories).exists();
        assertThat(new String(Files.readAllBytes(springFactories), UTF_8))
                .isEqualTo(SpringFactoriesGenerator.SPRING_FACTORIES_CONTENT);
    }

    private void assertSpringFactoriesAreNotGenerated(BuildResult result) {
        assertThat(result.task(":" + SpringFactoriesGenerator.GENERATE_TASK_NAME))
                .isNull();
        assertThat(projectDir.resolve("build/generated/axelix/META-INF/spring.factories"))
                .doesNotExist();
    }
}
