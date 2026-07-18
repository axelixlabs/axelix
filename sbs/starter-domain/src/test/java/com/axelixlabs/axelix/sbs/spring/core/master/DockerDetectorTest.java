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
package com.axelixlabs.axelix.sbs.spring.core.master;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link DockerDetector}.
 *
 * @author Ilya Naumov
 */
class DockerDetectorTest {

    @TempDir
    private Path tempDir;

    private DockerDetector subject;

    @BeforeEach
    void setUp() throws Exception {
        Path tempFile = Files.createTempFile(tempDir, ".dockerenv", null);
        this.subject = new DockerDetector(tempFile);
    }

    @Test // GH-1219
    void returnsTrue_whenDockerEnvFileExists() {
        // when.
        boolean result = subject.hasDockerMarker();

        // then.
        assertThat(result).isTrue();
    }

    @Test // GH-1219
    void returnsFalse_whenFileDoesNotExist() {
        // given.
        DockerDetector detector = new DockerDetector(Path.of("/nonexistent/.dockerenv"));

        // when.
        boolean result = detector.hasDockerMarker();

        // then.
        assertThat(result).isFalse();
    }
}
