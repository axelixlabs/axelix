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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.jayway.jsonpath.JsonPath;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.persistence.TransactionMonitoringEndpointTest.TransactionMonitoringEndpointTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.LogbackInMemoryPaginationAppenderRegistrar;
import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.NPlusOneCollectionLoadListener;
import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.NPlusOneEntityLoadListener;
import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.NPlusOneIntegrator;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.DefaultTransactionMonitoringService;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.DefaultTransactionStatsCollector;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAccessor;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionMonitoringService;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionStatsCollector;
import com.axelixlabs.axelix.sbs.spring.core.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.sbs.spring.core.utils.auth.ProtectedEndpointTests;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.jpa.boot.internal.EntityManagerFactoryBuilderImpl.INTEGRATOR_PROVIDER;

/**
 * Integration test for {@link TransactionMonitoringEndpoint}
 *
 * @since 26.01.2026
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"management.endpoints.web.exposure.include=axelix-transactions-monitoring"})
@Import({TransactionMonitoringEndpointTestConfiguration.class, JwtAuthTestConfiguration.class})
class TransactionMonitoringEndpointTest {

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
    private TagRepository tagRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void cleanUp() {
        transactionStatsCollector.clearStats();
        petRepository.deleteAll();
        tagRepository.deleteAll();
        ownerRepository.deleteAll();
    }

    @Test
    void shouldReturnsStatsAfterTransactionExecution() {
        propagationTestHelper.saveRequiresNew("Smith");

        String responseBody = getMonitoringResponse();

        assertThatJson(responseBody)
                .isEqualTo(
                        // language=json
                        "{\n" + "  \"entrypoints\" : [ {\n"
                                + "    \"className\" : \"com.axelixlabs.axelix.sbs.spring.core.persistence.TransactionMonitoringEndpointTest$PropagationTestHelper\",\n"
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
    void shouldTrackMultipleQueriesInsideTransaction() {
        propagationTestHelper.saveMultipleOwners();

        String responseBody = getMonitoringResponse();

        assertThatJson(responseBody)
                .isEqualTo(
                        // language=json
                        "{\n" + "  \"entrypoints\" : [ {\n"
                                + "    \"className\" : \"com.axelixlabs.axelix.sbs.spring.core.persistence.TransactionMonitoringEndpointTest$PropagationTestHelper\",\n"
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
                                + "    \"className\" : \"com.axelixlabs.axelix.sbs.spring.core.persistence.TransactionMonitoringEndpointTest$PropagationTestHelper\",\n"
                                + "    \"methodName\" : \"findOwnerById\",\n"
                                + "    \"executions\" : [ {\n"
                                + "      \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"endTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"queries\" : [ {\n"
                                + "        \"sql\" : \"select transactio0_.id as id1_1_0_, transactio0_.last_name as last_nam2_1_0_ from owner transactio0_ where transactio0_.id=?\",\n"
                                + "        \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "        \"endTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "        \"lazyLoadingTarget\" : {\n"
                                + "          \"ownerEntityClass\" : \"com.axelixlabs.axelix.sbs.spring.core.persistence.TransactionMonitoringEndpointTest$Owner\",\n"
                                + "          \"associationPropertyName\" : \"pets\"\n"
                                + "        }\n"
                                + "      }, {\n"
                                + "        \"sql\" : \"select pets0_.owner_id as owner_id4_2_0_, pets0_.id as id1_2_0_, pets0_.id as id1_2_1_, pets0_.category_id as category3_2_1_, pets0_.name as name2_2_1_, pets0_.owner_id as owner_id4_2_1_ from pet pets0_ where pets0_.owner_id=?\",\n"
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
                                + "    \"className\" : \"com.axelixlabs.axelix.sbs.spring.core.persistence.TransactionMonitoringEndpointTest$PropagationTestHelper\",\n"
                                + "    \"methodName\" : \"updateOwner\",\n"
                                + "    \"executions\" : [ {\n"
                                + "      \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"endTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"queries\" : [ {\n"
                                + "        \"sql\" : \"select transactio0_.id as id1_1_1_, transactio0_.last_name as last_nam2_1_1_, tags1_.owner_id as owner_id3_3_3_, tags1_.id as id1_3_3_, tags1_.id as id1_3_0_, tags1_.name as name2_3_0_, tags1_.owner_id as owner_id3_3_0_ from owner transactio0_ left outer join tag tags1_ on transactio0_.id=tags1_.owner_id where transactio0_.id=?\",\n"
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
                                + "    \"className\" : \"com.axelixlabs.axelix.sbs.spring.core.persistence.TransactionMonitoringEndpointTest$PropagationTestHelper\",\n"
                                + "    \"methodName\" : \"testRollbackScenario\",\n"
                                + "    \"executions\" : [ {\n"
                                + "      \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"endTimestampMs\" : \"#{json-unit.ignore}\",\n"
                                + "      \"queries\" : [ {\n"
                                + "        \"sql\" : \"select transactio0_.id as id1_1_, transactio0_.last_name as last_nam2_1_ from owner transactio0_ where transactio0_.last_name=?\",\n"
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
    void nestedRequiresNewPropagationIsMonitoredSeparately() {
        // given.
        propagationTestHelper.outerRequiredMethod("ParentOwner");

        // when.
        String responseBody = getMonitoringResponse();

        // then.
        assertThatJson(responseBody)
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo("{\n" + "  \"entrypoints\" : [\n"
                        + "    {\n"
                        + "      \"className\" : \"com.axelixlabs.axelix.sbs.spring.core.persistence.TransactionMonitoringEndpointTest$PropagationTestHelper\",\n"
                        + "      \"methodName\" : \"saveRequiresNew\",\n"
                        + "      \"executions\" : [\n"
                        + "        {\n"
                        + "          \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                        + "          \"endTimestampMs\" : \"#{json-unit.ignore}\",\n"
                        + "          \"queries\" : [\n"
                        + "            {\n"
                        + "              \"sql\" : \"insert into owner (id, last_name) values (default, ?)\",\n"
                        + "              \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                        + "              \"endTimestampMs\" : \"#{json-unit.ignore}\"\n"
                        + "            }\n"
                        + "          ]\n"
                        + "        }\n"
                        + "      ],\n"
                        + "      \"executionStats\" : {\n"
                        + "        \"averageDurationMs\" : \"#{json-unit.ignore}\",\n"
                        + "        \"maxDurationMs\" : \"#{json-unit.ignore}\",\n"
                        + "        \"medianDurationMs\" : \"#{json-unit.ignore}\"\n"
                        + "      }\n"
                        + "    },\n"
                        + "    {\n"
                        + "      \"className\" : \"com.axelixlabs.axelix.sbs.spring.core.persistence.TransactionMonitoringEndpointTest$PropagationTestHelper\",\n"
                        + "      \"methodName\" : \"outerRequiredMethod\",\n"
                        + "      \"executions\" : [\n"
                        + "        {\n"
                        + "          \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                        + "          \"endTimestampMs\" : \"#{json-unit.ignore}\",\n"
                        + "          \"queries\" : [\n"
                        + "            {\n"
                        + "              \"sql\" : \"insert into owner (id, last_name) values (default, ?)\",\n"
                        + "              \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                        + "              \"endTimestampMs\" : \"#{json-unit.ignore}\"\n"
                        + "            },\n"
                        + "            {\n"
                        + "              \"sql\" : \"insert into owner (id, last_name) values (default, ?)\",\n"
                        + "              \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                        + "              \"endTimestampMs\" : \"#{json-unit.ignore}\"\n"
                        + "            }\n"
                        + "          ]\n"
                        + "        }\n"
                        + "      ],\n"
                        + "      \"executionStats\" : {\n"
                        + "        \"averageDurationMs\" : \"#{json-unit.ignore}\",\n"
                        + "        \"maxDurationMs\" : \"#{json-unit.ignore}\",\n"
                        + "        \"medianDurationMs\" : \"#{json-unit.ignore}\"\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ]\n"
                        + "}");
    }

    @Test
    void nestedPropagationIsMonitored() {
        // given.
        propagationTestHelper.testNested();

        // when.
        String responseBody = getMonitoringResponse();

        // then.
        assertThatJson(responseBody)
                // language=json
                .isEqualTo("{\n" + "  \"entrypoints\" : [\n"
                        + "    {\n"
                        + "      \"className\" : \"com.axelixlabs.axelix.sbs.spring.core.persistence.TransactionMonitoringEndpointTest$PropagationTestHelper\",\n"
                        + "      \"methodName\" : \"testNested\",\n"
                        + "      \"executions\" : [\n"
                        + "        {\n"
                        + "          \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                        + "          \"endTimestampMs\" : \"#{json-unit.ignore}\",\n"
                        + "          \"queries\" : [\n"
                        + "            {\n"
                        + "              \"sql\" : \"select transactio0_.id as id1_1_, transactio0_.last_name as last_nam2_1_ from owner transactio0_ where transactio0_.last_name=?\",\n"
                        + "              \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                        + "              \"endTimestampMs\" : \"#{json-unit.ignore}\"\n"
                        + "            }\n"
                        + "          ]\n"
                        + "        }\n"
                        + "      ],\n"
                        + "      \"executionStats\" : {\n"
                        + "        \"averageDurationMs\" : \"#{json-unit.ignore}\",\n"
                        + "        \"maxDurationMs\" : \"#{json-unit.ignore}\",\n"
                        + "        \"medianDurationMs\" : \"#{json-unit.ignore}\"\n"
                        + "      }\n"
                        + "    }\n"
                        + "  ]\n"
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
    void supportsPropagationWithExistingTransactionShouldNotOpenATransaction() {
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
        // given.
        Owner owner1 = new Owner();
        owner1.addPet(new Pet("pet1", owner1));
        Owner owner2 = new Owner();
        owner2.addPet(new Pet("pet2", owner2));

        ownerRepository.save(owner1);
        ownerRepository.save(owner2);

        // when. (will cause collection N+1)
        propagationTestHelper.loadOwnersAndAccessPets();

        // then.
        String responseBody = getMonitoringResponse();

        assertThatJson(responseBody)
                // language=json
                .isEqualTo("{\n"
                        + "  \"entrypoints\" : [ {\n"
                        + "    \"className\" : \"#{json-unit.ignore}\",\n"
                        + "    \"methodName\" : \"loadOwnersAndAccessPets\",\n"
                        + "    \"executions\" : [ {\n"
                        + "      \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                        + "      \"endTimestampMs\" : \"#{json-unit.ignore}\",\n"
                        + "      \"queries\" : [ {\n"
                        + "        \"sql\" : \"#{json-unit.ignore}\",\n"
                        + "        \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                        + "        \"endTimestampMs\" : \"#{json-unit.ignore}\",\n"
                        + "        \"lazyLoadingTarget\" : {\n"
                        + "          \"ownerEntityClass\" : \"com.axelixlabs.axelix.sbs.spring.core.persistence.TransactionMonitoringEndpointTest$Owner\",\n"
                        + "          \"associationPropertyName\" : \"pets\"\n"
                        + "        }\n"
                        + "      }, {\n"
                        + "        \"sql\" : \"#{json-unit.ignore}\",\n"
                        + "        \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                        + "        \"endTimestampMs\" : \"#{json-unit.ignore}\",\n"
                        + "        \"lazyLoadingTarget\" : {\n"
                        + "          \"ownerEntityClass\" : \"com.axelixlabs.axelix.sbs.spring.core.persistence.TransactionMonitoringEndpointTest$Owner\",\n"
                        + "          \"associationPropertyName\" : \"pets\"\n"
                        + "        }\n"
                        + "      }, {\n"
                        + "        \"sql\" : \"#{json-unit.ignore}\",\n"
                        + "        \"startTimestampMs\" : \"#{json-unit.ignore}\",\n"
                        + "        \"endTimestampMs\" : \"#{json-unit.ignore}\"\n"
                        + "      } ]\n"
                        + "    } ],\n"
                        + "    \"executionStats\" : \"#{json-unit.ignore}\"\n"
                        + "  } ]\n"
                        + "}");
    }

    @Test
    void shouldMarkCollectionBatchPlusOne() {
        // given.
        Owner owner1 = new Owner();
        owner1.addTag(new Tag("tag1", owner1));
        Owner owner2 = new Owner();
        owner2.addTag(new Tag("tag2", owner2));
        Owner owner3 = new Owner();
        owner3.addTag(new Tag("tag3", owner3));

        ownerRepository.save(owner1);
        ownerRepository.save(owner2);
        ownerRepository.save(owner3);

        // when. (will cause collection Batch+1)
        propagationTestHelper.loadOwnersAndAccessTags();

        // then.
        String responseBody = getMonitoringResponse();

        assertThatJson(responseBody).node("entrypoints[0].methodName").isEqualTo("loadOwnersAndAccessTags");

        List<String> lazyLoadingAssociations = JsonPath.read(
                responseBody, "$.entrypoints[0].executions[0].queries[*].lazyLoadingTarget.associationPropertyName");

        assertThat(lazyLoadingAssociations).isNotEmpty().allMatch("tags"::equals);
    }

    @Test
    @Disabled("X-to-one lazy loading interception is not implemented yet.")
    void shouldRecognizeXToOneLazyLoading() {
        // given.
        Owner owner1 = new Owner().setLastName("owner1");
        Owner owner2 = new Owner().setLastName("owner2");
        ownerRepository.save(owner1);
        ownerRepository.save(owner2);

        petRepository.save(new Pet("pet1", owner1));
        petRepository.save(new Pet("pet2", owner2));

        // when. (will cause entity N+1 — pet.getOwner() triggers separate SELECT per pet)
        propagationTestHelper.loadPetsAndAccessOwners();

        // then.
        String responseBody = getMonitoringResponse();

        assertThatJson(responseBody).node("entrypoints[0].methodName").isEqualTo("loadPetsAndAccessOwners");

        List<String> lazyLoadingAssociations = JsonPath.read(
                responseBody, "$.entrypoints[0].executions[0].queries[*].lazyLoadingTarget.associationPropertyName");

        assertThat(lazyLoadingAssociations).isNotEmpty().allMatch("owner"::equals);
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
            return new DefaultTransactionStatsCollector(30);
        }

        @Bean
        public TransactionMonitoringBeanPostProcessor transactionMonitoringBeanPostProcessor(
                TransactionStatsCollector transactionStatsCollector, TransactionAccessor transactionAccessor) {
            return new TransactionMonitoringBeanPostProcessor(transactionStatsCollector, transactionAccessor);
        }

        @Bean
        public ProxyingDataSourceBeanPostProcessor transactionMonitoringDataSourceBeanPostProcessor(
                TransactionAccessor transactionAccessor) {
            return new ProxyingDataSourceBeanPostProcessor(transactionAccessor);
        }

        @Bean
        public TransactionAccessor transactionAccessor() {
            return new TransactionAccessor();
        }

        @Bean
        public PropagationTestHelper propagationTestHelper(
                OwnerRepository ownerRepository, PetRepository petRepository, @Lazy PropagationTestHelper self) {
            return new PropagationTestHelper(ownerRepository, petRepository, self);
        }

        @Bean
        public HibernatePropertiesCustomizer axelixhibernatePropertiesCustomizer(
                TransactionAccessor transactionAccessor) {
            return properties ->
                    properties.put(INTEGRATOR_PROVIDER, (IntegratorProvider) () -> List.of(new NPlusOneIntegrator(
                            new NPlusOneEntityLoadListener(transactionAccessor),
                            new NPlusOneCollectionLoadListener(transactionAccessor))));
        }

        @EventListener(ApplicationReadyEvent.class)
        public void registerAppender() {
            new LogbackInMemoryPaginationAppenderRegistrar().register();
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

        @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        @BatchSize(size = 2)
        private List<Tag> tags = new ArrayList<>();

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

        public List<Tag> getTags() {
            return tags;
        }

        public void addTag(Tag tag) {
            this.tags.add(tag);
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

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "category_id")
        private Category category;

        public Pet() {}

        public Pet(String name, Owner owner) {
            this.name = name;
            this.owner = owner;
        }

        public Owner getOwner() {
            return owner;
        }

        public Category getCategory() {
            return category;
        }
    }

    @Entity
    @Table(name = "tag")
    static class Tag {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        @ManyToOne
        @JoinColumn(name = "owner_id")
        @OnDelete(action = OnDeleteAction.CASCADE)
        private Owner owner;

        public Tag() {}

        public Tag(String name, Owner owner) {
            this.name = name;
            this.owner = owner;
        }
    }

    @Entity
    @Table(name = "category")
    @BatchSize(size = 2)
    static class Category {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        public Category() {}

        public Category(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    interface OwnerRepository extends JpaRepository<Owner, Long> {

        @Transactional
        Owner findByLastName(String lastName);

        @Transactional
        @Query(
                value = "SELECT o FROM TransactionMonitoringEndpointTest$Owner o JOIN FETCH o.pets",
                countQuery = "SELECT COUNT(o) FROM TransactionMonitoringEndpointTest$Owner o")
        Page<Owner> findAllWithPets(Pageable pageable);
    }

    interface PetRepository extends JpaRepository<Pet, Long> {}

    interface TagRepository extends JpaRepository<Tag, Long> {}

    static class PropagationTestHelper {

        private final OwnerRepository ownerRepository;
        private final PropagationTestHelper self;
        private final PetRepository petRepository;

        public PropagationTestHelper(
                OwnerRepository ownerRepository, PetRepository petRepository, @Lazy PropagationTestHelper self) {
            this.ownerRepository = ownerRepository;
            this.petRepository = petRepository;
            this.self = self;
        }

        // IMPORTANT: Calling via 'self' proxy is required to properly test the REQUIRED -> REQUIRES_NEW stack behavior.
        @Transactional(propagation = Propagation.REQUIRED)
        public void outerRequiredMethod(String outerName) {
            ownerRepository.save(new Owner().setLastName(outerName));

            self.saveRequiresNew("SomeName");

            ownerRepository.save(new Owner().setLastName(outerName));
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void saveRequiresNew(String lastName) {
            ownerRepository.save(new Owner().setLastName(lastName));
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void saveMultipleOwners() {
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

        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        public void testNotSupported(String lastName) {
            ownerRepository.findByLastName(lastName);
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

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void findAllWithPetsPageable() {
            ownerRepository.findAllWithPets(PageRequest.of(0, 5));
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void loadOwnersAndAccessPets() {
            List<Owner> owners = ownerRepository.findAll();
            owners.forEach(o -> o.getPets().size());
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void loadOwnersAndAccessTags() {
            List<Owner> owners = ownerRepository.findAll();
            owners.forEach(o -> o.getTags().size());
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void loadPetsAndAccessOwners() {
            List<Pet> pets = petRepository.findAll();
            pets.forEach(p -> p.getOwner().getId());
        }
    }
}
