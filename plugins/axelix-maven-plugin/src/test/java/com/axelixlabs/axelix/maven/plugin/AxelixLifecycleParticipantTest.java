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
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.jupiter.api.Test;

import static com.axelixlabs.axelix.maven.plugin.AxelixLifecycleParticipant.PROFILER_DETECTED_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link AxelixLifecycleParticipant}
 *
 * @author Artemiy Degtyarev
 * @author Mikhail Polivakha
 */
class AxelixLifecycleParticipantTest {

    public static final String CURRENT_DIR = new File("").getAbsolutePath();

    @Test
    void shouldNotContributeProfilerDependencyWhenAbsent() throws VerificationException, IOException {
        // given.
        String baseDir = CURRENT_DIR + "/src/integrationTest/without-deps";
        Verifier verifier = new Verifier(baseDir);

        // when.
        verifier.executeGoal("dependency:list");
        verifier.verify(true);

        // then. the plugin only detects and logs; it never adds the dependency itself.
        String logOutput = readLogFile(baseDir);
        assertThat(logOutput).doesNotContain("digital.pragmatech.testing:spring-test-profiler");
        assertThat(logOutput).contains("Spring Test Profiler was not detected in this reactor build");
    }

    @Test
    void shouldDetectPresentProfilerWithoutAlteringIt() throws VerificationException, IOException {
        // given.
        String baseDir = CURRENT_DIR + "/src/integrationTest/contains-deps";
        Verifier verifier = new Verifier(baseDir);

        // when.
        verifier.executeGoal("dependency:list");
        verifier.verify(true);

        // then. the pre-declared version is left untouched, and detection is logged.
        String logOutput = readLogFile(baseDir);
        assertThat(logOutput).contains("digital.pragmatech.testing:spring-test-profiler:jar:0.1.1:test");
        assertThat(logOutput).contains("Spring Test Profiler detected in this reactor build");
    }

    @Test
    void shouldRunProjectInfoGoalAutomaticallyWithoutAnyExecutionDeclared() throws VerificationException, IOException {
        // given. the pom declares only <extensions>true</extensions>, no <executions> at all.
        String baseDir = CURRENT_DIR + "/src/integrationTest/auto-bind-goals";
        cleanTargetDir(baseDir);

        // when.
        Verifier installVerifier = new Verifier(baseDir);
        installVerifier.executeGoal("install");
        installVerifier.verify(true);

        // then. project-info generation ran automatically, without any dependency contribution,
        // and it recorded that the profiler was not detected.
        Path infoFile = Paths.get(baseDir, "target/classes/META-INF/axelix-info.properties");
        assertThat(infoFile).exists();
        assertThat(loadProperties(infoFile).getProperty(PROFILER_DETECTED_PROPERTY))
                .isEqualTo("false");
    }

    @Test
    void shouldDetectProfilerPresentInReactorWhenOnlySharedCommonModuleDeclaresIt()
            throws VerificationException, IOException {
        // given. a reactor with a shared "common" module that depends on the profiler, and two
        // sibling modules ("module-a", "module-b") that only depend on common - the profiler is
        // test-scoped in common, so it is never transitively visible on the siblings' own
        // dependency graph.
        String baseDir = CURRENT_DIR + "/src/integrationTest/reactor-shared-common";
        cleanTargetDir(baseDir + "/common");
        cleanTargetDir(baseDir + "/module-a");
        cleanTargetDir(baseDir + "/module-b");

        // when.
        Verifier installVerifier = new Verifier(baseDir);
        installVerifier.executeGoal("install");
        installVerifier.verify(true);

        // then. the reactor-wide detection is driven by "common" alone.
        String logOutput = readLogFile(baseDir);
        assertThat(logOutput).contains("Spring Test Profiler detected in this reactor build");

        // and. project-info is generated for every module in the reactor, all reporting the same
        // reactor-wide "detected" outcome - even module-a/module-b, which never resolve the
        // profiler on their own dependency graph.
        for (String module : List.of("common", "module-a", "module-b")) {
            Path infoFile = Paths.get(baseDir, module, "target/classes/META-INF/axelix-info.properties");
            assertThat(infoFile).exists();
            assertThat(loadProperties(infoFile).getProperty(PROFILER_DETECTED_PROPERTY))
                    .isEqualTo("true");
        }
    }

    @Test
    void shouldNotAutoBindProjectInfoGoalWhenModuleOverridesExtensionsToFalse()
            throws VerificationException, IOException {
        // given. the parent declares <extensions>true</extensions> (which is what loads the extension for
        // the whole reactor in the first place), "with-extensions" inherits it as-is, and "without-extensions"
        // overrides its own plugin declaration back to <extensions>false</extensions>.
        String baseDir = CURRENT_DIR + "/src/integrationTest/reactor-extension-override";
        cleanTargetDir(baseDir + "/with-extensions");
        cleanTargetDir(baseDir + "/without-extensions");

        // when.
        Verifier installVerifier = new Verifier(baseDir);
        installVerifier.executeGoal("install");
        installVerifier.verify(true);

        // then. auto-binding only applies to the module that actually carries extensions=true.
        assertThat(Paths.get(baseDir, "with-extensions/target/classes/META-INF/axelix-info.properties"))
                .exists();
        assertThat(Paths.get(baseDir, "without-extensions/target/classes/META-INF/axelix-info.properties"))
                .doesNotExist();
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

    private static Properties loadProperties(Path propertiesFile) throws IOException {
        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(propertiesFile)) {
            properties.load(inputStream);
        }
        return properties;
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
