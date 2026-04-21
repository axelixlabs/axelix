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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test for {@link TransactionMonitoringEndpoint}
 *
 * @since 26.01.2026
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"management.endpoints.web.exposure.include=axelix-transactions-monitoring"})
@Import(TransactionMonitoringEndpointTest.TransactionMonitoringEndpointTestConfiguration.class)
class TransactionMonitoringEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PropagationTestHelper propagationTestHelper;

    @Autowired
    private TransactionStatsCollector transactionStatsCollector;

    @Autowired
    private OwnerRepository ownerRepository;

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
                        """
                {
                  "entrypoints" : [ {
                    "className" : "com.axelixlabs.axelix.sbs.spring.core.transactions.TransactionMonitoringEndpointTest$PropagationTestHelper",
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
    }

    @Test
    void shouldClearsAllTransactionMonitoringStats() {
        for (int i = 0; i < 3; i++) {
            propagationTestHelper.saveRequiresNew("Johnson");
        }

        var allStats = transactionStatsCollector.getAllStats();

        assertThat(allStats.size()).isGreaterThan(0);

        ResponseEntity<Void> deleteResponse =
                restTemplate.exchange("/actuator/axelix-transactions-monitoring", HttpMethod.DELETE, null, Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        allStats = transactionStatsCollector.getAllStats();
        assertThat(allStats).isEmpty();
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
                "className" : "com.axelixlabs.axelix.sbs.spring.core.transactions.TransactionMonitoringEndpointTest$PropagationTestHelper",
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
                    "className" : "com.axelixlabs.axelix.sbs.spring.core.transactions.TransactionMonitoringEndpointTest$PropagationTestHelper",
                    "methodName" : "findOwnerById",
                    "executions" : [ {
                      "startTimestampMs" : "#{json-unit.ignore}",
                      "endTimestampMs" : "#{json-unit.ignore}",
                      "queries" : [ {
                        "sql" : "select o1_0.id,o1_0.last_name from owner o1_0 where o1_0.id=?",
                        "startTimestampMs" : "#{json-unit.ignore}",
                        "endTimestampMs" : "#{json-unit.ignore}"
                      }, {
                        "sql" : "select p1_0.owner_id,p1_0.id,p1_0.name from pet p1_0 where p1_0.owner_id=?",
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
                "className" : "com.axelixlabs.axelix.sbs.spring.core.transactions.TransactionMonitoringEndpointTest$PropagationTestHelper",
                "methodName" : "updateOwner",
                "executions" : [ {
                  "startTimestampMs" : "#{json-unit.ignore}",
                  "endTimestampMs" : "#{json-unit.ignore}",
                  "queries" : [ {
                    "sql" : "select o1_0.id,o1_0.last_name from owner o1_0 where o1_0.id=?",
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
                    "className" : "com.axelixlabs.axelix.sbs.spring.core.transactions.TransactionMonitoringEndpointTest$PropagationTestHelper",
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
    }

    @Test
    @Transactional
    void supportsPropagationWithExistingTransaction() {
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
                    "className" : "com.axelixlabs.axelix.sbs.spring.core.transactions.TransactionMonitoringEndpointTest$PropagationTestHelper",
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
    }

    private String getMonitoringResponse() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/actuator/axelix-transactions-monitoring", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    @TestConfiguration
    @EnableJpaRepositories(basePackageClasses = OwnerRepository.class, considerNestedRepositories = true)
    @EntityScan(basePackageClasses = {Owner.class, Pet.class})
    static class TransactionMonitoringEndpointTestConfiguration {

        @Bean
        public TransactionMonitoringEndpoint transactionMonitoringEndpoint(
                TransactionMonitoringService transactionMonitoringService) {
            return new TransactionMonitoringEndpoint(transactionMonitoringService);
        }

        @Bean
        public TransactionMonitoringService transactionMonitoringService(
                TransactionStatsCollector transactionStatsCollector) {
            return new DefaultTransactionMonitoringService(transactionStatsCollector);
        }

        @Bean
        public TransactionStatsCollector transactionStatsCollector() {
            return new DefaultTransactionStatsCollector(30, Duration.ofSeconds(10000));
        }

        @Bean
        public TransactionMonitoringBeanPostProcessor transactionMonitoringBeanPostProcessor(
                TransactionStatsCollector transactionStatsCollector, QueriesRecorder queriesCollector) {
            return new TransactionMonitoringBeanPostProcessor(transactionStatsCollector, queriesCollector);
        }

        @Bean
        public QueriesRecorder queriesStatsCollector() {
            return new DefaultQueriesRecorder();
        }

        @Bean
        public ProxyingDataSourceBeanPostProcessor transactionMonitoringDataSourceBeanPostProcessor(
                QueriesRecorder queriesCollector) {
            return new ProxyingDataSourceBeanPostProcessor(queriesCollector);
        }

        @Bean
        public PropagationTestHelper propagationTestHelper(OwnerRepository ownerRepository) {
            return new PropagationTestHelper(ownerRepository);
        }
    }

    @Entity
    @Table(name = "owner")
    static class Owner {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String lastName;

        @OneToMany(mappedBy = "owner", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
        private List<Pet> pets = new ArrayList<>();

        public List<Pet> getPets() {
            return pets;
        }

        public Long getId() {
            return id;
        }

        public String getLastName() {
            return lastName;
        }

        public Owner setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Owner addPet(Pet pet) {
            this.pets.add(pet);
            return this;
        }
    }

    @Entity
    @Table(name = "pet")
    static class Pet {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        @ManyToOne
        @JoinColumn(name = "owner_id")
        private Owner owner;

        public Pet() {}

        public Pet(String name, Owner owner) {
            this.name = name;
            this.owner = owner;
        }
    }

    interface OwnerRepository extends JpaRepository<Owner, Long> {

        @Transactional
        Owner findByLastName(String lastName);

        @Transactional(propagation = Propagation.SUPPORTS)
        default List<Owner> findAll() {
            return List.of(new Owner());
        }
    }

    static class PropagationTestHelper {

        private final OwnerRepository ownerRepository;

        public PropagationTestHelper(OwnerRepository ownerRepository) {
            this.ownerRepository = ownerRepository;
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void saveRequiresNew(String lastName) {
            ownerRepository.save(new Owner().setLastName(lastName));
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void testSaveMultipleOwners() {
            ownerRepository.saveAll(List.of(new Owner(), new Owner(), new Owner()));
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void updateOwner(Owner owner) {
            // will cause entityManager.merge --> new SELECT, since Owner has an id
            ownerRepository.save(owner);
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void findOwnerById(Long id) {
            Owner owner = ownerRepository.findById(id).orElseThrow();
            owner.getPets().size(); // will cause n + 1
        }

        @Transactional(propagation = Propagation.NESTED)
        public void testNested() {
            ownerRepository.findByLastName("Schroeder");
        }

        @Transactional(propagation = Propagation.SUPPORTS)
        public void testSupports(String lastName) {
            ownerRepository.findByLastName(lastName);
        }

        @Transactional(propagation = Propagation.SUPPORTS)
        public void testSupportsWithoutTransaction() {}

        @Transactional
        public void testRollbackScenario(String lastName) {
            ownerRepository.findByLastName(lastName);
            throw new RuntimeException("Test rollback");
        }
    }
}
