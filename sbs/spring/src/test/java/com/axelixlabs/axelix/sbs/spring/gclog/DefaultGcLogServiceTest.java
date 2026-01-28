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
package com.axelixlabs.axelix.sbs.spring.gclog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.axelixlabs.axelix.common.api.gclog.GcLogStatusResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test for {@link DefaultGcLogService}
 *
 * @since 11.01.2026
 * @author Nikita Kirillov
 */
class DefaultGcLogServiceTest {

    private static final DefaultGcLogService subject = new DefaultGcLogService(new JcmdExecutor());

    @AfterEach
    void tearDown() {
        subject.disable();
    }

    @AfterAll
    static void afterAll() throws InterruptedException, IOException {
        Thread.sleep(500);
        Files.deleteIfExists(Path.of("gc.log"));
        Files.deleteIfExists(Path.of("gc.log.0"));
    }

    @Test
    void shouldReturnOnlyEnableableGcLogLevels() {
        var levels = subject.getStatus().availableLevels();

        assertThat(levels).isNotEmpty().doesNotContain("off");
    }

    @ParameterizedTest
    @MethodSource("availableLevelsProvider")
    void shouldEnableGcLoggingForEveryAvailableLevel(String level) {
        subject.enable(level);
        GcLogStatusResponse status = subject.getStatus();

        assertThat(status.enabled()).isTrue();
        assertThat(status.level()).isEqualTo(level);

        subject.disable();
    }

    private static Stream<String> availableLevelsProvider() {
        return subject.getStatus().availableLevels().stream();
    }

    @Test
    void shouldDisableGcLogging() {
        List<String> availableLevels = subject.getStatus().availableLevels();

        subject.enable(availableLevels.get(0));
        subject.disable();

        GcLogStatusResponse status = subject.getStatus();

        assertThat(status.enabled()).isFalse();
        assertThat(status.level()).isNull();
    }

    @Test
    void shouldCreateGcLogFileAndWriteToIt() throws InterruptedException, IOException {
        List<String> availableLevels = subject.getStatus().availableLevels();
        subject.enable(availableLevels.get(0));

        System.gc();

        Thread.sleep(500);

        File logFile = subject.getGcLogFile();
        assertThat(logFile).exists().isFile();

        String content = Files.readString(logFile.toPath());
        assertThat(content).isNotBlank();
    }

    @Test
    void shouldThrowExceptionWhenEnableWithNullLevel() {
        assertThatThrownBy(() -> subject.enable(null)).isInstanceOf(GcLogException.class);
    }

    @Test
    void shouldThrowExceptionWhenEnableWithInvalidLevel() {
        assertThatThrownBy(() -> subject.enable("invalid-level")).isInstanceOf(GcLogException.class);
    }

    @Test
    void shouldThrowExceptionWhenEnableWithOffLevel() {
        assertThatThrownBy(() -> subject.enable("off")).isInstanceOf(GcLogException.class);
    }
}
