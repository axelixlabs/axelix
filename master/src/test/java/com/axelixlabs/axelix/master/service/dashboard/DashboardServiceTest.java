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

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axelixlabs.axelix.master.api.external.response.dashboard.DependenciesDashboardResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.LanguagesProfileResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.PersistenceDashboardResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.PersistenceDashboardResponse.TreemapEntry;
import com.axelixlabs.axelix.master.api.external.response.dashboard.SpringPortfolioResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.SpringPortfolioResponse.PlatformDistribution;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformName;
import com.axelixlabs.axelix.master.service.ecosystem.DependencyAnalysisService;
import com.axelixlabs.axelix.master.service.ecosystem.LanguagesProfileService;
import com.axelixlabs.axelix.master.service.ecosystem.SpringPortfolioService;
import com.axelixlabs.axelix.master.service.state.DatabaseHistoricalApplicationSnapshotService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DashboardService}.
 *
 * @author Vyacheslav Yanin
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    private static final Instant COMPUTED_AT = Instant.parse("2026-01-01T00:00:00Z");

    @Mock
    private SpringPortfolioService springPortfolioService;

    @Mock
    private LanguagesProfileService languagesProfileService;

    @Mock
    private DatabaseHistoricalApplicationSnapshotService snapshotService;

    @Mock
    private DependencyAnalysisService dependencyAnalysisService;

    private DashboardService subject;

    @BeforeEach
    void setUp() {
        subject = new DashboardService(
                new InMemoryMapDashboardCache(),
                springPortfolioService,
                languagesProfileService,
                snapshotService,
                dependencyAnalysisService);
    }

    @Test
    void shouldServePersistenceDashboardFromCache() {
        // given.
        PersistenceDashboardResponse response = persistenceResponse("service-a");
        when(snapshotService.getPersistenceDashboard()).thenReturn(response);

        // when.
        PersistenceDashboardResponse first = subject.getPersistenceDashboard();
        PersistenceDashboardResponse second = subject.getPersistenceDashboard();

        // then.
        assertThat(first).isSameAs(response);
        assertThat(second).isSameAs(response);
        verify(snapshotService, times(1)).getPersistenceDashboard();
    }

    @Test
    void shouldServeDependenciesDashboardFromCache() {
        // given.
        DependenciesDashboardResponse response = dependenciesResponse();
        when(dependencyAnalysisService.getDashboard()).thenReturn(response);

        // when.
        DependenciesDashboardResponse first = subject.getDependenciesDashboard();
        DependenciesDashboardResponse second = subject.getDependenciesDashboard();

        // then.
        assertThat(first).isSameAs(response);
        assertThat(second).isSameAs(response);
        verify(dependencyAnalysisService, times(1)).getDashboard();
    }

    @Test
    void shouldServeSpringPortfolioFromCache() {
        // given.
        SpringPortfolioResponse response = portfolioResponse();
        when(springPortfolioService.getSpringPortfolio()).thenReturn(response);

        // when.
        SpringPortfolioResponse first = subject.getSpringPortfolio();
        SpringPortfolioResponse second = subject.getSpringPortfolio();

        // then.
        assertThat(first).isSameAs(response);
        assertThat(second).isSameAs(response);
        verify(springPortfolioService, times(1)).getSpringPortfolio();
    }

    @Test
    void shouldServeLanguagesProfileFromCache() {
        // given.
        LanguagesProfileResponse response = languagesResponse();
        when(languagesProfileService.getLanguagesProfile()).thenReturn(response);

        // when.
        LanguagesProfileResponse first = subject.getLanguagesProfile();
        LanguagesProfileResponse second = subject.getLanguagesProfile();

        // then.
        assertThat(first).isSameAs(response);
        assertThat(second).isSameAs(response);
        verify(languagesProfileService, times(1)).getLanguagesProfile();
    }

    @Test
    void shouldRefreshAllDashboards() {
        // given.
        PersistenceDashboardResponse persistence = persistenceResponse("service-a");
        DependenciesDashboardResponse dependencies = dependenciesResponse();
        SpringPortfolioResponse portfolio = portfolioResponse();
        LanguagesProfileResponse languages = languagesResponse();

        when(snapshotService.getPersistenceDashboard()).thenReturn(persistence);
        when(dependencyAnalysisService.getDashboard()).thenReturn(dependencies);
        when(springPortfolioService.getSpringPortfolio()).thenReturn(portfolio);
        when(languagesProfileService.getLanguagesProfile()).thenReturn(languages);

        // when.
        subject.refreshAll();

        // then.
        assertThat(subject.getPersistenceDashboard()).isSameAs(persistence);
        assertThat(subject.getDependenciesDashboard()).isSameAs(dependencies);
        assertThat(subject.getSpringPortfolio()).isSameAs(portfolio);
        assertThat(subject.getLanguagesProfile()).isSameAs(languages);

        verify(snapshotService, times(1)).getPersistenceDashboard();
        verify(dependencyAnalysisService, times(1)).getDashboard();
        verify(springPortfolioService, times(1)).getSpringPortfolio();
        verify(languagesProfileService, times(1)).getLanguagesProfile();
    }

    @Test
    void shouldServeTheSameValueUntilRefreshed() {
        // given.
        PersistenceDashboardResponse first = persistenceResponse("service-a");
        PersistenceDashboardResponse second = persistenceResponse("service-b");

        when(snapshotService.getPersistenceDashboard()).thenReturn(first, second);
        when(dependencyAnalysisService.getDashboard()).thenReturn(dependenciesResponse());
        when(springPortfolioService.getSpringPortfolio()).thenReturn(portfolioResponse());
        when(languagesProfileService.getLanguagesProfile()).thenReturn(languagesResponse());

        // when.
        assertThat(subject.getPersistenceDashboard()).isSameAs(first);
        assertThat(subject.getPersistenceDashboard()).isSameAs(first);

        subject.refreshAll();

        // then.
        assertThat(subject.getPersistenceDashboard()).isSameAs(second);
        verify(snapshotService, times(2)).getPersistenceDashboard();
    }

    private static PersistenceDashboardResponse persistenceResponse(String appName) {
        return new PersistenceDashboardResponse(
                List.of(new TreemapEntry(appName, 1)), List.of(), COMPUTED_AT);
    }

    private static DependenciesDashboardResponse dependenciesResponse() {
        return new DependenciesDashboardResponse(0, 0, Map.of(), 0, Map.of(), Map.of(), COMPUTED_AT);
    }

    private static SpringPortfolioResponse portfolioResponse() {
        return new SpringPortfolioResponse(
                0,
                0,
                new PlatformDistribution(PlatformName.SPRING_BOOT, 0, 0, List.of()),
                new PlatformDistribution(PlatformName.SPRING_FRAMEWORK, 0, 0, List.of()),
                List.of(),
                COMPUTED_AT);
    }

    private static LanguagesProfileResponse languagesResponse() {
        return new LanguagesProfileResponse(0, 0, List.of(), List.of(), null, List.of(), COMPUTED_AT);
    }
}
