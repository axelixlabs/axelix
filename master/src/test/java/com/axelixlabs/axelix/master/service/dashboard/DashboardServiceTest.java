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

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DashboardService} and {@link DashboardRefreshScheduler}.
 *
 * @author Vyacheslav Yanin
 */
@SpringJUnitConfig(DashboardServiceTest.Config.class)
class DashboardServiceTest {

    private static final Instant COMPUTED_AT = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private DashboardService subject;

    @Autowired
    private DashboardRefreshScheduler scheduler;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private SpringPortfolioService springPortfolioService;

    @Autowired
    private LanguagesProfileService languagesProfileService;

    @Autowired
    private DatabaseHistoricalApplicationSnapshotService snapshotService;

    @Autowired
    private DependencyAnalysisService dependencyAnalysisService;

    @BeforeEach
    void setUp() {
        reset(springPortfolioService, languagesProfileService, snapshotService, dependencyAnalysisService);
        for (String name : cacheManager.getCacheNames()) {
            org.springframework.cache.Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        }
    }

    @Test // GH-1844
    void shouldLoadMissingDashboardOnRequestAfterInitialRefreshFails() {
        // given.
        PersistenceDashboardResponse persistence = persistenceResponse("service-a");
        when(snapshotService.getPersistenceDashboard())
                .thenThrow(IllegalStateException.class)
                .thenReturn(persistence);
        when(dependencyAnalysisService.getDashboard()).thenReturn(dependenciesResponse());
        when(springPortfolioService.getSpringPortfolio()).thenReturn(portfolioResponse());
        when(languagesProfileService.getLanguagesProfile()).thenReturn(languagesResponse());

        // when.
        context.publishEvent(
                new ApplicationReadyEvent(new SpringApplication(Config.class), new String[0], context, Duration.ZERO));

        // then.
        assertThat(subject.getPersistenceDashboard()).isSameAs(persistence);
        verify(snapshotService, times(2)).getPersistenceDashboard();
    }

    @Test // GH-1844
    void shouldLoadAndCacheMissingDashboardsOnRequest() {
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
        subject.getPersistenceDashboard();
        subject.getDependenciesDashboard();
        subject.getSpringPortfolio();
        subject.getLanguagesProfile();

        // then.
        assertThat(subject.getPersistenceDashboard()).isSameAs(persistence);
        assertThat(subject.getDependenciesDashboard()).isSameAs(dependencies);
        assertThat(subject.getSpringPortfolio()).isSameAs(portfolio);
        assertThat(subject.getLanguagesProfile()).isSameAs(languages);
        verify(snapshotService).getPersistenceDashboard();
        verify(dependencyAnalysisService).getDashboard();
        verify(springPortfolioService).getSpringPortfolio();
        verify(languagesProfileService).getLanguagesProfile();
    }

    @Test // GH-1844
    void shouldRetryLoaderOnNextRequestAfterFailure() {
        // given.
        PersistenceDashboardResponse persistence = persistenceResponse("service-a");
        when(snapshotService.getPersistenceDashboard())
                .thenThrow(IllegalStateException.class)
                .thenReturn(persistence);
        assertThatThrownBy(subject::getPersistenceDashboard).isInstanceOf(IllegalStateException.class);

        // when.
        PersistenceDashboardResponse result = subject.getPersistenceDashboard();

        // then.
        assertThat(result).isSameAs(persistence);
        assertThat(subject.getPersistenceDashboard()).isSameAs(persistence);
        verify(snapshotService, times(2)).getPersistenceDashboard();
    }

    @Test
    void shouldServeEachDashboardFromItsCache() {
        // given.
        PersistenceDashboardResponse persistence = persistenceResponse("service-a");
        DependenciesDashboardResponse dependencies = dependenciesResponse();
        SpringPortfolioResponse portfolio = portfolioResponse();
        LanguagesProfileResponse languages = languagesResponse();
        when(snapshotService.getPersistenceDashboard()).thenReturn(persistence);
        when(dependencyAnalysisService.getDashboard()).thenReturn(dependencies);
        when(springPortfolioService.getSpringPortfolio()).thenReturn(portfolio);
        when(languagesProfileService.getLanguagesProfile()).thenReturn(languages);

        scheduler.refresh();

        // when.
        PersistenceDashboardResponse firstPersistence = subject.getPersistenceDashboard();
        PersistenceDashboardResponse secondPersistence = subject.getPersistenceDashboard();
        DependenciesDashboardResponse firstDependencies = subject.getDependenciesDashboard();
        DependenciesDashboardResponse secondDependencies = subject.getDependenciesDashboard();
        SpringPortfolioResponse firstPortfolio = subject.getSpringPortfolio();
        SpringPortfolioResponse secondPortfolio = subject.getSpringPortfolio();
        LanguagesProfileResponse firstLanguages = subject.getLanguagesProfile();
        LanguagesProfileResponse secondLanguages = subject.getLanguagesProfile();

        // then.
        assertThat(firstPersistence).isSameAs(persistence);
        assertThat(secondPersistence).isSameAs(persistence);
        assertThat(firstDependencies).isSameAs(dependencies);
        assertThat(secondDependencies).isSameAs(dependencies);
        assertThat(firstPortfolio).isSameAs(portfolio);
        assertThat(secondPortfolio).isSameAs(portfolio);
        assertThat(firstLanguages).isSameAs(languages);
        assertThat(secondLanguages).isSameAs(languages);
        verify(snapshotService, times(1)).getPersistenceDashboard();
        verify(dependencyAnalysisService, times(1)).getDashboard();
        verify(springPortfolioService, times(1)).getSpringPortfolio();
        verify(languagesProfileService, times(1)).getLanguagesProfile();
    }

    @Test
    void shouldReplaceCachedDashboardsOnRefresh() {
        // given.
        PersistenceDashboardResponse first = persistenceResponse("service-a");
        PersistenceDashboardResponse second = persistenceResponse("service-b");
        when(snapshotService.getPersistenceDashboard()).thenReturn(first, second);
        when(dependencyAnalysisService.getDashboard()).thenReturn(dependenciesResponse());
        when(springPortfolioService.getSpringPortfolio()).thenReturn(portfolioResponse());
        when(languagesProfileService.getLanguagesProfile()).thenReturn(languagesResponse());
        scheduler.refresh();

        // when.
        scheduler.refresh();

        // then.
        assertThat(subject.getPersistenceDashboard()).isSameAs(second);
        verify(snapshotService, times(2)).getPersistenceDashboard();
        verify(dependencyAnalysisService, times(2)).getDashboard();
        verify(springPortfolioService, times(2)).getSpringPortfolio();
        verify(languagesProfileService, times(2)).getLanguagesProfile();
    }

    @Test
    void shouldRefreshTheOtherDashboardsWhenOneReloadFails() {
        // given.
        DependenciesDashboardResponse dependencies = dependenciesResponse();
        when(snapshotService.getPersistenceDashboard()).thenThrow(IllegalStateException.class);
        when(dependencyAnalysisService.getDashboard()).thenReturn(dependencies);
        when(springPortfolioService.getSpringPortfolio()).thenReturn(portfolioResponse());
        when(languagesProfileService.getLanguagesProfile()).thenReturn(languagesResponse());

        // when.
        scheduler.refresh();

        // then.
        assertThat(subject.getDependenciesDashboard()).isSameAs(dependencies);
        verify(dependencyAnalysisService, times(1)).getDashboard();
        verify(springPortfolioService, times(1)).getSpringPortfolio();
        verify(languagesProfileService, times(1)).getLanguagesProfile();
    }

    @ParameterizedTest // GH-1844
    @ValueSource(
            strings = {
                DashboardCaches.PERSISTENCE,
                DashboardCaches.DEPENDENCIES,
                DashboardCaches.SPRING_PORTFOLIO,
                DashboardCaches.LANGUAGES
            })
    void shouldLimitEachDashboardCacheToASingleEntry(String cacheName) {
        // given.
        org.springframework.cache.Cache cache = Objects.requireNonNull(cacheManager.getCache(cacheName));

        // when.
        for (int i = 0; i < 20; i++) {
            cache.put("key-" + i, "value-" + i);
        }

        // then.
        assertThat(nativeCache(cache).asMap()).hasSize(1);
    }

    @SuppressWarnings("unchecked")
    private static Cache<Object, Object> nativeCache(org.springframework.cache.Cache cache) {
        return (Cache<Object, Object>) cache.getNativeCache();
    }

    private static PersistenceDashboardResponse persistenceResponse(String appName) {
        return new PersistenceDashboardResponse(List.of(new TreemapEntry(appName, 1)), List.of(), COMPUTED_AT);
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

    @Configuration
    @EnableCaching
    static class Config {

        @Bean
        CacheManager cacheManager() {
            return DashboardCaches.cacheManager();
        }

        @Bean
        SpringPortfolioService springPortfolioService() {
            return mock(SpringPortfolioService.class);
        }

        @Bean
        LanguagesProfileService languagesProfileService() {
            return mock(LanguagesProfileService.class);
        }

        @Bean
        DatabaseHistoricalApplicationSnapshotService snapshotService() {
            return mock(DatabaseHistoricalApplicationSnapshotService.class);
        }

        @Bean
        DependencyAnalysisService dependencyAnalysisService() {
            return mock(DependencyAnalysisService.class);
        }

        @Bean
        DashboardService dashboardService(
                SpringPortfolioService springPortfolioService,
                LanguagesProfileService languagesProfileService,
                DatabaseHistoricalApplicationSnapshotService snapshotService,
                DependencyAnalysisService dependencyAnalysisService) {
            return new DashboardService(
                    springPortfolioService, languagesProfileService, snapshotService, dependencyAnalysisService);
        }

        @Bean
        DashboardRefreshScheduler dashboardRefreshScheduler(
                SpringPortfolioService springPortfolioService,
                LanguagesProfileService languagesProfileService,
                DatabaseHistoricalApplicationSnapshotService snapshotService,
                DependencyAnalysisService dependencyAnalysisService,
                CacheManager cacheManager) {
            return new DashboardRefreshScheduler(
                    springPortfolioService,
                    languagesProfileService,
                    snapshotService,
                    dependencyAnalysisService,
                    cacheManager);
        }
    }
}
