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

import java.time.Duration;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
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

/**
 * Integration test for {@link TransactionMonitoringEndpoint}
 *
 * TODO: Revisit design of this test.
 *
 * @since 26.01.2026
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
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
    @EnableJpaRepositories(
            basePackageClasses = TransactionMonitoringEndpointTest.OwnerRepository.class,
            considerNestedRepositories = true)
    @EntityScan(basePackageClasses = TransactionMonitoringEndpointTest.Owner.class)
    static class TransactionMonitoringEndpointTestConfiguration {

        @Bean
        public TransactionMonitoringEndpoint transactionMonitoringEndpoint(
                TransactionMonitoringService transactionMonitoringService) {
            return new TransactionMonitoringEndpoint(
                    (DefaultTransactionMonitoringService) transactionMonitoringService);
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
                TransactionStatsCollector transactionStatsCollector) {
            return new TransactionMonitoringBeanPostProcessor(transactionStatsCollector);
        }

        @Bean
        public PropagationTestHelper propagationTestHelper(
                OwnerRepository ownerRepository, @Lazy PropagationTestHelper self) {
            return new PropagationTestHelper(ownerRepository, self);
        }

        @Bean
        public PropagationTestService propagationTestService(
                OwnerRepository ownerRepository, PropagationTestHelper helper) {
            return new PropagationTestService(ownerRepository, helper);
        }
    }

    /**
     * Simple {@link Entity} for test.
     */
    @Entity
    static class Owner {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String lastName;

        public Long getId() {
            return id;
        }

        public String getLastName() {
            return lastName;
        }
    }

    /**
     * Simple {@link JpaRepository} for test.
     */
    interface OwnerRepository extends JpaRepository<Owner, Long> {

        @Transactional
        default Owner findByLastName(String lastName) {
            return new Owner();
        }

        @Transactional(propagation = Propagation.SUPPORTS)
        default List<Owner> findAll() {
            return List.of(new Owner());
        }
    }

    /**
     * Test helper for demonstrating transaction propagation scenarios and monitoring.
     * Used in integration tests to verify transaction tracking behavior.
     */
    static class PropagationTestHelper {

        private final OwnerRepository ownerRepository;
        private final PropagationTestHelper self;

        public PropagationTestHelper(OwnerRepository ownerRepository, @Lazy PropagationTestHelper self) {
            this.ownerRepository = ownerRepository;
            this.self = self;
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void testRequiresNew(String lastName) {
            ownerRepository.findByLastName(lastName);
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void testNestedRequiresNew() {
            ownerRepository.findByLastName("Franklin");
        }

        @Transactional(propagation = Propagation.NESTED)
        public void testNested() {
            ownerRepository.findByLastName("Schroeder");
        }

        @Transactional(propagation = Propagation.MANDATORY)
        public void testMandatory(String lastName) {
            ownerRepository.findByLastName(lastName);
        }

        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        public void testNotSupported(String lastName) {
            ownerRepository.findByLastName(lastName);
        }

        @Transactional
        public void testSelfInvocation() {
            internalMethod();
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void internalMethod() {
            ownerRepository.findByLastName("Black");
        }

        @Transactional
        public void testCorrectSelfInvocation() {
            self.requiresNewViaProxy();
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void requiresNewViaProxy() {
            ownerRepository.findByLastName("White");
        }
    }

    /**
     * Test service for demonstrating transaction propagation scenarios and monitoring.
     * Used in integration tests to verify transaction tracking behavior.
     */
    static class PropagationTestService {

        private final OwnerRepository ownerRepository;
        private final PropagationTestHelper helperService;

        public PropagationTestService(OwnerRepository ownerRepository, PropagationTestHelper helperService) {
            this.ownerRepository = ownerRepository;
            this.helperService = helperService;
        }

        @Transactional(propagation = Propagation.REQUIRED)
        void testRequired(String lastName) {
            ownerRepository.findByLastName(lastName);
            helperService.testNestedRequiresNew();
        }

        public void testFromNonTransactional(String lastName) {
            helperService.testRequiresNew(lastName);
        }

        @Transactional
        protected void testRollbackScenario(String lastName) {
            ownerRepository.findByLastName(lastName);
            helperService.testNestedRequiresNew();
            throw new RuntimeException("Test rollback");
        }

        @Transactional(propagation = Propagation.SUPPORTS)
        public void testSupports(String lastName) {
            ownerRepository.findByLastName(lastName);
        }

        @Transactional(propagation = Propagation.SUPPORTS)
        public void testSupportsWithoutTransaction() {}
    }
}
