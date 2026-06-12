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
import java.nio.file.Files;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies the plugin end-to-end on empty Spring Boot applications: after running their tests,
 * the Spring Test Profiler must have produced its HTML report. The profiler requires Java 17+,
 * so these builds run on the current JVM.
 */
class SpringBootApplicationFunctionalTest {

    private static final String SPRING_BOOT_2_VERSION = "2.7.18";
    private static final String SPRING_BOOT_4_VERSION = "4.0.7";

    /** Boot-2-era Gradle; the io.spring.dependency-management plugin predates Gradle 9. */
    private static final String SPRING_BOOT_2_GRADLE_VERSION = "8.14";

    @TempDir Path projectDir;

    /**
     * The realistic Spring Boot 2 setup: the io.spring.dependency-management plugin applies the
     * Boot BOM with Maven semantics, downgrading the profiler's Thymeleaf to 3.0.x — without the
     * plugin's counteracting resolution rule the report silently fails to render.
     */
    @Test
    void springBoot2WithDependencyManagementGeneratesProfilerReport() throws IOException {
        // given.
        writeSpringBootApplicationSources();
        writeFile(
                "build.gradle",
                "plugins {\n"
                        + "    id 'io.spring.dependency-management' version '1.1.7'\n"
                        + "    id 'com.axelixlabs.axelix'\n"
                        + "}\n"
                        + "apply plugin: 'java'\n"
                        + "\n"
                        + "repositories { mavenCentral() }\n"
                        + "\n"
                        + "dependencyManagement {\n"
                        + "    imports { mavenBom 'org.springframework.boot:spring-boot-dependencies:"
                        + SPRING_BOOT_2_VERSION
                        + "' }\n"
                        + "}\n"
                        + "\n"
                        + "dependencies {\n"
                        + "    implementation 'org.springframework.boot:spring-boot-starter'\n"
                        + "    testImplementation 'org.springframework.boot:spring-boot-starter-test'\n"
                        + "}\n"
                        + "\n"
                        + "test { useJUnitPlatform() }\n");

        // when.
        BuildResult result =
                createRunner("test", "--stacktrace")
                        .withGradleVersion(SPRING_BOOT_2_GRADLE_VERSION)
                        .build();

        // then.
        assertProfilerReportGenerated(result);
    }

    @Test
    void springBoot4GeneratesProfilerReport() throws IOException {
        // given.
        writeSpringBootApplicationSources();
        writeFile(
                "build.gradle",
                "plugins {\n"
                        + "    id 'com.axelixlabs.axelix'\n"
                        + "}\n"
                        + "apply plugin: 'java'\n"
                        + "\n"
                        + "repositories { mavenCentral() }\n"
                        + "\n"
                        + "dependencies {\n"
                        + "    implementation platform('org.springframework.boot:spring-boot-dependencies:"
                        + SPRING_BOOT_4_VERSION
                        + "')\n"
                        + "    implementation 'org.springframework.boot:spring-boot-starter'\n"
                        + "    testImplementation 'org.springframework.boot:spring-boot-starter-test'\n"
                        // Gradle 9 requires the launcher on the test runtime classpath; the Boot
                        // BOM supplies its version via the imported junit-bom.
                        + "    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'\n"
                        + "}\n"
                        + "\n"
                        + "test { useJUnitPlatform() }\n");

        // when.
        BuildResult result = createRunner("test", "--stacktrace").build();

        // then.
        assertProfilerReportGenerated(result);
    }

    private GradleRunner createRunner(String... arguments) {
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .withArguments(arguments);
    }

    private void assertProfilerReportGenerated(BuildResult result) {
        assertThat(result.task(":test").getOutcome()).isEqualTo(TaskOutcome.SUCCESS);
        assertThat(result.task(":generateAxelixSpringFactories").getOutcome())
                .isEqualTo(TaskOutcome.SUCCESS);
        assertThat(projectDir.resolve("build/spring-test-profiler/latest.html")).exists();
    }

    private void writeSpringBootApplicationSources() throws IOException {
        writeFile("settings.gradle", "rootProject.name = 'axelix-spring-boot-test'\n");
        writeFile(
                "src/main/java/com/example/DemoApplication.java",
                "package com.example;\n"
                        + "\n"
                        + "import org.springframework.boot.autoconfigure.SpringBootApplication;\n"
                        + "\n"
                        + "@SpringBootApplication\n"
                        + "public class DemoApplication {}\n");
        writeFile(
                "src/test/java/com/example/DemoApplicationTest.java",
                "package com.example;\n"
                        + "\n"
                        + "import org.junit.jupiter.api.Test;\n"
                        + "import org.springframework.boot.test.context.SpringBootTest;\n"
                        + "\n"
                        + "@SpringBootTest\n"
                        + "class DemoApplicationTest {\n"
                        + "    @Test\n"
                        + "    void contextLoads() {}\n"
                        + "}\n");
    }

    private void writeFile(String relativePath, String content) throws IOException {
        Path file = projectDir.resolve(relativePath);
        Files.createDirectories(file.getParent());
        Files.write(file, content.getBytes(UTF_8));
    }
}
