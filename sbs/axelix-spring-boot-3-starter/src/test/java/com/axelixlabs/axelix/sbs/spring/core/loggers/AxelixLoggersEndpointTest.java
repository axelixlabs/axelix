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

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
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

    @AfterEach
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
    @MethodSource("provideValidLoggerAndGroupPaths")
    void shouldReturnOk_WhenLoggerOrGroupFound(String path) {
        // when.
        ResponseEntity<String> response = testRestTemplate.asViewer().getForEntity(path, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private static Stream<Arguments> provideValidLoggerAndGroupPaths() {
        return Stream.of(
                Arguments.of("/actuator/axelix-loggers/logger/%s".formatted(LOGGER)),
                Arguments.of("/actuator/axelix-loggers/group/%s".formatted(GROUP)));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidLoggerAndGroupPaths")
    void shouldReturnBadRequest_WhenLoggerOrGroupNotFound(String path) {
        // when.
        ResponseEntity<String> response = testRestTemplate.asEditor().getForEntity(path, String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private static Stream<Arguments> provideInvalidLoggerAndGroupPaths() {
        return Stream.of(
                Arguments.of("/actuator/axelix-loggers/logger/non.existent.logger"),
                Arguments.of("/actuator/axelix-loggers/group/non.existent.group"));
    }

    @Test
    void shouldSetLoggerLevel() {
        // language=json
        String request = """
        {
          "configuredLevel":"debug"
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
    void shouldSetGroupLevel() {
        // language=json
        String request = """
        {
          "configuredLevel":"debug"
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
    @MethodSource("argSetLogLevel")
    void shouldReturnBadRequest_SetLogLevel(String path) {
        // language=json
        String request = """
        {
          "configuredLevel":"debug"
        }
        """;

        // when.
        ResponseEntity<Void> response =
                testRestTemplate.asViewer().postForEntity(path, defaultJsonEntity(request), Void.class);

        // then.
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private static Stream<Arguments> argSetLogLevel() {
        return Stream.of(
                Arguments.of("/actuator/axelix-loggers/group/non.existent.logger/change-level"),
                Arguments.of("/actuator/axelix-loggers/logger/non.existent.logger/change-level"));
    }

    @Test
    void shouldResetLogLevel_WhenLogLevelAffectsOtherLoggers() {
        // language=json
        String request = """
        {
          "configuredLevel":"error"
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

    @ProtectedEndpointTests(method = HttpMethod.GET, path = "/actuator/axelix-loggers")
    void negativeAuthTests() {}

    private LogLevel getLogLevel(String loggerName) {
        return loggingSystem.getLoggerConfiguration(loggerName).getEffectiveLevel();
    }

    private LogLevel getGroupLevel(String groupName) {
        return loggerGroups.get(groupName).getConfiguredLevel();
    }

    private <T> HttpEntity<T> defaultJsonEntity(T request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(request, headers);
    }

    @TestConfiguration
    static class AxelixLoggersEndpointTestConfiguration {

        @Bean
        public AxelixLoggersEndpoint axelixLoggersEndpoint(
                LoggingSystem loggingSystem, ObjectProvider<LoggerGroups> loggerGroups) {
            return new AxelixLoggersEndpoint(loggingSystem, loggerGroups.getIfAvailable(LoggerGroups::new));
        }
    }
}
