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
package com.axelixlabs.axelix.sbs.spring.core.loggers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggerGroups;
import org.springframework.boot.logging.LoggingSystem;

import com.axelixlabs.axelix.common.api.loggers.LogLevelChangeRequest;
import com.axelixlabs.axelix.common.api.loggers.SingleLoggerProfile;
import com.axelixlabs.axelix.sbs.spring.core.loggers.exceptions.LoggerNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DefaultLoggersService}.
 *
 * @author Mikhail Polivakha
 */
class DefaultLoggersServiceTest {

    private static final String LOGGER_NAME = "com.axelixlabs.axelix.loggers.test";

    private LoggingSystem loggingSystem;
    private DefaultLoggersService subject;

    @BeforeEach
    void setUp() {
        loggingSystem = LoggingSystem.get(getClass().getClassLoader());
        LoggerFactory.getLogger(LOGGER_NAME);
        loggingSystem.setLogLevel(LOGGER_NAME, LogLevel.WARN);
        subject = new DefaultLoggersService(loggingSystem, new LoggerGroups());
    }

    @AfterEach
    void tearDown() throws LoggerNotFoundException {
        try {
            subject.resetLogLevelByLoggerName(LOGGER_NAME);
        } catch (LoggerNotFoundException ignored) {
            loggingSystem.setLogLevel(LOGGER_NAME, null);
        }
    }

    @Test
    void shouldClearOverrideMetadataWhenResetAfterTemporaryChange() throws Exception {
        // given.
        subject.changeLogLevelByLoggerName(LOGGER_NAME, new LogLevelChangeRequest("DEBUG", 30L));

        SingleLoggerProfile beforeReset = subject.getSingleLogger(LOGGER_NAME);
        assertThat(beforeReset.getTemporaryLevelInitiatedAt()).isNotNull();
        assertThat(beforeReset.getTemporaryLevelRollsBackAt()).isNotNull();
        assertThat(beforeReset.getFallbackLevel()).isNotNull();

        // when.
        subject.resetLogLevelByLoggerName(LOGGER_NAME);

        // then.
        SingleLoggerProfile afterReset = subject.getSingleLogger(LOGGER_NAME);
        assertThat(afterReset.getTemporaryLevelInitiatedAt()).isNull();
        assertThat(afterReset.getTemporaryLevelRollsBackAt()).isNull();
        assertThat(afterReset.getFallbackLevel()).isNull();
        assertThat(afterReset.getConfiguredLevel()).isEqualTo("WARN");
    }

    @Test
    void shouldExtendTemporaryOverrideWhenNewChangeHasLongerDuration() throws Exception {
        // given.
        subject.changeLogLevelByLoggerName(LOGGER_NAME, new LogLevelChangeRequest("DEBUG", 30L));

        SingleLoggerProfile firstOverride = subject.getSingleLogger(LOGGER_NAME);
        Instant firstInitiatedAt = Instant.parse(firstOverride.getTemporaryLevelInitiatedAt());
        Instant firstRollsBackAt = Instant.parse(firstOverride.getTemporaryLevelRollsBackAt());

        // when.
        Instant beforeSecondChange = Instant.now();
        subject.changeLogLevelByLoggerName(LOGGER_NAME, new LogLevelChangeRequest("TRACE", 60L));
        Instant afterSecondChange = Instant.now();

        // then.
        SingleLoggerProfile secondOverride = subject.getSingleLogger(LOGGER_NAME);
        Instant secondInitiatedAt = Instant.parse(secondOverride.getTemporaryLevelInitiatedAt());
        Instant secondRollsBackAt = Instant.parse(secondOverride.getTemporaryLevelRollsBackAt());

        assertThat(secondInitiatedAt).isAfter(firstInitiatedAt);
        assertThat(secondRollsBackAt).isAfter(firstRollsBackAt);
        assertThat(secondInitiatedAt).isBetween(beforeSecondChange, afterSecondChange);
        assertThat(secondRollsBackAt)
                .isBetween(
                        beforeSecondChange.plus(60, ChronoUnit.SECONDS),
                        afterSecondChange.plus(60, ChronoUnit.SECONDS).plusSeconds(5));
        assertThat(secondOverride.getFallbackLevel()).isEqualTo("WARN");
        assertThat(secondOverride.getConfiguredLevel()).isEqualTo("TRACE");
    }

    @Test
    void shouldConvertTemporaryOverrideToPermanentWhenNewChangeHasNoDuration() throws Exception {
        // given.
        subject.changeLogLevelByLoggerName(LOGGER_NAME, new LogLevelChangeRequest("DEBUG", 30L));

        SingleLoggerProfile temporaryOverride = subject.getSingleLogger(LOGGER_NAME);
        Instant temporaryInitiatedAt = Instant.parse(temporaryOverride.getTemporaryLevelInitiatedAt());
        assertThat(temporaryOverride.getTemporaryLevelRollsBackAt()).isNotNull();

        // when.
        Instant beforePermanentChange = Instant.now();
        subject.changeLogLevelByLoggerName(LOGGER_NAME, new LogLevelChangeRequest("TRACE", null));
        Instant afterPermanentChange = Instant.now();

        // then.
        SingleLoggerProfile permanentOverride = subject.getSingleLogger(LOGGER_NAME);
        Instant permanentInitiatedAt = Instant.parse(permanentOverride.getTemporaryLevelInitiatedAt());

        assertThat(permanentOverride.getTemporaryLevelRollsBackAt()).isNull();
        assertThat(permanentInitiatedAt).isAfter(temporaryInitiatedAt);
        assertThat(permanentInitiatedAt).isBetween(beforePermanentChange, afterPermanentChange);
        assertThat(permanentOverride.getFallbackLevel()).isEqualTo("WARN");
        assertThat(permanentOverride.getConfiguredLevel()).isEqualTo("TRACE");
    }
}
