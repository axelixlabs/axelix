/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.sbs.spring.gclog;

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

import com.nucleonforge.axelix.common.api.gclog.GcLogStatusResponse;

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
