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
package com.axelixlabs.axelix.sbs.spring.core.gclog;

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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.axelixlabs.axelix.common.api.gclog.GcLogStatus;
import com.axelixlabs.axelix.sbs.spring.core.testutils.NoOpLogger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test for {@link DefaultGcLogService}
 *
 * @since 11.01.2026
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
class DefaultGcLogServiceTest {

    private static final DefaultGcLogService subject = new DefaultGcLogService(new JcmdExecutor(), new NoOpLogger());

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
        var levels = subject.getStatus().getAvailableLevels();

        assertThat(levels).isNotEmpty().doesNotContain("off");
    }

    @ParameterizedTest
    @MethodSource("availableLevelsProvider")
    void shouldEnableGcLoggingForEveryAvailableLevel(String level) {
        subject.enable(level);
        GcLogStatus status = subject.getStatus();

        assertThat(status.isEnabled()).isTrue();
        assertThat(status.getLevel()).isEqualTo(level);

        subject.disable();
    }

    private static Stream<String> availableLevelsProvider() {
        return subject.getStatus().getAvailableLevels().stream();
    }

    @ParameterizedTest
    @MethodSource("enabledGcLogStatusProvider")
    void getStatus_shouldReturnEnabled_whenUnifiedLoggingSelectorsContainGc(String vmLogOutput, String level) {
        // given.
        var subject = new DefaultGcLogService(new StaticJcmdExecutor(vmLogOutput), new NoOpLogger());

        // when.
        GcLogStatus status = subject.getStatus();

        // then.
        assertThat(status.isEnabled()).isTrue();
        assertThat(status.getLevel()).isEqualTo(level);
    }

    private static Stream<Arguments> enabledGcLogStatusProvider() {
        return Stream.of(
                Arguments.of(vmLogList(" #0: stdout gc=info uptime,level,tags"), "info"),
                Arguments.of(vmLogList(" #0: stdout all=off,gc=info uptime,level,tags"), "info"),
                Arguments.of(vmLogList(" #0: stdout gc uptime,level,tags"), "info"),
                Arguments.of(vmLogList(" #0: stdout gc*=debug uptime,level,tags"), "debug"),
                Arguments.of(vmLogList(" #0: stdout all=off,gc*=debug uptime,level,tags"), "debug"),
                Arguments.of(vmLogList(" #0: stdout gc+heap=info uptime,level,tags"), "info"),
                Arguments.of(vmLogList(" #0: stdout all=off,gc+heap=trace uptime,level,tags"), "trace"),
                Arguments.of(vmLogList(" #0: stdout gc+heap=info,gc+pause=debug uptime,level,tags"), "debug"),
                Arguments.of(vmLogList(" #0: stdout gc+heap*=trace uptime,level,tags"), "trace"),
                Arguments.of(vmLogList(" #0: stdout all=info uptime,level,tags"), "info"),
                Arguments.of(vmLogList(" #0: stdout all uptime,level,tags"), "info"),
                Arguments.of(vmLogList(" #0: stdout all=debug uptime,level,tags"), "debug"),
                Arguments.of(vmLogList(" #0: stdout all=trace uptime,level,tags"), "trace"),
                Arguments.of(vmLogList(" #0: stdout gc=off uptime,level,tags"), null),
                Arguments.of(vmLogList(" #0: stdout gc*=off uptime,level,tags"), null),
                Arguments.of(vmLogList(" #0: stdout all=info,gc*=off uptime,level,tags"), "info"),
                Arguments.of(vmLogList(" #0: stdout gc*=info,gc*=off uptime,level,tags"), "info"),
                Arguments.of(vmLogList(" #0: stdout gc*=info,rt*=off uptime,level,tags"), "info"),
                Arguments.of(vmLogList(" #0: stdout tc=info,gc=info uptime,level,tags"), "info"),
                Arguments.of(vmLogList(" #0: stdout gc=trace,gc*=info uptime,level,tags"), "trace"),
                Arguments.of(vmLogList(" #0: stdout gc+heap=off,gc+pause=debug uptime,level,tags"), "debug"),
                Arguments.of(
                        vmLogList(
                                " #0: stdout all=warning uptime,level,tags",
                                " #1: file=gc.log gc=trace uptime,level,tags"),
                        "trace"),
                Arguments.of(vmLogList(" #0: stdout gc=off,gc*=info uptime,level,tags"), "info"),
                Arguments.of(vmLogList(" #0: stdout gc=off,gc+heap=debug uptime,level,tags"), "debug"),
                Arguments.of(vmLogList(" #0: stdout gc=off,gc=trace uptime,level,tags"), "trace"),
                Arguments.of(vmLogList(" #0: stdout gc=off,all=info uptime,level,tags"), "info"),
                Arguments.of(vmLogList(" #0: stdout all=warning,gc=info uptime,level,tags"), "info"),
                Arguments.of(vmLogList(" #0: stdout all=off,gc=debug uptime,level,tags"), "debug"),
                Arguments.of(vmLogList(" #0: stdout gc=info,gc=debug uptime,level,tags"), "debug"),
                Arguments.of(vmLogList(" #0: stdout gc=warning,gc=trace,gc=info uptime,level,tags"), "trace"),
                Arguments.of(
                        vmLogList(" #0: stdout gc+heap=error,gc+pause=info,gc+region=debug uptime,level,tags"),
                        "debug"),
                Arguments.of(vmLogList(" #0: stdout gc,gc=debug uptime,level,tags"), "debug"),
                Arguments.of(vmLogList(" #0: stdout gc,gc*=info uptime,level,tags"), "info"),
                Arguments.of(vmLogList(" #0: stdout gc=off,gc=off uptime,level,tags"), null));
    }

    @ParameterizedTest
    @MethodSource("disabledGcLogStatusProvider")
    void getStatus_shouldReturnDisabled_whenUnifiedLoggingSelectorsDoNotContainGc(String vmLogOutput) {
        // given.
        var subject = new DefaultGcLogService(new StaticJcmdExecutor(vmLogOutput), new NoOpLogger());

        // when.
        GcLogStatus status = subject.getStatus();

        // then.
        assertThat(status.isEnabled()).isFalse();
        assertThat(status.getLevel()).isNull();
    }

    private static Stream<String> disabledGcLogStatusProvider() {
        return Stream.of(
                vmLogList(" #0: stdout all=warning uptime,level,tags"),
                vmLogList(" #0: stdout all=error uptime,level,tags"),
                vmLogList(" #0: stdout all=warning uptime,level,tags", " #1: stderr all=off uptime,level,tags"),
                vmLogList(" #0: stdout all=off uptime,level,tags"),
                vmLogList(" #0: stdout rt*=off uptime,level,tags"),
                vmLogList(" #0: stdout heap=info uptime,level,tags"),
                vmLogList(" #0: stdout heap+gc=info uptime,level,tags"),
                vmLogList(" #0: stdout heap+gc*=info uptime,level,tags"),
                vmLogList(" #0: stdout gcx=info uptime,level,tags"),
                vmLogList(" #0: stdout xgc=info uptime,level,tags"),
                vmLogList(" #0: stdout tc-info uptime,level,tags"),
                vmLogList(" #0: stdout tc=info uptime,level,tags"),
                vmLogList(" #0: stdout GC=INFO uptime,level,tags"),
                vmLogList(" #0:"),
                vmLogList());
    }

    @ParameterizedTest
    @MethodSource("specifiedLogFileProvider")
    void isGcLogFileSpecified_shouldReturnTrue_whenJvmLogFileOutputIsSpecified(String vmLogOutput) {
        // given.
        var subject = new DefaultGcLogService(new StaticJcmdExecutor(vmLogOutput), new NoOpLogger());

        // when.
        boolean status = subject.isGcLogFileSpecified();

        // then.
        assertThat(status).isTrue();
    }

    private static Stream<String> specifiedLogFileProvider() {
        return Stream.of(
                vmLogList(" #0: file=gc.log gc*=debug time,level,tags"),
                vmLogList(" #0: file=gc.log all=off,gc*=debug time,level,tags"),
                vmLogList(" #0: file=/tmp/custom-gc.log gc=info time,level,tags"),
                vmLogList(" #0: file=logs/app-gc.log gc+heap=debug time,level,tags"),
                vmLogList(" #0: file=gc.log all=info time,level,tags"),
                vmLogList(" #0: file=gc.log gc*=off time,level,tags"));
    }

    @ParameterizedTest
    @MethodSource("notSpecifiedLogFileProvider")
    void isGcLogFileSpecified_shouldReturnFalse_whenJvmLogFileOutputIsNotSpecified(String vmLogOutput) {
        // given.
        var subject = new DefaultGcLogService(new StaticJcmdExecutor(vmLogOutput), new NoOpLogger());

        // when.
        boolean status = subject.isGcLogFileSpecified();

        // then.
        assertThat(status).isFalse();
    }

    private static Stream<String> notSpecifiedLogFileProvider() {
        return Stream.of(
                vmLogList(" #0: stdout gc*=debug time,level,tags"),
                vmLogList(" #0: stderr gc=info time,level,tags"),
                vmLogList(" #0: stdout all=info time,level,tags"),
                vmLogList(" #0: file=app.log heap=info time,level,tags"),
                vmLogList(" #0: file=app.log all=warning time,level,tags"),
                vmLogList(" #0:"),
                vmLogList());
    }

    @Test
    void shouldDisableGcLogging() {
        List<String> availableLevels = subject.getStatus().getAvailableLevels();

        subject.enable(availableLevels.get(0));
        subject.disable();

        GcLogStatus status = subject.getStatus();

        assertThat(status.isEnabled()).isFalse();
        assertThat(status.getLevel()).isNull();
    }

    @Test
    void shouldCreateGcLogFileAndWriteToIt() throws InterruptedException, IOException {
        List<String> availableLevels = subject.getStatus().getAvailableLevels();
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

    private static String vmLogList(String... outputConfigurations) {
        return "Available log levels: off, trace, debug, info, warning, error\n"
                + "Log output configuration:\n"
                + String.join("\n", outputConfigurations);
    }

    private static final class StaticJcmdExecutor extends JcmdExecutor {
        private final String output;

        private StaticJcmdExecutor(String output) {
            this.output = output;
        }

        @Override
        public ProcessResult execute(String... command) {
            return new ProcessResult(0, output);
        }
    }
}
