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
import java.util.stream.Stream;

import com.axelixlabs.axelix.sbs.spring.core.IgnoreArchitectureTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggerGroups;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.common.api.loggers.SingleLoggerProfile;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.loggers.AxelixLoggersEndpointTest.AxelixLoggersEndpointTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.sbs.spring.core.utils.auth.ProtectedEndpointTests;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixLoggersEndpoint}.
 *
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {
            "logging.group.axelix.logger.group=axelix.logger.test",
            "logging.level.axelix.logger.test=WARN",
            "logging.level.a.b=WARN",
            "logging.level.a.b.c.d.e=DEBUG"
        })
@Import({AxelixLoggersEndpointTestConfiguration.class, JwtAuthTestConfiguration.class})
@IgnoreArchitectureTest
public class AxelixLoggersEndpointTest {

    @Autowired
    private TestRestTemplateBuilder testRestTemplate;

    @Autowired
    private LoggingSystem loggingSystem;

    @Autowired
    private LoggerGroups loggerGroups;

    private static final String LOGGER = "axelix.logger.test";
    private static final String GROUP = "axelix.logger.group";
    private static final String AB_RESET_LOGGER = "a.b";
    private static final String ABC_RESET_LOGGER = "a.b.c";
    private static final String ABCD_RESET_LOGGER = "a.b.c.d";
    private static final String ABCDE_RESET_LOGGER = "a.b.c.d.e";

    private static final Logger abc_reset_logger = LoggerFactory.getLogger(ABC_RESET_LOGGER);
    private static final Logger abcd_reset_logger = LoggerFactory.getLogger(ABCD_RESET_LOGGER);

    @BeforeEach
    void resetLogLevels() {
        loggingSystem.setLogLevel(LOGGER, LogLevel.WARN);
        loggingSystem.setLogLevel(AB_RESET_LOGGER, LogLevel.WARN);
        loggingSystem.setLogLevel(ABC_RESET_LOGGER, null);
        loggingSystem.setLogLevel(ABCD_RESET_LOGGER, null);
    }

    @Test
    void shouldReturnAllLoggers() {
        // when.
        ResponseEntity<String> response =
                testRestTemplate.asViewer().getForEntity("/actuator/axelix-loggers", String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidLoggerAndGroupPaths")
    void shouldReturnBadRequest_WhenLoggerOrGroupNotFound(String path) {
        // when.
        ResponseEntity<String> response = testRestTemplate.asEditor().getForEntity(path, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldSetLoggerLevelWithoutTtl() {
        // language=json
        String request = """
        {
          "configuredLevel" : "debug"
        }
        """;

        // when.
        ResponseEntity<Void> response = testRestTemplate
                .asAdmin()
                .postForEntity(
                        "/actuator/axelix-loggers/logger/%s/change-level".formatted(LOGGER),
                        defaultJsonEntity(request),
                        Void.class);

        // then.
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(getLogLevel(LOGGER)).isEqualTo(LogLevel.DEBUG);
    }

    @Test
    void shouldSetGroupLevelHappyPath() {
        // language=json
        String request = """
        {
          "configuredLevel" : "debug"
        }
        """;

        // when.
        ResponseEntity<Void> response = testRestTemplate
                .asViewer()
                .postForEntity(
                        "/actuator/axelix-loggers/group/%s/change-level".formatted(GROUP),
                        defaultJsonEntity(request),
                        Void.class);

        // then.
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(getGroupLevel(GROUP)).isEqualTo(LogLevel.DEBUG);
    }

    @ParameterizedTest
    @MethodSource("nonExistingLoggerChangeLevel")
    void shouldReturnBadRequest_SetLogLevel(String path) {
        // language=json
        String request = """
        {
          "configuredLevel" : "debug"
        }
        """;

        // when.
        ResponseEntity<Void> response =
                testRestTemplate.asViewer().postForEntity(path, defaultJsonEntity(request), Void.class);

        // then.
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldResetLogLevel_WhenLogLevelAffectsOtherLoggers() {
        // language=json
        String request = """
        {
          "configuredLevel" : "error"
        }
        """;

        testRestTemplate
                .asViewer()
                .postForEntity(
                        "/actuator/axelix-loggers/logger/%s/change-level".formatted(ABC_RESET_LOGGER),
                        defaultJsonEntity(request),
                        Void.class);
        assertThat(getLogLevel(ABC_RESET_LOGGER)).isEqualTo(LogLevel.ERROR);
        assertThat(getLogLevel(ABCD_RESET_LOGGER)).isEqualTo(LogLevel.ERROR);

        // when.
        testRestTemplate
                .asViewer()
                .postForEntity(
                        "/actuator/axelix-loggers/logger/%s/reset".formatted(ABC_RESET_LOGGER), null, Void.class);

        // then.
        assertThat(getLogLevel(ABC_RESET_LOGGER)).isEqualTo(LogLevel.WARN);
        assertThat(getLogLevel(ABCD_RESET_LOGGER)).isEqualTo(LogLevel.WARN);
    }

    @Test
    void shouldResetLogLevel_WhenLogLevelDoesNotAffectOtherLoggers() {
        // language=json
        String request = """
        {
          "configuredLevel":"error"
        }
        """;

        testRestTemplate
                .asViewer()
                .postForEntity(
                        "/actuator/axelix-loggers/logger/%s/change-level".formatted(AB_RESET_LOGGER),
                        defaultJsonEntity(request),
                        Void.class);
        assertThat(getLogLevel(AB_RESET_LOGGER)).isEqualTo(LogLevel.ERROR);
        assertThat(getLogLevel(ABCDE_RESET_LOGGER)).isEqualTo(LogLevel.DEBUG);

        // when.
        testRestTemplate
                .asViewer()
                .postForEntity("/actuator/axelix-loggers/logger/%s/reset".formatted(AB_RESET_LOGGER), null, Void.class);

        // then.
        assertThat(getLogLevel(AB_RESET_LOGGER)).isEqualTo(LogLevel.WARN);
        assertThat(getLogLevel(ABCDE_RESET_LOGGER)).isEqualTo(LogLevel.DEBUG);
    }

    @Test
    void shouldReturnBadRequest_WhenResettingUnknownLogger() {
        ResponseEntity<Void> response = testRestTemplate
                .asViewer()
                .postForEntity("/actuator/axelix-loggers/logger/non.existent.logger/reset", null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // Temporary override log level
    @Test
    void shouldContainOverrideInfo_WhenTemporaryLevelIsSet() {
        // language=json
        String request = """
            {
              "configuredLevel": "debug",
              "ttlSeconds": 30
            }
        """;

        testRestTemplate
                .asAdmin()
                .postForEntity(
                        "/actuator/axelix-loggers/logger/%s/change-level".formatted(LOGGER),
                        defaultJsonEntity(request),
                        Void.class);

        // when.
        ResponseEntity<SingleLoggerProfile> response = testRestTemplate
                .asAdmin()
                .getForEntity("/actuator/axelix-loggers/logger/%s".formatted(LOGGER), SingleLoggerProfile.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        SingleLoggerProfile loggerProfile = response.getBody();
        assertThat(loggerProfile).isNotNull();
        assertThat(loggerProfile.getTemporaryLevelInitiatedAt()).isNotNull();
        assertThat(loggerProfile.getTemporaryLevelRollsBackAt()).isNotNull();
        assertThat(loggerProfile.getFallbackLevel()).isNotNull();
    }

    @Test
    void shouldNotContainOverrideInfo_WhenPermanentLevelIsSet() {
        // language=json
        String request = """
             {
               "configuredLevel": "debug"
             }
             """;

        testRestTemplate
                .asAdmin()
                .postForEntity(
                        "/actuator/axelix-loggers/logger/%s/change-level".formatted(LOGGER),
                        defaultJsonEntity(request),
                        Void.class);

        // when.
        ResponseEntity<SingleLoggerProfile> response = testRestTemplate
                .asAdmin()
                .getForEntity("/actuator/axelix-loggers/logger/%s".formatted(LOGGER), SingleLoggerProfile.class);

        // then.
        SingleLoggerProfile loggerProfile = response.getBody();
        assertThat(loggerProfile).isNotNull();
        assertThat(loggerProfile.getTemporaryLevelInitiatedAt()).isNotNull();
        assertThat(loggerProfile.getTemporaryLevelRollsBackAt()).isNull();
        assertThat(loggerProfile.getFallbackLevel()).isNotNull();
    }

    @Test
    void shouldCancelOverride_WhenPermanentChangeAppliedAfterTemporary() {
        // language=json
        String temporaryRequest = """
            {
              "configuredLevel": "debug",
              "ttlSeconds": 30
            }
            """;
        // language=json
        String permanentRequest = """
            {
              "configuredLevel": "trace"
            }
            """;

        testRestTemplate
                .asAdmin()
                .postForEntity(
                        "/actuator/axelix-loggers/logger/%s/change-level".formatted(LOGGER),
                        defaultJsonEntity(temporaryRequest),
                        Void.class);

        assertThat(getLogLevel(LOGGER)).isEqualTo(LogLevel.DEBUG);

        // when. — permanent over temporary
        testRestTemplate
                .asAdmin()
                .postForEntity(
                        "/actuator/axelix-loggers/logger/%s/change-level".formatted(LOGGER),
                        defaultJsonEntity(permanentRequest),
                        Void.class);

        // then. — override should disappear
        ResponseEntity<SingleLoggerProfile> response = testRestTemplate
                .asAdmin()
                .getForEntity("/actuator/axelix-loggers/logger/%s".formatted(LOGGER), SingleLoggerProfile.class);

        SingleLoggerProfile loggerProfile = response.getBody();
        assertThat(loggerProfile).isNotNull();
        assertThat(getLogLevel(LOGGER)).isEqualTo(LogLevel.TRACE);
        assertThat(loggerProfile.getTemporaryLevelInitiatedAt()).isNotNull();
        assertThat(loggerProfile.getTemporaryLevelRollsBackAt()).isNull();
    }

    @Test
    void shouldClearOverride_WhenResetCalled() {
        // language=json
        String request = """
    {
      "configuredLevel": "debug",
      "ttlSeconds": 30
    }
    """;

        testRestTemplate
                .asAdmin()
                .postForEntity(
                        "/actuator/axelix-loggers/logger/%s/change-level".formatted(LOGGER),
                        defaultJsonEntity(request),
                        Void.class);

        assertThat(getLogLevel(LOGGER)).isEqualTo(LogLevel.DEBUG);

        // when.
        testRestTemplate
                .asAdmin()
                .postForEntity("/actuator/axelix-loggers/logger/%s/reset".formatted(LOGGER), null, Void.class);

        // then.
        ResponseEntity<SingleLoggerProfile> response = testRestTemplate
                .asAdmin()
                .getForEntity("/actuator/axelix-loggers/logger/%s".formatted(LOGGER), SingleLoggerProfile.class);

        SingleLoggerProfile loggerProfile = response.getBody();
        assertThat(loggerProfile).isNotNull();
        assertThat(getLogLevel(LOGGER)).isEqualTo(LogLevel.WARN);
        assertThat(loggerProfile.getTemporaryLevelInitiatedAt()).isNull();
        assertThat(loggerProfile.getTemporaryLevelRollsBackAt()).isNull();
    }

    @Test
    void shouldReplaceActiveOverride_WhenNewTemporaryRequestComes() {
        // language=json
        String firstRequest = """
            {
              "configuredLevel": "debug",
              "ttlSeconds": 30
            }
            """;
        // language=json
        String secondRequest = """
            {
              "configuredLevel": "trace",
              "ttlSeconds": 60
            }
            """;

        testRestTemplate
                .asAdmin()
                .postForEntity(
                        "/actuator/axelix-loggers/logger/%s/change-level".formatted(LOGGER),
                        defaultJsonEntity(firstRequest),
                        Void.class);

        assertThat(getLogLevel(LOGGER)).isEqualTo(LogLevel.DEBUG);

        Instant beforeSecondRequest = Instant.now();

        // when.
        testRestTemplate
                .asAdmin()
                .postForEntity(
                        "/actuator/axelix-loggers/logger/%s/change-level".formatted(LOGGER),
                        defaultJsonEntity(secondRequest),
                        Void.class);

        Instant afterSecondRequest = Instant.now();

        // then.
        ResponseEntity<SingleLoggerProfile> response = testRestTemplate
                .asAdmin()
                .getForEntity("/actuator/axelix-loggers/logger/%s".formatted(LOGGER), SingleLoggerProfile.class);

        SingleLoggerProfile loggerProfile = response.getBody();
        assertThat(loggerProfile).isNotNull();
        assertThat(getLogLevel(LOGGER)).isEqualTo(LogLevel.TRACE);

        assertThat(loggerProfile.getTemporaryLevelInitiatedAt()).isNotNull();
        assertThat(loggerProfile.getTemporaryLevelRollsBackAt()).isNotNull();
        Instant appliedAt = Instant.parse(loggerProfile.getTemporaryLevelInitiatedAt());
        Instant expiresAt = Instant.parse(loggerProfile.getTemporaryLevelRollsBackAt());

        // appliedAt must be at the time of the second request
        assertThat(appliedAt).isBetween(beforeSecondRequest, afterSecondRequest);

        // expiresAt should be approximately 60 seconds (±10 sec for margin of error)
        assertThat(expiresAt)
                .isBetween(
                        beforeSecondRequest.plus(60, ChronoUnit.SECONDS),
                        afterSecondRequest.plus(60, ChronoUnit.SECONDS).plusSeconds(10));
    }

    @ProtectedEndpointTests(method = HttpMethod.GET, path = "/actuator/axelix-loggers")
    void negativeAuthTests() {}

    private LogLevel getLogLevel(String loggerName) {
        return loggingSystem.getLoggerConfiguration(loggerName).getEffectiveLevel();
    }

    private LogLevel getGroupLevel(String groupName) {
        return loggerGroups.get(groupName).getConfiguredLevel();
    }

    private static Stream<Arguments> nonExistingLoggerChangeLevel() {
        return Stream.of(
                Arguments.of("/actuator/axelix-loggers/group/non.existent.logger/change-level"),
                Arguments.of("/actuator/axelix-loggers/logger/non.existent.logger/change-level"));
    }

    private static Stream<Arguments> provideInvalidLoggerAndGroupPaths() {
        return Stream.of(
                Arguments.of("/actuator/axelix-loggers/logger/non.existent.logger"),
                Arguments.of("/actuator/axelix-loggers/group/non.existent.group"));
    }

    private <T> HttpEntity<T> defaultJsonEntity(T request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(request, headers);
    }

    @TestConfiguration
    static class AxelixLoggersEndpointTestConfiguration {

        @Bean
        public AxelixLoggersEndpoint axelixLoggersEndpoint(LoggersService loggersService) {
            return new AxelixLoggersEndpoint(loggersService);
        }

        @Bean
        public LoggersService loggersService(LoggingSystem loggingSystem, ObjectProvider<LoggerGroups> loggerGroups) {
            return new DefaultLoggersService(loggingSystem, loggerGroups.getIfAvailable(LoggerGroups::new));
        }
    }
}
