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

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CopyProfilerReportsMojo}
 *
 * @author Artemiy Degtyarev
 * @author Nikita Kirillov
 */
class CopyProfilerReportsMojoTest {
    public static final String CURRENT_DIR = new File("").getAbsolutePath();

    @Test
    void shouldCopyReport() throws VerificationException, IOException {
        // given.
        String baseDir = CURRENT_DIR + "/src/integrationTest/copy-report";
        Path classpathReportPath = Path.of(baseDir, "target/classes/META-INF/axelix/latest.html");
        Files.deleteIfExists(classpathReportPath);

        Verifier verifier = new Verifier(baseDir);
        verifier.setAutoclean(false);

        // when.
        verifier.executeGoal("install");
        verifier.verify(true);

        // then.
        assertThat(classpathReportPath).exists();
    }

    @Test
    void shouldSkipCopyingReportWhenProjectTargetsJavaBelow17() throws VerificationException, IOException {
        // given. the fixture's maven-compiler-plugin is configured for Java 11.
        String baseDir = CURRENT_DIR + "/src/integrationTest/java-11-target";
        Path classpathReportPath = Path.of(baseDir, "target/classes/META-INF/axelix/latest.html");
        Files.deleteIfExists(classpathReportPath);

        Verifier verifier = new Verifier(baseDir);
        verifier.setAutoclean(false);

        // when.
        verifier.executeGoal("install");
        verifier.verify(true);

        // then.
        assertThat(classpathReportPath).doesNotExist();
    }
}
