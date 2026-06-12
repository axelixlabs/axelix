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
package com.axelixlabs.gradle.plugin;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AxelixGradlePluginFunctionalTest {

    private static final String MIN_GRADLE_VERSION = "4.0";
    private static final String MAX_GRADLE_VERSION = "9.5.1";

    private static final String EXPECTED_SPRING_FACTORIES =
            "org.springframework.test.context.TestExecutionListener=\\\n"
                    + "digital.pragmatech.testing.SpringTestProfilerListener\n"
                    + "org.springframework.context.ApplicationContextInitializer=\\\n"
                    + "digital.pragmatech.testing.diagnostic.ContextDiagnosticApplicationInitializer\n";

    @TempDir Path projectDir;

    @ParameterizedTest
    @ValueSource(strings = {MIN_GRADLE_VERSION, MAX_GRADLE_VERSION})
    void addsProfilerDependencyAndGeneratesSpringFactories(String gradleVersion)
            throws IOException {
        // given.
        writeCommonProjectFiles(gradleVersion);
        writeFile(
                "build.gradle",
                "plugins {\n"
                        + "    id 'com.axelixlabs.axelix'\n"
                        + "}\n"
                        // Applied after our plugin on purpose: exercises the withPlugin reaction.
                        + "apply plugin: 'java'\n"
                        + "\n"
                        + "repositories { mavenCentral() }\n"
                        + "\n"
                        + "task printTestRuntimeClasspath {\n"
                        + "    dependsOn configurations.testRuntimeClasspath\n"
                        + "    doLast {\n"
                        + "        configurations.testRuntimeClasspath.files.each { f ->\n"
                        + "            println 'TRC>> ' + f.absolutePath\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n");

        // when.
        BuildResult result =
                createRunner(gradleVersion, "printTestRuntimeClasspath", "--stacktrace").build();

        // then.
        assertThat(result.task(":printTestRuntimeClasspath").getOutcome())
                .isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.task(":generateAxelixSpringFactories").getOutcome())
                .isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.getOutput()).contains("spring-test-profiler-0.1.2.jar");
        assertThat(result.getOutput().lines().filter(line -> line.startsWith("TRC>> ")))
                .anySatisfy(line -> assertThat(line).endsWith("build/generated/axelix"));

        Path springFactories =
                projectDir.resolve("build/generated/axelix/META-INF/spring.factories");
        assertThat(springFactories).exists();
        assertThat(new String(Files.readAllBytes(springFactories), UTF_8))
                .isEqualTo(EXPECTED_SPRING_FACTORIES);
    }

    @ParameterizedTest
    @ValueSource(strings = {MIN_GRADLE_VERSION, MAX_GRADLE_VERSION})
    void springFactoriesIsVisibleToTestsAtRuntime(String gradleVersion) throws IOException {
        // given.
        writeCommonProjectFiles(gradleVersion);
        writeFile(
                "build.gradle",
                "plugins {\n"
                        + "    id 'com.axelixlabs.axelix'\n"
                        + "}\n"
                        + "apply plugin: 'java'\n"
                        + "\n"
                        + "repositories { mavenCentral() }\n"
                        + "\n"
                        + "dependencies { testImplementation 'junit:junit:4.13.2' }\n");
        // The test source must stay Java-8 compatible: on Gradle 4.0 it is compiled by JDK 8.
        writeFile(
                "src/test/java/FactoriesVisibleTest.java",
                "import java.io.BufferedReader;\n"
                        + "import java.io.InputStreamReader;\n"
                        + "import java.net.URL;\n"
                        + "import java.util.Enumeration;\n"
                        + "import org.junit.Test;\n"
                        + "import static org.junit.Assert.assertTrue;\n"
                        + "\n"
                        + "public class FactoriesVisibleTest {\n"
                        + "    @Test\n"
                        + "    public void factoriesOnClasspath() throws Exception {\n"
                        + "        Enumeration<URL> urls = getClass().getClassLoader()\n"
                        + "                .getResources(\"META-INF/spring.factories\");\n"
                        + "        boolean found = false;\n"
                        + "        while (urls.hasMoreElements()) {\n"
                        + "            BufferedReader reader = new BufferedReader(new InputStreamReader(\n"
                        + "                    urls.nextElement().openStream(), \"UTF-8\"));\n"
                        + "            StringBuilder content = new StringBuilder();\n"
                        + "            String line;\n"
                        + "            while ((line = reader.readLine()) != null) {\n"
                        + "                content.append(line).append('\\n');\n"
                        + "            }\n"
                        + "            reader.close();\n"
                        + "            if (content.toString().contains(\n"
                        + "                    \"digital.pragmatech.testing.SpringTestProfilerListener\")) {\n"
                        + "                found = true;\n"
                        + "            }\n"
                        + "        }\n"
                        + "        assertTrue(found);\n"
                        + "    }\n"
                        + "}\n");

        // when.
        BuildResult result = createRunner(gradleVersion, "test", "--stacktrace").build();

        // then.
        assertThat(result.task(":test").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
    }

    private GradleRunner createRunner(String gradleVersion, String... arguments) {
        // Never call withDebug(true) here: a debug run executes the build in-process on the
        // current (modern) JVM, bypassing the JDK 8 daemon required by Gradle 4.0.
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .withGradleVersion(gradleVersion)
                .withArguments(arguments);
    }

    private void writeCommonProjectFiles(String gradleVersion) throws IOException {
        writeFile("settings.gradle", "rootProject.name = 'axelix-plugin-test'\n");
        if (gradleVersion.startsWith("4.")) {
            // Gradle 4.0 daemons cannot run on Java 9+, so fork them on a JDK 8.
            writeFile(
                    "gradle.properties",
                    "org.gradle.java.home=" + locateJdk8Home() + "\n"
                            + "org.gradle.jvmargs=-Xmx512m\n");
        }
    }

    private void writeFile(String relativePath, String content) throws IOException {
        Path file = projectDir.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.write(file, content.getBytes(UTF_8));
    }

    private static String locateJdk8Home() {
        String override = System.getenv("AXELIX_TEST_JDK8_HOME");
        if (override != null && !override.isEmpty()) {
            return override;
        }
        Path candidates = Paths.get(System.getProperty("user.home"), ".sdkman/candidates/java");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(candidates, "8.*")) {
            for (Path candidate : stream) {
                if (Files.isExecutable(candidate.resolve("bin/java"))) {
                    return candidate.toString();
                }
            }
        } catch (IOException ignored) {
            // Fall through to the failure below.
        }
        throw new IllegalStateException(
                "No JDK 8 found for the Gradle "
                        + MIN_GRADLE_VERSION
                        + " functional tests. Install Liberica JDK 8 via sdkman:\n"
                        + "  source ~/.sdkman/bin/sdkman-init.sh && echo n | sdk install java"
                        + " 8.0.492-librca\n"
                        + "or point AXELIX_TEST_JDK8_HOME at an existing JDK 8 installation.");
    }
}
