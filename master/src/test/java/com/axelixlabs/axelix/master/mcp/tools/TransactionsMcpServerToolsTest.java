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
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import com.axelixlabs.axelix.common.api.LazyLoadingTarget;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.CountedLazyLoadingTarget;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.ExecutionStats;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.PersistenceInsights;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.TransactionAggregatedProfile;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.TransactionOrigin;
import com.axelixlabs.axelix.common.api.registration.insights.persistence.TransactionalKey;
import com.axelixlabs.axelix.common.domain.insights.GarbageCollector;
import com.axelixlabs.axelix.master.domain.ApplicationId;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot.SnapshotId;
import com.axelixlabs.axelix.master.domain.Insights;
import com.axelixlabs.axelix.master.service.state.DatabaseHistoricalApplicationSnapshotService;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TransactionsMcpServerTools}.
 *
 * @author Mikhail Polivakha
 */
class TransactionsMcpServerToolsTest {

    private static final String GROUP_ID = "com.example";
    private static final String ARTIFACT_ID = "orders-service";
    private static final String CLASS_NAME = "com.example.OrderService";
    private static final String METHOD_NAME = "placeOrder";

    private final DatabaseHistoricalApplicationSnapshotService snapshotService =
            mock(DatabaseHistoricalApplicationSnapshotService.class);

    private final TransactionsMcpServerTools subject =
            new TransactionsMcpServerTools(new JsonMapper(), snapshotService);

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
            stubApplication(
                    new PersistenceInsights(List.of(nPlusOneTransaction, paginationTransaction, cleanTransaction)));

            // when.
            String result = subject.getApplicationTransactionsProfile(GROUP_ID, ARTIFACT_ID, null, null);

            // then.
            assertThatJson(result).isArray().hasSize(2);
            assertThat(result).contains(METHOD_NAME).contains("loadOwners").doesNotContain("buildReport");
            assertThat(result).doesNotContain("\"count\"");
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
            stubApplication(new PersistenceInsights(List.of(placeOrder, other)));

            // when.
            String result = subject.getApplicationTransactionsProfile(GROUP_ID, ARTIFACT_ID, CLASS_NAME, METHOD_NAME);

            // then.
            assertThatJson(result).isArray().hasSize(1);
            assertThatJson(result).node("[0].transactionalKey.className").isEqualTo(CLASS_NAME);
            assertThatJson(result).node("[0].transactionalKey.methodName").isEqualTo(METHOD_NAME);
            assertThatJson(result).node("[0].nPlusOne").isArray().hasSize(1);
            assertThatJson(result)
                    .node("[0].nPlusOne[0].associationPropertyName")
                    .isEqualTo("items");
            assertThatJson(result).node("[0].nPlusOne[0].ownerEntityClass").isEqualTo("com.example.Order");
            assertThatJson(result)
                    .node("[0].inMemoryPaginatedEntities")
                    .isArray()
                    .containsExactly("orders");
            assertThat(result).doesNotContain("\"count\"");
        }

        @Test
        void shouldExcludeLazyLoadingTargetsLoadedOnlyOnce() {
            // given. an association lazily loaded exactly once is legitimate and is not an N+1.
            TransactionAggregatedProfile placeOrder =
                    profile(CLASS_NAME, METHOD_NAME, List.of(nPlusOne("items", 1)), Map.of());
            stubApplication(new PersistenceInsights(List.of(placeOrder)));

            // when.
            String result = subject.getApplicationTransactionsProfile(GROUP_ID, ARTIFACT_ID, CLASS_NAME, METHOD_NAME);

            // then.
            assertThatJson(result).isArray().hasSize(1);
            assertThatJson(result).node("[0].nPlusOne").isArray().isEmpty();
        }

        @Test
        void shouldReturnMessageWhenNoMatchingTransaction() {
            // given.
            stubApplication(new PersistenceInsights(
                    List.of(profile("com.example.OwnerService", "loadOwners", List.of(), Map.of()))));

            // when.
            String result = subject.getApplicationTransactionsProfile(GROUP_ID, ARTIFACT_ID, CLASS_NAME, METHOD_NAME);

            // then.
            assertThat(result).contains("No transaction found for method");
        }

        @Test
        void shouldReturnMessageWhenOnlyMethodNameProvided() {
            // given.
            stubApplication(new PersistenceInsights(List.of()));

            // when.
            String result = subject.getApplicationTransactionsProfile(GROUP_ID, ARTIFACT_ID, null, METHOD_NAME);

            // then.
            assertThat(result).contains("'methodName' can only be provided together with 'className'");
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
            stubApplication(new PersistenceInsights(List.of(placeOrder, cancelOrder, cleanOrderMethod, otherClass)));

            // when.
            String result = subject.getApplicationTransactionsProfile(GROUP_ID, ARTIFACT_ID, CLASS_NAME, null);

            // then.
            assertThatJson(result).isArray().hasSize(2);
            assertThat(result)
                    .contains(METHOD_NAME)
                    .contains("cancelOrder")
                    .doesNotContain("findOrder")
                    .doesNotContain("loadOwners");
        }
    }

    @Nested
    class ApplicationLookup {

        @Test
        void shouldReturnMessageWhenApplicationNotFound() {
            // given.
            when(snapshotService.getCurrentRecord(any())).thenReturn(null);

            // when.
            String result = subject.getApplicationTransactionsProfile(GROUP_ID, ARTIFACT_ID, null, null);

            // then.
            assertThat(result).contains("No application found");
        }

        @Test
        void shouldReturnMessageWhenNoTransactionsRecorded() {
            // given.
            stubApplication(new PersistenceInsights(null));

            // when.
            String result = subject.getApplicationTransactionsProfile(GROUP_ID, ARTIFACT_ID, null, null);

            // then.
            assertThat(result).contains("No transactions profile");
        }
    }

    private void stubApplication(PersistenceInsights persistenceInsights) {
        when(snapshotService.getCurrentRecord(ApplicationId.of(GROUP_ID, ARTIFACT_ID)))
                .thenReturn(snapshot(persistenceInsights));
    }

    private static TransactionAggregatedProfile profile(
            String className,
            String methodName,
            List<CountedLazyLoadingTarget> lazyLoadingTargets,
            Map<String, Integer> inMemoryPagination) {
        return new TransactionAggregatedProfile(
                TransactionOrigin.APPLICATION_DECLARATIVE,
                new TransactionalKey(className, methodName),
                new ExecutionStats(1, 10, 5),
                lazyLoadingTargets,
                inMemoryPagination,
                List.of(),
                "REQUIRED",
                "DEFAULT",
                false);
    }

    private static CountedLazyLoadingTarget nPlusOne(String associationPropertyName, int count) {
        return new CountedLazyLoadingTarget(new LazyLoadingTarget("com.example.Order", associationPropertyName), count);
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
                        persistenceInsights));
    }
}
