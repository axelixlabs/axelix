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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggerGroups;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixLoggersEndpoint}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(AxelixLoggersEndpointTest.AxelixLoggersEndpointTestConfiguration.class)
public class AxelixLoggersEndpointTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private LoggingSystem loggingSystem;

    @Test
    void shouldReturnAllLoggers() {
        // when.
        ResponseEntity<String> response = testRestTemplate.getForEntity("/actuator/axelix-loggers", String.class);

        // then.
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(AxelixLoggersEndpointTest.class.getName());
    }

    @Test
    void shouldReturnSingleLogger() {
        String loggerName = AxelixLoggersEndpointTest.class.getName();

        // when.
        ResponseEntity<String> response =
                testRestTemplate.getForEntity("/actuator/axelix-loggers/" + loggerName, String.class);

        // then.
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("effectiveLevel");
    }

    @Test
    void shouldReturnSetLoggerLevel() {
        String loggerName = AxelixLoggersEndpointTest.class.getName();
        // language=json
        String request = """
        {
          "configuredLevel":"debug"
        }
        """;

        // when.
        ResponseEntity<String> response = testRestTemplate.postForEntity(
                "/actuator/axelix-loggers/" + loggerName, defaultJsonEntity(request), String.class);

        // then.
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldResetLogLevel_WhenLogLevelAffectsOtherLoggers() {
        loggingSystem.setLogLevel("a.b", LogLevel.WARN);
        loggingSystem.setLogLevel("a.b.c", null);
        loggingSystem.setLogLevel("a.b.c.d", null);

        // language=json
        String request = """
        {
          "configuredLevel":"error"
        }
        """;

        testRestTemplate.postForEntity("/actuator/axelix-loggers/" + "a.b.c", defaultJsonEntity(request), String.class);
        assertThat(getLogLevel("a.b.c")).isEqualTo(LogLevel.ERROR);
        assertThat(getLogLevel("a.b.c.d")).isEqualTo(LogLevel.ERROR);

        // when.
        testRestTemplate.postForEntity("/actuator/axelix-loggers/reset/" + "a.b.c", null, String.class);

        // then.
        assertThat(getLogLevel("a.b.c")).isEqualTo(LogLevel.WARN);
        assertThat(getLogLevel("a.b.c.d")).isEqualTo(LogLevel.WARN);
    }

    @Test
    void shouldResetLogLevel_WhenLogLevelDoesNotAffectOtherLoggers() {
        loggingSystem.setLogLevel("a.b", LogLevel.WARN);
        loggingSystem.setLogLevel("a.b.c", null); // Inherits WARN from parent
        loggingSystem.setLogLevel("a.b.c.d", LogLevel.DEBUG);

        // language=json
        String request = """
        {
          "configuredLevel":"error"
        }
        """;

        testRestTemplate.postForEntity("/actuator/axelix-loggers/" + "a.b.c", defaultJsonEntity(request), String.class);
        assertThat(getLogLevel("a.b.c")).isEqualTo(LogLevel.ERROR);
        assertThat(getLogLevel("a.b.c.d")).isEqualTo(LogLevel.DEBUG);

        // when.
        testRestTemplate.postForEntity("/actuator/axelix-loggers/reset/" + "a.b.c", null, String.class);

        // then.
        assertThat(getLogLevel("a.b.c")).isEqualTo(LogLevel.WARN);
        assertThat(getLogLevel("a.b.c.d")).isEqualTo(LogLevel.DEBUG);
    }

    private LogLevel getLogLevel(String loggerName) {
        return loggingSystem.getLoggerConfiguration(loggerName).getEffectiveLevel();
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
