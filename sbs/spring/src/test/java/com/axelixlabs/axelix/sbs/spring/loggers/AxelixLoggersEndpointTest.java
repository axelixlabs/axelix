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
package com.axelixlabs.axelix.sbs.spring.loggers;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
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

    @Test
    void shouldReturnAllLoggers() {
        ResponseEntity<String> response = testRestTemplate.getForEntity("/actuator/axelix-loggers", String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(AxelixLoggersEndpointTest.class.getName());
    }

    @Test
    void shouldReturnSingleLogger() {
        String loggerName = AxelixLoggersEndpointTest.class.getName();

        ResponseEntity<String> response =
                testRestTemplate.getForEntity("/actuator/axelix-loggers/" + loggerName, String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("effectiveLevel");
    }

    @Test
    void shouldReturnSetLoggerLevel() {
        // language=json
        String request = """
        {
          "configuredLevel":"debug"
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(request, headers);
        String loggerName = AxelixLoggersEndpointTest.class.getName();

        ResponseEntity<String> response =
                testRestTemplate.postForEntity("/actuator/axelix-loggers/" + loggerName, entity, String.class);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @TestConfiguration
    static class AxelixLoggersEndpointTestConfiguration {

        @Bean
        public AxelixLoggersEndpoint axelixLoggersEndpoint(
                LoggingSystem loggingSystem, ObjectProvider<LoggerGroups> loggerGroups) {
            return new AxelixLoggersEndpoint(
                    new LoggersEndpoint(loggingSystem, loggerGroups.getIfAvailable(LoggerGroups::new)));
        }
    }
}
