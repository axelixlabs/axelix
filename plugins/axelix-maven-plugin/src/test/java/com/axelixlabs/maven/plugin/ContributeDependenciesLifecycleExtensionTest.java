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
package com.axelixlabs.maven.plugin;

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
 */
class ContributeDependenciesLifecycleExtensionTest {

    public static final String CURRENT_DIR = new File("").getAbsolutePath();

    @Test
    void should_add_profiler_dependency_if_not_present() throws VerificationException, IOException {
        String baseDir = CURRENT_DIR + "/src/integrationTest/without-deps";

        Verifier verifier = new Verifier(baseDir);
        verifier.executeGoal("dependency:list");

        verifier.verify(true);

        assertThat(readLogFile(baseDir)).contains("digital.pragmatech.testing:spring-test-profiler:jar:0.1.2:test");
    }

    @Test
    void should_not_add_profiler_dependency_if_present() throws VerificationException, IOException {
        String baseDir = CURRENT_DIR + "/src/integrationTest/contains-deps";

        Verifier verifier = new Verifier(baseDir);
        verifier.executeGoal("dependency:list");

        verifier.verify(true);

        assertThat(readLogFile(baseDir)).contains("digital.pragmatech.testing:spring-test-profiler:jar:0.1.1:test");
    }

    @Test
    void should_add_thymeleaf_present_if_lower_than_min() throws VerificationException, IOException {
        String baseDir = CURRENT_DIR + "/src/integrationTest/lower-thymeleaf";

        Verifier verifier = new Verifier(baseDir);
        verifier.executeGoal("dependency:list");

        verifier.verify(true);

        assertThat(readLogFile(baseDir)).contains("org.thymeleaf:thymeleaf:jar:3.1.5.RELEASE");
        assertThat(readLogFile(baseDir)).doesNotContain("org.thymeleaf:thymeleaf:jar:3.0.15.RELEASE");
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
