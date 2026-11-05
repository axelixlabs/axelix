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
package com.axelixlabs.axelix.master.mcp.tools;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

import com.axelixlabs.axelix.common.contract.metadata.CountedLazyLoadingTarget;
import com.axelixlabs.axelix.common.contract.metadata.ExecutionStats;
import com.axelixlabs.axelix.common.contract.metadata.LazyLoadingTarget;
import com.axelixlabs.axelix.common.contract.metadata.PersistenceInsights;
import com.axelixlabs.axelix.common.contract.metadata.TransactionAggregatedProfile;
import com.axelixlabs.axelix.common.contract.metadata.TransactionOrigin;
import com.axelixlabs.axelix.common.contract.metadata.TransactionalKey;
import com.axelixlabs.axelix.common.domain.insights.GarbageCollector;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot.SnapshotId;
import com.axelixlabs.axelix.master.domain.Insights;
import com.axelixlabs.axelix.master.domain.JavaVersion;
import com.axelixlabs.axelix.master.domain.JdkVendor;
import com.axelixlabs.axelix.master.mcp.McpEndpoints;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TransactionsMcpServerTools}.
 *
 * @author Mikhail Polivakha
 */
class TransactionsMcpServerToolsTest extends AbstractMcpServerToolsTest {

    private static final String GROUP_ID = "com.example";
    private static final String ARTIFACT_ID = "orders-service";
    private static final String CLASS_NAME = "com.example.OrderService";
    private static final String METHOD_NAME = "placeOrder";

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    @AfterEach
    void deleteSnapshots() {
        jdbcAggregateTemplate.deleteAll(HistoricalApplicationSnapshot.class);
    }

    @Nested
    class WhenMethodNotSpecified {

        @Test
        void shouldReturnProblemsOfEveryProblematicTransaction() {
            // given.
            TransactionAggregatedProfile nPlusOneTransaction =
                    profile(CLASS_NAME, METHOD_NAME, List.of(nPlusOne("items", 5)), Map.of());
            TransactionAggregatedProfile paginationTransaction =
                    profile(CLASS_NAME, "loadOwners", List.of(), Map.of("owners", 2));
            TransactionAggregatedProfile cleanTransaction =
                    profile("com.example.ReportService", "buildReport", List.of(nPlusOne("logs", 1)), Map.of());
            stubApplication(new PersistenceInsights()
                    .transactions(List.of(nPlusOneTransaction, paginationTransaction, cleanTransaction)));

            // when.
            String result = getApplicationTransactionsProfile(GROUP_ID, ARTIFACT_ID, null, null);

            // then. the clean transaction is left out, and the lazy-loading counts are not exposed.
            // language=json
            assertThatJson(result).isEqualTo("""
                    [
                      {
                        "transactionalKey": { "className": "com.example.OrderService", "methodName": "placeOrder" },
                        "nPlusOne": [
                          { "ownerEntityClass": "com.example.Order", "associationPropertyName": "items" }
                        ],
                        "inMemoryPaginatedEntities": []
                      },
                      {
                        "transactionalKey": { "className": "com.example.OrderService", "methodName": "loadOwners" },
                        "nPlusOne": [],
                        "inMemoryPaginatedEntities": ["owners"]
                      }
                    ]
                    """);
        }
    }

    @Nested
    class WhenMethodSpecified {

        @Test
        void shouldReturnOnlyProblemsForMatchingTransaction() {
            // given.
            TransactionAggregatedProfile placeOrder =
                    profile(CLASS_NAME, METHOD_NAME, List.of(nPlusOne("items", 42)), Map.of("orders", 3));
            TransactionAggregatedProfile other =
                    profile(CLASS_NAME, "loadOwners", List.of(nPlusOne("pets", 7)), Map.of());
            stubApplication(new PersistenceInsights().transactions(List.of(placeOrder, other)));

            // when.
            String result = getApplicationTransactionsProfile(GROUP_ID, ARTIFACT_ID, CLASS_NAME, METHOD_NAME);

            // then.
            // language=json
            assertThatJson(result).isEqualTo("""
                    [
                      {
                        "transactionalKey": { "className": "com.example.OrderService", "methodName": "placeOrder" },
                        "nPlusOne": [
                          { "ownerEntityClass": "com.example.Order", "associationPropertyName": "items" }
                        ],
                        "inMemoryPaginatedEntities": ["orders"]
                      }
                    ]
                    """);
        }

        @Test
        void shouldExcludeLazyLoadingTargetsLoadedOnlyOnce() {
            // given. an association lazily loaded exactly once is legitimate and is not an N+1.
            TransactionAggregatedProfile placeOrder =
                    profile(CLASS_NAME, METHOD_NAME, List.of(nPlusOne("items", 1)), Map.of());
            stubApplication(new PersistenceInsights().transactions(List.of(placeOrder)));

            // when.
            String result = getApplicationTransactionsProfile(GROUP_ID, ARTIFACT_ID, CLASS_NAME, METHOD_NAME);

            // then.
            // language=json
            assertThatJson(result).isEqualTo("""
                    [
                      {
                        "transactionalKey": { "className": "com.example.OrderService", "methodName": "placeOrder" },
                        "nPlusOne": [],
                        "inMemoryPaginatedEntities": []
                      }
                    ]
                    """);
        }

        @Test
        void shouldReturnMessageWhenNoMatchingTransaction() {
            // given.
            stubApplication(new PersistenceInsights()
                    .transactions(List.of(profile("com.example.OwnerService", "loadOwners", List.of(), Map.of()))));

            // when.
            String result = getApplicationTransactionsProfile(GROUP_ID, ARTIFACT_ID, CLASS_NAME, METHOD_NAME);

            // then.
            assertThat(result)
                    .isEqualTo("No transaction found for method 'com.example.OrderService#placeOrder' "
                            + "in application 'com.example:orders-service'.");
        }

        @Test
        void shouldReturnMessageWhenOnlyMethodNameProvided() {
            // given.
            stubApplication(new PersistenceInsights().transactions(List.of()));

            // when.
            String result = getApplicationTransactionsProfile(GROUP_ID, ARTIFACT_ID, null, METHOD_NAME);

            // then.
            assertThat(result).isEqualTo("'methodName' can only be provided together with 'className'.");
        }
    }

    @Nested
    class WhenOnlyClassNameProvided {

        @Test
        void shouldReturnProblemsOfEveryProblematicTransactionInThatClass() {
            // given.
            TransactionAggregatedProfile placeOrder =
                    profile(CLASS_NAME, METHOD_NAME, List.of(nPlusOne("items", 5)), Map.of());
            TransactionAggregatedProfile cancelOrder =
                    profile(CLASS_NAME, "cancelOrder", List.of(), Map.of("orders", 2));
            TransactionAggregatedProfile cleanOrderMethod =
                    profile(CLASS_NAME, "findOrder", List.of(nPlusOne("logs", 1)), Map.of());
            TransactionAggregatedProfile otherClass =
                    profile("com.example.OwnerService", "loadOwners", List.of(nPlusOne("pets", 9)), Map.of());
            stubApplication(new PersistenceInsights()
                    .transactions(List.of(placeOrder, cancelOrder, cleanOrderMethod, otherClass)));

            // when.
            String result = getApplicationTransactionsProfile(GROUP_ID, ARTIFACT_ID, CLASS_NAME, null);

            // then. the clean 'findOrder' and the other class's 'loadOwners' are left out.
            // language=json
            assertThatJson(result).isEqualTo("""
                    [
                      {
                        "transactionalKey": { "className": "com.example.OrderService", "methodName": "placeOrder" },
                        "nPlusOne": [
                          { "ownerEntityClass": "com.example.Order", "associationPropertyName": "items" }
                        ],
                        "inMemoryPaginatedEntities": []
                      },
                      {
                        "transactionalKey": { "className": "com.example.OrderService", "methodName": "cancelOrder" },
                        "nPlusOne": [],
                        "inMemoryPaginatedEntities": ["orders"]
                      }
                    ]
                    """);
        }
    }

    @Nested
    class ApplicationLookup {

        @Test
        void shouldReturnMessageWhenApplicationNotFound() {
            // when. no snapshot of the application is recorded.
            String result = getApplicationTransactionsProfile(GROUP_ID, ARTIFACT_ID, null, null);

            // then.
            assertThat(result)
                    .isEqualTo("No application found with groupId 'com.example' and artifactId 'orders-service'.");
        }

        @Test
        void shouldReturnMessageWhenNoTransactionsRecorded() {
            // given.
            stubApplication(new PersistenceInsights().transactions(null));

            // when.
            String result = getApplicationTransactionsProfile(GROUP_ID, ARTIFACT_ID, null, null);

            // then.
            assertThat(result)
                    .isEqualTo(
                            "No transactions profile has been recorded for application 'com.example:orders-service'.");
        }
    }

    private void stubApplication(PersistenceInsights persistenceInsights) {
        jdbcAggregateTemplate.insert(snapshot(persistenceInsights));
    }

    private String getApplicationTransactionsProfile(
            String groupId, String artifactId, @Nullable String className, @Nullable String methodName) {
        Map<String, Object> arguments = new HashMap<>(Map.of("groupId", groupId, "artifactId", artifactId));
        if (className != null) {
            arguments.put("className", className);
        }
        if (methodName != null) {
            arguments.put("methodName", methodName);
        }
        return callTool(McpEndpoints.TRANSACTIONS_PROFILE_TOOL_NAME, arguments);
    }

    private static TransactionAggregatedProfile profile(
            String className,
            String methodName,
            List<CountedLazyLoadingTarget> lazyLoadingTargets,
            Map<String, Integer> inMemoryPagination) {
        return new TransactionAggregatedProfile()
                .transactionOrigin(TransactionOrigin.APPLICATION_DECLARATIVE)
                .transactionalKey(new TransactionalKey().className(className).methodName(methodName))
                .transactionOverallStats(
                        new ExecutionStats().minMs(1L).maxMs(10L).averageMs(5L))
                .lazyLoadingTargets(lazyLoadingTargets)
                .inMemoryPagination(inMemoryPagination)
                .externalCalls(List.of())
                .propagation("REQUIRED")
                .isolation("DEFAULT")
                .readOnly(false);
    }

    private static CountedLazyLoadingTarget nPlusOne(String associationPropertyName, int count) {
        return new CountedLazyLoadingTarget()
                .target(new LazyLoadingTarget()
                        .ownerEntityClass("com.example.Order")
                        .associationPropertyName(associationPropertyName))
                .count(count);
    }

    private static HistoricalApplicationSnapshot snapshot(PersistenceInsights persistenceInsights) {
        return new HistoricalApplicationSnapshot(
                new SnapshotId(GROUP_ID, ARTIFACT_ID, LocalDate.now(ZoneOffset.UTC)),
                new Insights(
                        new Insights.HotSpot(
                                new Insights.HotSpot.ProjectLeyden(false, false),
                                new Insights.HotSpot.GarbageCollector(false, GarbageCollector.G1),
                                new Insights.HotSpot.ProjectLilliput(false)),
                        new Insights.SpringFramework(false),
                        persistenceInsights),
                new HistoricalApplicationSnapshot.Versions(
                        "1.0.0", "3.5.0", "6.2.0", new JavaVersion(21, 0, 0, 0), JdkVendor.ADOPTIUM, null));
    }
}
