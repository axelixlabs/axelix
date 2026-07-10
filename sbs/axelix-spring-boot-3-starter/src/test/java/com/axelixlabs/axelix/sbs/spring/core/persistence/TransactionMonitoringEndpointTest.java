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
package com.axelixlabs.axelix.sbs.spring.core.persistence;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.jayway.jsonpath.JsonPath;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.sbs.spring.core.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.sbs.spring.core.utils.auth.ProtectedEndpointTests;

import static com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricNames.TRANSACTION_DURATION;
import static com.axelixlabs.axelix.sbs.spring.core.metrics.AxelixMetricNames.TRANSACTION_QUERIES;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test for {@link TransactionMonitoringEndpoint}
 *
 * @since 26.01.2026
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 * @author Artemiy Degtyarev
 */
class TransactionMonitoringEndpointTest extends AbstractTransactionMonitoringSharedContextTest {

    private final String expectedClassName = PropagationTestHelper.class.getSimpleName();

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @Autowired
    private PropagationTestHelper propagationTestHelper;

    @Autowired
    private TransactionStatsCollector transactionStatsCollector;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MeterRegistry meterRegistry;

    @BeforeEach
    void cleanUp() {
        transactionStatsCollector.clearStats();
        meterRegistry.clear();
        petRepository.deleteAll();
        ownerRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void shouldReturnsStatsAfterTransactionExecution() {
        propagationTestHelper.saveRequiresNew("Smith");

        String responseBody = getMonitoringResponse();

        assertThatJson(responseBody)
                .isEqualTo(
                        // language=json
                        """
                {
                  "entrypoints" : [ {
                    "className" : "com.axelixlabs.axelix.sbs.spring.core.persistence.AbstractTransactionMonitoringSharedContextTest$PropagationTestHelper",
                    "methodName" : "saveRequiresNew",
                    "executions" : [ {
                      "startTimestampMs" : "#{json-unit.ignore}",
                      "endTimestampMs" : "#{json-unit.ignore}",
                      "queries" : [ {
                        "sql" : "insert into owner (id, last_name) values (default, ?)",
                        "startTimestampMs" : "#{json-unit.ignore}",
                        "endTimestampMs" : "#{json-unit.ignore}"
                      } ]
                    } ],
                    "executionStats" : {
                      "averageDurationMs" : "#{json-unit.ignore}",
                      "maxDurationMs" : "#{json-unit.ignore}",
                      "medianDurationMs" : "#{json-unit.ignore}"
                    }
                  } ]
                }""");

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

        // and then. Verify that metrics were successfully published to MeterRegistry
        checkMeterRegistry(expectedClassName, "saveRequiresNew", 1);
    }

    @Test
    void shouldTrackMultipleQueriesInsideTransaction() {
        propagationTestHelper.testSaveMultipleOwners();

        String responseBody = getMonitoringResponse();

        assertThatJson(responseBody)
                .isEqualTo(
                        // language=json
                        """
            {
              "entrypoints" : [ {
                "className" : "com.axelixlabs.axelix.sbs.spring.core.persistence.AbstractTransactionMonitoringSharedContextTest$PropagationTestHelper",
                "methodName" : "testSaveMultipleOwners",
                "executions" : [ {
                  "startTimestampMs" : "#{json-unit.ignore}",
                  "endTimestampMs" : "#{json-unit.ignore}",
                  "queries" : [ {
                    "sql" : "insert into owner (id, last_name) values (default, ?)",
                    "startTimestampMs" : "#{json-unit.ignore}",
                    "endTimestampMs" : "#{json-unit.ignore}"
                  }, {
                    "sql" : "insert into owner (id, last_name) values (default, ?)",
                    "startTimestampMs" : "#{json-unit.ignore}",
                    "endTimestampMs" : "#{json-unit.ignore}"
                  }, {
                    "sql" : "insert into owner (id, last_name) values (default, ?)",
                    "startTimestampMs" : "#{json-unit.ignore}",
                    "endTimestampMs" : "#{json-unit.ignore}"
                  } ]
                } ],
                "executionStats" : {
                  "averageDurationMs" : "#{json-unit.ignore}",
                  "maxDurationMs" : "#{json-unit.ignore}",
                  "medianDurationMs" : "#{json-unit.ignore}"
                }
              } ]
            }
            """);

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

        // and then. Verify that metrics were successfully published to MeterRegistry
        checkMeterRegistry(expectedClassName, "testSaveMultipleOwners", 3);
    }

    @Test
    void shouldTrackNPlusOneInsideTransaction() {
        // given.
        Owner owner = new Owner();
        owner.addPet(new Pet("pet1", owner)).addPet(new Pet("pet2", owner)).addPet(new Pet("pet3", owner));

        Owner save = ownerRepository.save(owner);

        // (will cause N + 1)
        propagationTestHelper.findOwnerById(save.getId());

        // when.
        String responseBody = getMonitoringResponse();

        // then.
        assertThatJson(responseBody)
                .isEqualTo(
                        // language=json
                        """
                {
                  "entrypoints" : [ {
                    "className" : "com.axelixlabs.axelix.sbs.spring.core.persistence.AbstractTransactionMonitoringSharedContextTest$PropagationTestHelper",
                    "methodName" : "findOwnerById",
                    "executions" : [ {
                      "startTimestampMs" : "#{json-unit.ignore}",
                      "endTimestampMs" : "#{json-unit.ignore}",
                      "queries" : [ {
                        "sql" : "select o1_0.id,o1_0.last_name from owner o1_0 where o1_0.id=?",
                        "startTimestampMs" : "#{json-unit.ignore}",
                        "endTimestampMs" : "#{json-unit.ignore}",
                        "lazyLoadingTarget" : {
                          "ownerEntityClass" : "com.axelixlabs.axelix.sbs.spring.core.persistence.AbstractTransactionMonitoringSharedContextTest$Owner",
                          "associationPropertyName" : "pets"
                        }
                      }, {
                        "sql" : "select p1_0.owner_id,p1_0.id,p1_0.category_id,p1_0.name from pet p1_0 where p1_0.owner_id=?",
                        "startTimestampMs" : "#{json-unit.ignore}",
                        "endTimestampMs" : "#{json-unit.ignore}"
                      } ]
                    } ],
                    "executionStats" : {
                      "averageDurationMs" : "#{json-unit.ignore}",
                      "maxDurationMs" : "#{json-unit.ignore}",
                      "medianDurationMs" : "#{json-unit.ignore}"
                    }
                  } ]
                }
                """);

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

        // and then. Verify that metrics were successfully published to MeterRegistry
        checkMeterRegistry(expectedClassName, "findOwnerById", 2);
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

        // then.
        assertThatJson(responseBody)
                .isEqualTo(
                        // language=json
                        """
            {
              "entrypoints" : [ {
                "className" : "com.axelixlabs.axelix.sbs.spring.core.persistence.AbstractTransactionMonitoringSharedContextTest$PropagationTestHelper",
                "methodName" : "updateOwner",
                "executions" : [ {
                  "startTimestampMs" : "#{json-unit.ignore}",
                  "endTimestampMs" : "#{json-unit.ignore}",
                  "queries" : [ {
                    "sql" : "select o1_0.id,o1_0.last_name,t1_0.owner_id,t1_0.id,t1_0.name from owner o1_0 left join tag t1_0 on o1_0.id=t1_0.owner_id where o1_0.id=?",
                    "startTimestampMs" : "#{json-unit.ignore}",
                    "endTimestampMs" : "#{json-unit.ignore}"
                  }, {
                    "sql" : "update owner set last_name=? where id=?",
                    "startTimestampMs" : "#{json-unit.ignore}",
                    "endTimestampMs" : "#{json-unit.ignore}"
                  } ]
                } ],
                "executionStats" : {
                  "averageDurationMs" : "#{json-unit.ignore}",
                  "maxDurationMs" : "#{json-unit.ignore}",
                  "medianDurationMs" : "#{json-unit.ignore}"
                }
              } ]
            }
            """);

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

        // and then. Verify that metrics were successfully published to MeterRegistry
        checkMeterRegistry(expectedClassName, "updateOwner", 2);
    }

    @Test
    void rollbackScenarioIsMonitored() {
        // given.
        assertThatThrownBy(() -> propagationTestHelper.testRollbackScenario("Rodriquez"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Test rollback");

        // when.
        String responseBody = getMonitoringResponse();

        // then.
        assertThatJson(responseBody)
                .isEqualTo(
                        // language=json
                        """
                {
                  "entrypoints" : [ {
                    "className" : "com.axelixlabs.axelix.sbs.spring.core.persistence.AbstractTransactionMonitoringSharedContextTest$PropagationTestHelper",
                    "methodName" : "testRollbackScenario",
                    "executions" : [ {
                      "startTimestampMs" : "#{json-unit.ignore}",
                      "endTimestampMs" : "#{json-unit.ignore}",
                      "queries" : [ {
                        "sql" : "select o1_0.id,o1_0.last_name from owner o1_0 where o1_0.last_name=?",
                        "startTimestampMs" : "#{json-unit.ignore}",
                        "endTimestampMs" : "#{json-unit.ignore}"
                      } ]
                    } ],
                    "executionStats" : {
                      "averageDurationMs" : "#{json-unit.ignore}",
                      "maxDurationMs" : "#{json-unit.ignore}",
                      "medianDurationMs" : "#{json-unit.ignore}"
                    }
                  } ]
                }
            """);

        // and then. Verify that metrics were successfully published to MeterRegistry
        checkMeterRegistry(expectedClassName, "testRollbackScenario", 1);
    }

    @Test
    void nestedRequiresNewPropagationIsMonitoredSeparately() {
        // given.
        propagationTestHelper.outerRequiredMethod("ParentOwner");

        // when.
        String responseBody = getMonitoringResponse();

        // then.
        assertThatJson(responseBody)
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(
                        // language=json
                        """
        {
          "entrypoints" : [
            {
              "className" : "com.axelixlabs.axelix.sbs.spring.core.persistence.AbstractTransactionMonitoringSharedContextTest$PropagationTestHelper",
              "methodName" : "saveRequiresNew",
              "executions" : [ {
                "startTimestampMs" : "#{json-unit.ignore}",
                "endTimestampMs" : "#{json-unit.ignore}",
                "queries" : [
                  {
                    "sql" : "insert into owner (id, last_name) values (default, ?)",
                    "startTimestampMs" : "#{json-unit.ignore}",
                    "endTimestampMs" : "#{json-unit.ignore}"
                  }
                ]
              } ],
              "executionStats" : {
                "averageDurationMs" : "#{json-unit.ignore}",
                "maxDurationMs" : "#{json-unit.ignore}",
                "medianDurationMs" : "#{json-unit.ignore}"
              }
            },
            {
              "className" : "com.axelixlabs.axelix.sbs.spring.core.persistence.AbstractTransactionMonitoringSharedContextTest$PropagationTestHelper",
              "methodName" : "outerRequiredMethod",
              "executions" : [ {
                "startTimestampMs" : "#{json-unit.ignore}",
                "endTimestampMs" : "#{json-unit.ignore}",
                "queries" : [
                  {
                    "sql" : "insert into owner (id, last_name) values (default, ?)",
                    "startTimestampMs" : "#{json-unit.ignore}",
                    "endTimestampMs" : "#{json-unit.ignore}"
                  },
                  {
                    "sql" : "insert into owner (id, last_name) values (default, ?)",
                    "startTimestampMs" : "#{json-unit.ignore}",
                    "endTimestampMs" : "#{json-unit.ignore}"
                  }
                ]
              } ],
              "executionStats" : {
                "averageDurationMs" : "#{json-unit.ignore}",
                "maxDurationMs" : "#{json-unit.ignore}",
                "medianDurationMs" : "#{json-unit.ignore}"
              }
            }
          ]
        }
    """);
    }

    @Test
    void nestedPropagationIsMonitored() {
        // given.
        propagationTestHelper.testNested();

        // when.
        String responseBody = getMonitoringResponse();

        // then.
        assertThatJson(responseBody)
                .isEqualTo(
                        // language=json
                        """
        {
          "entrypoints" : [ {
            "className" : "com.axelixlabs.axelix.sbs.spring.core.persistence.AbstractTransactionMonitoringSharedContextTest$PropagationTestHelper",
            "methodName" : "testNested",
            "executions" : [ {
              "startTimestampMs" : "#{json-unit.ignore}",
              "endTimestampMs" : "#{json-unit.ignore}",
              "queries" : [ {
                "sql" : "select o1_0.id,o1_0.last_name from owner o1_0 where o1_0.last_name=?",
                "startTimestampMs" : "#{json-unit.ignore}",
                "endTimestampMs" : "#{json-unit.ignore}"
              } ]
            } ],
            "executionStats" : {
              "averageDurationMs" : "#{json-unit.ignore}",
              "maxDurationMs" : "#{json-unit.ignore}",
              "medianDurationMs" : "#{json-unit.ignore}"
            }
          } ]
        }
    """);

        // and then. Verify that metrics were successfully published to MeterRegistry
        checkMeterRegistry(expectedClassName, "testNested", 1);
    }

    @Test
    @Transactional
    void supportsPropagationWithExistingTransactionShouldNotOpenATransaction() {
        // given.
        propagationTestHelper.testSupports("Rodriquez");

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
    void shouldDetectInMemoryPagination() {
        // given.
        Owner owner = new Owner();
        owner.addPet(new Pet("Rodriquez", owner));
        ownerRepository.save(owner);

        // when.
        propagationTestHelper.findAllWithPetsPageable();

        // then.
        String responseBody = getMonitoringResponse();

        assertThatJson(responseBody).node("entrypoints[0].methodName").isEqualTo("findAllWithPetsPageable");

        List<Boolean> inMemoryPaginatedFlags =
                JsonPath.read(responseBody, "$.entrypoints[0].executions[0].queries[*].inMemoryPaginated");

        assertThat(inMemoryPaginatedFlags).contains(true);
    }

    @Test
    void shouldMarkCollectionNPlusOne() {
        // given
        Owner owner1 = new Owner();
        owner1.addPet(new Pet("pet1", owner1));
        Owner owner2 = new Owner();
        owner2.addPet(new Pet("pet2", owner2));

        ownerRepository.save(owner1);
        ownerRepository.save(owner2);

        // when (will cause collection N+1)
        propagationTestHelper.loadOwnersAndAccessPets();

        // then
        String responseBody = getMonitoringResponse();

        assertThatJson(responseBody)
                // language=json
                .isEqualTo("""
            {
              "entrypoints" : [ {
                "className" : "#{json-unit.ignore}",
                "methodName" : "loadOwnersAndAccessPets",
                "executions" : [ {
                  "startTimestampMs" : "#{json-unit.ignore}",
                  "endTimestampMs" : "#{json-unit.ignore}",
                  "queries" : [ {
                    "sql" : "#{json-unit.ignore}",
                    "startTimestampMs" : "#{json-unit.ignore}",
                    "endTimestampMs" : "#{json-unit.ignore}",
                    "lazyLoadingTarget" : {
                      "ownerEntityClass" : "com.axelixlabs.axelix.sbs.spring.core.persistence.AbstractTransactionMonitoringSharedContextTest$Owner",
                      "associationPropertyName" : "pets"
                    }
                  }, {
                    "sql" : "#{json-unit.ignore}",
                    "startTimestampMs" : "#{json-unit.ignore}",
                    "endTimestampMs" : "#{json-unit.ignore}",
                    "lazyLoadingTarget" : {
                      "ownerEntityClass" : "com.axelixlabs.axelix.sbs.spring.core.persistence.AbstractTransactionMonitoringSharedContextTest$Owner",
                      "associationPropertyName" : "pets"
                    }
                  }, {
                    "sql" : "#{json-unit.ignore}",
                    "startTimestampMs" : "#{json-unit.ignore}",
                    "endTimestampMs" : "#{json-unit.ignore}"
                  } ]
                } ],
                "executionStats" : "#{json-unit.ignore}"
              } ]
            }
            """);
    }

    @Test
    void shouldMarkCollectionBatchPlusOne() {
        // given
        Owner owner1 = new Owner();
        owner1.addTag(new Tag("tag1", owner1));
        Owner owner2 = new Owner();
        owner2.addTag(new Tag("tag2", owner2));
        Owner owner3 = new Owner();
        owner3.addTag(new Tag("tag3", owner3));

        ownerRepository.save(owner1);
        ownerRepository.save(owner2);
        ownerRepository.save(owner3);

        // when (will cause collection Batch+1)
        propagationTestHelper.loadOwnersAndAccessTags();

        // then
        String responseBody = getMonitoringResponse();

        assertThatJson(responseBody).node("entrypoints[0].methodName").isEqualTo("loadOwnersAndAccessTags");

        List<String> lazyLoadingAssociations = JsonPath.read(
                responseBody, "$.entrypoints[0].executions[0].queries[*].lazyLoadingTarget.associationPropertyName");

        assertThat(lazyLoadingAssociations).isNotEmpty().allMatch("tags"::equals);
    }

    @Test
    @Disabled("X-to-one lazy loading interception is not implemented yet.")
    void shouldRecognizeXToOneLazyLoading() {
        // given
        Owner owner1 = new Owner().setLastName("owner1");
        Owner owner2 = new Owner().setLastName("owner2");
        ownerRepository.save(owner1);
        ownerRepository.save(owner2);

        petRepository.save(new Pet("pet1", owner1));
        petRepository.save(new Pet("pet2", owner2));

        // when (will cause entity N+1 — pet.getOwner() triggers separate SELECT per pet)
        propagationTestHelper.loadPetsAndAccessOwners();

        // then
        String responseBody = getMonitoringResponse();

        assertThatJson(responseBody).node("entrypoints[0].methodName").isEqualTo("loadPetsAndAccessOwners");

        List<String> lazyLoadingAssociations = JsonPath.read(
                responseBody, "$.entrypoints[0].executions[0].queries[*].lazyLoadingTarget.associationPropertyName");

        assertThat(lazyLoadingAssociations).isNotEmpty().allMatch("owner"::equals);
    }

    private void checkMeterRegistry(String expectedClassName, String expectedMethodName, int expectedQueryCount) {
        // given when. Verify Transaction Duration Timer
        Timer transactionTimer = meterRegistry
                .find(TRANSACTION_DURATION)
                .tag("class", expectedClassName)
                .tag("method", expectedMethodName)
                .timer();

        // then.
        assertThat(transactionTimer).isNotNull();
        assertThat(transactionTimer.count()).isEqualTo(1);
        assertThat(transactionTimer.totalTime(TimeUnit.NANOSECONDS)).isGreaterThan(0);

        // given when. Verify SQL Queries Counter
        Counter queriesCounter = meterRegistry
                .find(TRANSACTION_QUERIES)
                .tag("class", expectedClassName)
                .tag("method", expectedMethodName)
                .counter();

        // then.
        assertThat(queriesCounter).isNotNull();
        assertThat(queriesCounter.count()).isEqualTo(expectedQueryCount);
    }

    @ProtectedEndpointTests(
            method = com.axelixlabs.axelix.common.domain.http.HttpMethod.GET,
            path = "/actuator/axelix-transactions-monitoring")
    void negativeAuthTests() {}

    private String getMonitoringResponse() {
        ResponseEntity<String> response =
                restTemplate.asViewer().getForEntity("/actuator/axelix-transactions-monitoring", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }
}
