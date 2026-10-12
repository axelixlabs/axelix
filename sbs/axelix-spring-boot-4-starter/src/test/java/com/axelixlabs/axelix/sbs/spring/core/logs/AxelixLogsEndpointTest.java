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
package com.axelixlabs.axelix.sbs.spring.core.logs;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.sbs.spring.core.IgnoreTestContextArchitecture;
import com.axelixlabs.axelix.sbs.spring.core.SlidingWindow;
import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.contract.logs.LogEntriesTail;
import com.axelixlabs.axelix.sbs.spring.core.contract.logs.LogEntry;
import com.axelixlabs.axelix.sbs.spring.core.logs.AxelixLogsEndpointTest.AxelixLogsEndpointTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.sbs.spring.core.utils.auth.ProtectedEndpointTests;

import static com.axelixlabs.axelix.sbs.spring.core.IgnoreTestContextArchitecture.NO_SIBLINGS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixLogsEndpoint}.
 *
 * @author Mikhail Polivakha
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({AxelixLogsEndpointTestConfiguration.class, JwtAuthTestConfiguration.class})
@IgnoreTestContextArchitecture(reason = NO_SIBLINGS)
class AxelixLogsEndpointTest {

    private static final String LOGGER = "axelix.logs.test";

    private static final Logger logger = LoggerFactory.getLogger(LOGGER);

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @Test
    void shouldReturnLogEntriesInChronologicalOrder() {
        // given.
        logger.info("first {}", "entry");
        logger.error("second entry", new IllegalStateException("something went wrong"));

        // when.
        ResponseEntity<LogEntriesTail> response =
                restTemplate.asViewer().getForEntity("/actuator/axelix-logs?limit=1000", LogEntriesTail.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // other loggers of the application write into the very same buffer concurrently
        List<LogEntry> entries = response.getBody().getEntries().stream()
                .filter(entry -> LOGGER.equals(entry.getLogger()))
                .collect(Collectors.toList());

        assertThat(entries).hasSize(2);

        assertThat(entries.get(0).getMessage()).isEqualTo("first entry");
        assertThat(entries.get(0).getLevel()).isEqualTo("INFO");
        assertThat(entries.get(0).getThread()).isEqualTo(Thread.currentThread().getName());
        assertThat(entries.get(0).getTimestamp()).isPositive();
        assertThat(entries.get(0).getStackTrace()).isNull();

        assertThat(entries.get(1).getMessage()).isEqualTo("second entry");
        assertThat(entries.get(1).getLevel()).isEqualTo("ERROR");
        assertThat(entries.get(1).getStackTrace())
                .contains(IllegalStateException.class.getName(), "something went wrong");
    }

    @Test
    void shouldReturnNoMoreEntriesThanRequested() {
        // given.
        Logger anotherLogger = LoggerFactory.getLogger("axelix.logs.test.another");
        anotherLogger.info("one");
        anotherLogger.info("two");
        anotherLogger.info("three");

        // when.
        ResponseEntity<LogEntriesTail> response =
                restTemplate.asViewer().getForEntity("/actuator/axelix-logs?limit=2", LogEntriesTail.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEntries()).hasSize(2);
    }

    @Test
    void shouldReturnBadRequest_WhenLimitIsMissing() {
        // when.
        ResponseEntity<String> response = restTemplate.asViewer().getForEntity("/actuator/axelix-logs", String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @ProtectedEndpointTests(method = HttpMethod.GET, path = "/actuator/axelix-logs?limit=1")
    void negativeAuthTests() {}

    @TestConfiguration
    static class AxelixLogsEndpointTestConfiguration {

        @Bean
        public SlidingWindow<LogEntry> logEntries() {
            SlidingWindow<LogEntry> logEntries = new SlidingWindow<>(1000);
            LogbackInMemoryLogsAppender.register(logEntries);
            return logEntries;
        }

        @Bean
        public AxelixLogsEndpoint axelixLogsEndpoint(SlidingWindow<LogEntry> logEntries) {
            return new AxelixLogsEndpoint(logEntries);
        }
    }
}
