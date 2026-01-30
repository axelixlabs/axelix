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
package com.axelixlabs.axelix.sbs.spring.transactions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link TransactionMonitoringEndpoint}
 *
 * TODO: Revisit design of this test.
 *
 * @since 26.01.2026
 * @author Nikita Kirillov
 */
@Disabled
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"management.endpoints.web.exposure.include=axelix-transactions-monitoring"})
class TransactionMonitoringEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PropagationTestHelper propagationTestHelper;

    @Autowired
    private TransactionStatsCollector transactionStatsCollector;

    @BeforeEach
    void cleanUp() {
        transactionStatsCollector.clearAllStats();
    }

    @Test
    void shouldReturnsStatsAfterTransactionExecution() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        propagationTestHelper.testRequiresNew("Smith");

        ResponseEntity<String> response =
                restTemplate.getForEntity("/actuator/axelix-transactions-monitoring", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        String responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThatJson(responseBody).isObject().containsKey("entrypoints");
        assertThatJson(responseBody).node("entrypoints").isArray();

        JsonNode json = objectMapper.readTree(responseBody);
        assertThat(json.get("entrypoints").size()).isGreaterThan(0);

        assertThatJson(responseBody).node("entrypoints[0]").isObject();
        assertThatJson(responseBody).node("entrypoints[0].className").isEqualTo(PropagationTestHelper.class.getName());
        assertThatJson(responseBody).node("entrypoints[0].methodName").isEqualTo("testRequiresNew");

        assertThatJson(responseBody).node("entrypoints[0].executions").isArray();
        assertThatJson(responseBody).node("entrypoints[0].executions[0]").isObject();
        assertThatJson(responseBody)
                .node("entrypoints[0].executions[0].durationsMs")
                .isNumber();
        assertThatJson(responseBody)
                .node("entrypoints[0].executions[0].timestamp")
                .isNumber();

        assertThatJson(responseBody).node("entrypoints[0].executionStats").isObject();
        assertThatJson(responseBody)
                .node("entrypoints[0].executionStats.averageDurationMs")
                .isNumber();
        assertThatJson(responseBody)
                .node("entrypoints[0].executionStats.maxDurationMs")
                .isNumber();
        assertThatJson(responseBody)
                .node("entrypoints[0].executionStats.medianDurationMs")
                .isNumber();
    }

    @Test
    void shouldClearsAllTransactionMonitoringStats() {
        for (int i = 0; i < 3; i++) {
            propagationTestHelper.testRequiresNew("Johnson");
        }

        var allStats = transactionStatsCollector.getAllStats();

        assertThat(allStats.size()).isGreaterThan(0);

        ResponseEntity<Void> deleteResponse =
                restTemplate.exchange("/actuator/axelix-transactions-monitoring", HttpMethod.DELETE, null, Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        allStats = transactionStatsCollector.getAllStats();
        assertThat(allStats).isEmpty();
    }

    @TestConfiguration
    @Import(TransactionMonitoringServiceTest.TransactionMonitoringIntegrationTestConfiguration.class)
    static class TransactionMonitoringEndpointTestConfiguration {

        @Bean
        public TransactionMonitoringEndpoint transactionMonitoringEndpoint(
                TransactionMonitoringService transactionMonitoringService) {
            return new TransactionMonitoringEndpoint(
                    (DefaultTransactionMonitoringService) transactionMonitoringService);
        }
    }
}
