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

import java.util.Objects;

import org.junit.jupiter.api.Test;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;

import com.axelixlabs.axelix.master.api.external.response.dashboard.DependenciesDashboardResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.PersistenceDashboardResponse;
import com.axelixlabs.axelix.master.service.ecosystem.DependencyAnalysisService;
import com.axelixlabs.axelix.master.service.ecosystem.LanguagesProfileService;
import com.axelixlabs.axelix.master.service.ecosystem.SpringPortfolioService;
import com.axelixlabs.axelix.master.service.state.DatabaseHistoricalApplicationSnapshotService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DashboardRefreshScheduler}.
 *
 * @author Vyacheslav Yanin
 */
class DashboardRefreshSchedulerTest {

    private final CacheManager cacheManager = DashboardCaches.cacheManager();
    private final DatabaseHistoricalApplicationSnapshotService snapshotService =
            mock(DatabaseHistoricalApplicationSnapshotService.class);
    private final DependencyAnalysisService dependencyAnalysisService = mock(DependencyAnalysisService.class);
    private final DashboardRefreshScheduler subject = new DashboardRefreshScheduler(
            mock(SpringPortfolioService.class),
            mock(LanguagesProfileService.class),
            snapshotService,
            dependencyAnalysisService,
            cacheManager);

    @Test // GH-1844
    void shouldKeepServingPreviousResultDuringRefresh() {
        // given.
        Cache cache = Objects.requireNonNull(cacheManager.getCache(DashboardCaches.PERSISTENCE));
        PersistenceDashboardResponse previous = mock(PersistenceDashboardResponse.class);
        PersistenceDashboardResponse replacement = mock(PersistenceDashboardResponse.class);
        cache.put(SimpleKey.EMPTY, previous);
        when(snapshotService.getPersistenceDashboard()).thenAnswer(invocation -> {
            assertThat(cache.get(SimpleKey.EMPTY, PersistenceDashboardResponse.class))
                    .isSameAs(previous);
            return replacement;
        });

        // when.
        subject.refresh();

        // then.
        assertThat(cache.get(SimpleKey.EMPTY, PersistenceDashboardResponse.class))
                .isSameAs(replacement);
    }

    @Test // GH-1844
    void shouldRetainPreviousResultAndRefreshOtherDashboardsWhenComputationFails() {
        // given.
        Cache persistenceCache = Objects.requireNonNull(cacheManager.getCache(DashboardCaches.PERSISTENCE));
        PersistenceDashboardResponse previous = mock(PersistenceDashboardResponse.class);
        DependenciesDashboardResponse dependencies = mock(DependenciesDashboardResponse.class);
        persistenceCache.put(SimpleKey.EMPTY, previous);
        when(snapshotService.getPersistenceDashboard()).thenThrow(IllegalStateException.class);
        when(dependencyAnalysisService.getDashboard()).thenReturn(dependencies);

        // when.
        subject.refresh();

        // then.
        assertThat(persistenceCache.get(SimpleKey.EMPTY, PersistenceDashboardResponse.class))
                .isSameAs(previous);
        Cache dependenciesCache = Objects.requireNonNull(cacheManager.getCache(DashboardCaches.DEPENDENCIES));
        assertThat(dependenciesCache.get(SimpleKey.EMPTY, DependenciesDashboardResponse.class))
                .isSameAs(dependencies);
    }
}
