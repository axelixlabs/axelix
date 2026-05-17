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
package com.axelixlabs.axelix.sbs.spring.core.transactions;

import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionTemplate;

import com.axelixlabs.axelix.sbs.spring.core.shared.AbstractEndpointTest;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test for {@link TransactionMonitoringEndpoint}
 *
 * @since 26.01.2026
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
class TransactionMonitoringEndpointTest extends AbstractEndpointTest {

    private static final String PROPAGATION_HELPER_FQN = PropagationTestHelper.class.getName();

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PropagationTestHelper propagationTestHelper;

    @Autowired
    private TransactionStatsCollector transactionStatsCollector;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void cleanUp() {
        transactionStatsCollector.clearAllStats();
    }

    @Test
    void shouldReturnsStatsAfterTransactionExecution() {
        propagationTestHelper.saveRequiresNew("Smith");

        String responseBody = getMonitoringResponse();

        assertThatJson(responseBody)
                .isEqualTo(
                        // language=json
                        "{\n" + "  \"entrypoints\" : [ {\n"
                                + "    \"className\" : \"" + PROPAGATION_HELPER_FQN + "\",\n"
                                + "    \"methodName\" : \"saveRequiresNew\",\n"
                                + "    \"executions\" : [ {\n"
                                + "      \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"endTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"queries\" : [ {\n"
                                + "        \"sql\" : \"insert into owner (id, last_name) values (default, ?)\",\n"
                                + "        \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "        \"endTimestampMs\" : \"#{json-unit.ignore}\"\n"
                                + "      } ]\n"
                                + "    } ],\n"
                                + "    \"executionStats\" : {\n"
                                + "      \"averageDurationMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"maxDurationMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"medianDurationMs\" : \"#{json-unit.ignore}\"\n"
                                + "    }\n"
                                + "  } ]\n"
                                + "}");

        Long executionStartTimestampMs = JsonPath.read(responseBody, "$.entrypoints[0].executions[0].startTimestampMs");
        Long executionEndTimestampMs = JsonPath.read(responseBody, "$.entrypoints[0].executions[0].endTimestampMs");
        Long queryStartTimestampMs =
                JsonPath.read(responseBody, "$.entrypoints[0].executions[0].queries[0].startTimestampMs");
        Long queryEndTimestampMs =
                JsonPath.read(responseBody, "$.entrypoints[0].executions[0].queries[0].endTimestampMs");

        assertThat(executionStartTimestampMs).isLessThanOrEqualTo(executionEndTimestampMs);
        assertThat(queryStartTimestampMs).isLessThanOrEqualTo(queryEndTimestampMs);
        assertThat(queryStartTimestampMs).isBetween(executionStartTimestampMs, executionEndTimestampMs);
        assertThat(queryEndTimestampMs).isBetween(executionStartTimestampMs, executionEndTimestampMs);

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
            propagationTestHelper.saveRequiresNew("Johnson");
        }

        Map<?, ?> allStats = transactionStatsCollector.getAllStats();

        assertThat(allStats.size()).isGreaterThan(0);

        ResponseEntity<Void> deleteResponse =
                restTemplate.exchange("/actuator/axelix-transactions-monitoring", HttpMethod.DELETE, null, Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        allStats = transactionStatsCollector.getAllStats();
        assertThat(allStats).isEmpty();
    }

    @Test
    void shouldTrackMultipleQueriesInsideTransaction() {
        propagationTestHelper.saveMultipleOwners();

        String responseBody = getMonitoringResponse();

        assertThatJson(responseBody)
                .isEqualTo(
                        // language=json
                        "{\n" + "  \"entrypoints\" : [ {\n"
                                + "    \"className\" : \"" + PROPAGATION_HELPER_FQN + "\",\n"
                                + "    \"methodName\" : \"saveMultipleOwners\",\n"
                                + "    \"executions\" : [ {\n"
                                + "      \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"endTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"queries\" : [ {\n"
                                + "        \"sql\" : \"insert into owner (id, last_name) values (default, ?)\",\n"
                                + "        \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "        \"endTimestampMs\" : \"#{json-unit.ignore}\"\n"
                                + "      }, {\n"
                                + "        \"sql\" : \"insert into owner (id, last_name) values (default, ?)\",\n"
                                + "        \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "        \"endTimestampMs\" : \"#{json-unit.ignore}\"\n"
                                + "      }, {\n"
                                + "        \"sql\" : \"insert into owner (id, last_name) values (default, ?)\",\n"
                                + "        \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "        \"endTimestampMs\" : \"#{json-unit.ignore}\"\n"
                                + "      } ]\n"
                                + "    } ],\n"
                                + "    \"executionStats\" : {\n"
                                + "      \"averageDurationMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"maxDurationMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"medianDurationMs\" : \"#{json-unit.ignore}\"\n"
                                + "    }\n"
                                + "  } ]\n"
                                + "}");

        Long executionStartTimestampMs = JsonPath.read(responseBody, "$.entrypoints[0].executions[0].startTimestampMs");
        Long executionEndTimestampMs = JsonPath.read(responseBody, "$.entrypoints[0].executions[0].endTimestampMs");
        List<Map<String, Number>> queries = JsonPath.read(responseBody, "$.entrypoints[0].executions[0].queries");

        assertThat(executionStartTimestampMs).isLessThanOrEqualTo(executionEndTimestampMs);
        assertThat(queries).isNotEmpty();

        for (Map<String, Number> query : queries) {
            Long queryStartTimestampMs = query.get("startTimestampMs").longValue();
            Long queryEndTimestampMs = query.get("endTimestampMs").longValue();

            assertThat(queryStartTimestampMs).isLessThanOrEqualTo(queryEndTimestampMs);
            assertThat(queryStartTimestampMs).isBetween(executionStartTimestampMs, executionEndTimestampMs);
            assertThat(queryEndTimestampMs).isBetween(executionStartTimestampMs, executionEndTimestampMs);
        }
    }

    @Test
    void shouldTrackNPlusOneInsideTransaction() {
        // given.
        Owner owner = new Owner();
        owner.addPet(new Pet("pet1", owner)).addPet(new Pet("pet2", owner)).addPet(new Pet("pet3", owner));

        Owner save = ownerRepository.save(owner);

        // when. (will cause N + 1)
        propagationTestHelper.findOwnerById(save.getId());

        // then.
        String responseBody = getMonitoringResponse();

        assertThatJson(responseBody)
                .isEqualTo(
                        // language=json
                        "{\n" + "  \"entrypoints\" : [ {\n"
                                + "    \"className\" : \"" + PROPAGATION_HELPER_FQN + "\",\n"
                                + "    \"methodName\" : \"findOwnerById\",\n"
                                + "    \"executions\" : [ {\n"
                                + "      \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"endTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"queries\" : [ {\n"
                                + "        \"sql\" : \"select owner0_.id as id1_1_0_, owner0_.last_name as last_nam2_1_0_ from owner owner0_ where owner0_.id=?\",\n"
                                + "        \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "        \"endTimestampMs\" : \"#{json-unit.ignore}\"\n"
                                + "      }, {\n"
                                + "        \"sql\" : \"select pets0_.owner_id as owner_id3_2_0_, pets0_.id as id1_2_0_, pets0_.id as id1_2_1_, pets0_.name as name2_2_1_, pets0_.owner_id as owner_id3_2_1_ from pet pets0_ where pets0_.owner_id=?\",\n"
                                + "        \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "        \"endTimestampMs\" : \"#{json-unit.ignore}\"\n"
                                + "      } ]\n"
                                + "    } ],\n"
                                + "    \"executionStats\" : {\n"
                                + "      \"averageDurationMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"maxDurationMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"medianDurationMs\" : \"#{json-unit.ignore}\"\n"
                                + "    }\n"
                                + "  } ]\n"
                                + "}");

        Long executionStartTimestampMs = JsonPath.read(responseBody, "$.entrypoints[0].executions[0].startTimestampMs");
        Long executionEndTimestampMs = JsonPath.read(responseBody, "$.entrypoints[0].executions[0].endTimestampMs");
        List<Map<String, Number>> queries = JsonPath.read(responseBody, "$.entrypoints[0].executions[0].queries");

        assertThat(executionStartTimestampMs).isLessThanOrEqualTo(executionEndTimestampMs);
        assertThat(queries).isNotEmpty();

        for (Map<String, Number> query : queries) {
            Long queryStartTimestampMs = query.get("startTimestampMs").longValue();
            Long queryEndTimestampMs = query.get("endTimestampMs").longValue();

            assertThat(queryStartTimestampMs).isLessThanOrEqualTo(queryEndTimestampMs);
            assertThat(queryStartTimestampMs).isBetween(executionStartTimestampMs, executionEndTimestampMs);
            assertThat(queryEndTimestampMs).isBetween(executionStartTimestampMs, executionEndTimestampMs);
        }
    }

    @Test
    void shouldTrackHibernateOnMerge() {
        // given.
        Owner owner = new Owner();
        Owner saved = ownerRepository.save(owner);
        saved.setLastName("newLastName");

        // when. (will cause entityManager.merge)
        propagationTestHelper.updateOwner(saved);

        String responseBody = getMonitoringResponse();

        assertThatJson(responseBody)
                .isEqualTo(
                        // language=json
                        "{\n" + "  \"entrypoints\" : [ {\n"
                                + "    \"className\" : \"" + PROPAGATION_HELPER_FQN + "\",\n"
                                + "    \"methodName\" : \"updateOwner\",\n"
                                + "    \"executions\" : [ {\n"
                                + "      \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"endTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"queries\" : [ {\n"
                                + "        \"sql\" : \"select owner0_.id as id1_1_0_, owner0_.last_name as last_nam2_1_0_ from owner owner0_ where owner0_.id=?\",\n"
                                + "        \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "        \"endTimestampMs\" : \"#{json-unit.ignore}\"\n"
                                + "      }, {\n"
                                + "        \"sql\" : \"update owner set last_name=? where id=?\",\n"
                                + "        \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "        \"endTimestampMs\" : \"#{json-unit.ignore}\"\n"
                                + "      } ]\n"
                                + "    } ],\n"
                                + "    \"executionStats\" : {\n"
                                + "      \"averageDurationMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"maxDurationMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"medianDurationMs\" : \"#{json-unit.ignore}\"\n"
                                + "    }\n"
                                + "  } ]\n"
                                + "}");

        Long executionStartTimestampMs = JsonPath.read(responseBody, "$.entrypoints[0].executions[0].startTimestampMs");
        Long executionEndTimestampMs = JsonPath.read(responseBody, "$.entrypoints[0].executions[0].endTimestampMs");
        List<Map<String, Number>> queries = JsonPath.read(responseBody, "$.entrypoints[0].executions[0].queries");

        assertThat(executionStartTimestampMs).isLessThanOrEqualTo(executionEndTimestampMs);
        assertThat(queries).isNotEmpty();

        for (Map<String, Number> query : queries) {
            Long queryStartTimestampMs = query.get("startTimestampMs").longValue();
            Long queryEndTimestampMs = query.get("endTimestampMs").longValue();

            assertThat(queryStartTimestampMs).isLessThanOrEqualTo(queryEndTimestampMs);
            assertThat(queryStartTimestampMs).isBetween(executionStartTimestampMs, executionEndTimestampMs);
            assertThat(queryEndTimestampMs).isBetween(executionStartTimestampMs, executionEndTimestampMs);
        }
    }

    @Test
    void rollbackScenarioIsMonitored() {
        // given.
        assertThatThrownBy(() -> propagationTestHelper.testRollbackScenario("Rodriquez"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Test rollback");

        // when.
        String responseBody = getMonitoringResponse();

        assertThatJson(responseBody)
                .isEqualTo(
                        // language=json
                        "{\n" + "  \"entrypoints\" : [ {\n"
                                + "    \"className\" : \"" + PROPAGATION_HELPER_FQN + "\",\n"
                                + "    \"methodName\" : \"testRollbackScenario\",\n"
                                + "    \"executions\" : [ {\n"
                                + "      \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"endTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"queries\" : [ {\n"
                                + "        \"sql\" : \"select owner0_.id as id1_1_, owner0_.last_name as last_nam2_1_ from owner owner0_ where owner0_.last_name=?\",\n"
                                + "        \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "        \"endTimestampMs\" : \"#{json-unit.ignore}\"\n"
                                + "      } ]\n"
                                + "    } ],\n"
                                + "    \"executionStats\" : {\n"
                                + "      \"averageDurationMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"maxDurationMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"medianDurationMs\" : \"#{json-unit.ignore}\"\n"
                                + "    }\n"
                                + "  } ]\n"
                                + "}");
    }

    @Test
    void notSupportedPropagationIsNotMonitored() {
        // given.
        propagationTestHelper.testNotSupported("Rodriquez");

        // when.
        String responseBody = getMonitoringResponse();
        List<String> methodNames = JsonPath.read(responseBody, "$.entrypoints[*].methodName");

        // then.
        assertThat(methodNames).doesNotContain("testNotSupported");
        assertThat(methodNames).contains("findByLastName"); // Spring Data repository opens transaction
    }

    @Test
    void supportsPropagationWithExistingTransaction() {
        // given.
        transactionTemplate.executeWithoutResult(status -> {
            propagationTestHelper.testSupports("Rodriquez");
        });

        // when.
        String responseBody = getMonitoringResponse();

        // then.
        assertThatJson(responseBody).node("entrypoints").isArray().isEmpty();
    }

    @Test
    void supportsWithoutTransactionIsNotMonitored() {
        // given.
        propagationTestHelper.testSupportsWithoutTransaction();

        // when.
        String responseBody = getMonitoringResponse();

        // then.
        assertThatJson(responseBody).node("entrypoints").isArray().isEmpty();
    }

    @Test
    void nestedPropagationIsMonitored() {
        // given.
        propagationTestHelper.testNested();

        // when.
        String responseBody = getMonitoringResponse();
        List<String> methodNames = JsonPath.read(responseBody, "$.entrypoints[*].methodName");

        // then.
        assertThat(methodNames).contains("testNested");
    }

    private String getMonitoringResponse() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/actuator/axelix-transactions-monitoring", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }
}
