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
package com.axelixlabs.axelix.master.service.dashboard;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import com.axelixlabs.axelix.master.api.external.response.dashboard.DependenciesDashboardResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.PersistenceDashboardResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.PersistenceDashboardResponse.TreemapEntry;
import com.axelixlabs.axelix.master.contract.metadata.PersistenceInsights;
import com.axelixlabs.axelix.master.contract.metadata.TransactionAggregatedProfile;
import com.axelixlabs.axelix.master.domain.ApplicationDependencies;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.instance.Instance;
import com.axelixlabs.axelix.master.service.state.DatabaseHistoricalApplicationSnapshotService;
import com.axelixlabs.axelix.master.utils.TestMetadataFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;

/**
 * Integration tests for {@link DashboardRefreshScheduler} with real persistence, dashboard aggregators, and caching.
 *
 * @author Vyacheslav Yanin
 */
@SpringBootTest
class DashboardRefreshSchedulerTest {

    @Autowired
    private DashboardRefreshScheduler subject;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    @MockitoSpyBean
    private DatabaseHistoricalApplicationSnapshotService snapshotService;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        jdbcAggregateTemplate.deleteAll(Instance.class);
        jdbcAggregateTemplate.deleteAll(HistoricalApplicationSnapshot.class);
        jdbcAggregateTemplate.deleteAll(ApplicationDependencies.class);
        for (String name : cacheManager.getCacheNames()) {
            Objects.requireNonNull(cacheManager.getCache(name)).clear();
        }
    }

    @Test // GH-1844
    void shouldKeepServingPreviousResultDuringRefresh() {
        // given.
        subject.refresh();
        PersistenceDashboardResponse previous = dashboardService.getPersistenceDashboard();
        persistApplication();
        AtomicReference<PersistenceDashboardResponse> servedDuringRefresh = new AtomicReference<>();
        doAnswer(invocation -> {
                    servedDuringRefresh.set(dashboardService.getPersistenceDashboard());
                    return invocation.callRealMethod();
                })
                .when(snapshotService)
                .getPersistenceDashboard();

        // when.
        subject.refresh();

        // then.
        assertThat(servedDuringRefresh.get()).isSameAs(previous);
        PersistenceDashboardResponse refreshed = dashboardService.getPersistenceDashboard();
        assertThat(refreshed).isNotSameAs(previous);
        assertThat(refreshed.inMemoryPagination()).containsExactly(new TreemapEntry("service-a", 1));
        assertThat(dashboardService.getPersistenceDashboard()).isSameAs(refreshed);
    }

    @Test // GH-1844
    void shouldRetainPreviousResultAndRefreshOtherDashboardsWhenComputationFails() {
        // given.
        subject.refresh();
        PersistenceDashboardResponse previous = dashboardService.getPersistenceDashboard();
        DependenciesDashboardResponse previousDependencies = dashboardService.getDependenciesDashboard();
        persistApplication();
        doThrow(IllegalStateException.class).when(snapshotService).getPersistenceDashboard();

        // when.
        subject.refresh();

        // then.
        assertThat(dashboardService.getPersistenceDashboard()).isSameAs(previous);
        assertThat(dashboardService.getDependenciesDashboard()).isNotSameAs(previousDependencies);
        assertThat(dashboardService.getSpringPortfolio().applicationsTotal()).isEqualTo(1);
        assertThat(dashboardService.getLanguagesProfile().applicationsOnLts()
                        + dashboardService.getLanguagesProfile().applicationsOnNonLts())
                .isEqualTo(1);
    }

    private void persistApplication() {
        TransactionAggregatedProfile transaction = new TransactionAggregatedProfile()
                .lazyLoadingTargets(List.of())
                .inMemoryPagination(Map.of("owners", 3));
        snapshotService.reloadCurrentStateBulk(List.of(TestMetadataFactory.withPersistenceInsights(
                "com.example", "service-a", new PersistenceInsights().transactions(List.of(transaction)))));
    }
}
