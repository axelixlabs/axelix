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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
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
    void should_add_profiler_dependency_if_not_present() throws VerificationException, IOException {
        // given.
        String baseDir = CURRENT_DIR + "/src/integrationTest/without-deps";
        Verifier verifier = new Verifier(baseDir);

        // when.
        verifier.executeGoal("dependency:list");
        verifier.verify(true);

        // then.
        String logOutput = readLogFile(baseDir);
        assertThat(logOutput).contains("digital.pragmatech.testing:spring-test-profiler:jar:0.1.2:test");
        assertThat(logOutput).contains("org.thymeleaf:thymeleaf:jar:3.1.5.RELEASE:test");
    }

    @Test
    void should_not_add_profiler_dependency_if_present() throws VerificationException, IOException {
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
    void should_noop_when_thymeleaf_is_lower_than_min() throws VerificationException, IOException {
        // given.
        String baseDir = CURRENT_DIR + "/src/integrationTest/lower-thymeleaf";
        Verifier verifier = new Verifier(baseDir);

        // when.
        verifier.executeGoal("dependency:list");
        verifier.verify(true);

        // then.
        String logOutput = readLogFile(baseDir);
        assertThat(logOutput).contains("org.thymeleaf:thymeleaf:jar:3.0.15.RELEASE");
        assertThat(logOutput).doesNotContain("digital.pragmatech.testing:spring-test-profiler");
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
}
