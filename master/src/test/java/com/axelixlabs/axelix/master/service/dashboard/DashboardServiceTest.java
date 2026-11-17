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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

import com.axelixlabs.axelix.master.contract.metadata.PersistenceInsights;
import com.axelixlabs.axelix.master.contract.metadata.TransactionAggregatedProfile;
import com.axelixlabs.axelix.master.domain.ApplicationDependencies;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.instance.Instance;
import com.axelixlabs.axelix.master.service.ecosystem.DependencyAnalysisService;
import com.axelixlabs.axelix.master.service.ecosystem.LanguagesProfileService;
import com.axelixlabs.axelix.master.service.ecosystem.SpringPortfolioService;
import com.axelixlabs.axelix.master.service.state.DatabaseHistoricalApplicationSnapshotService;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.CycloneDxDocuments;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;
import com.axelixlabs.axelix.master.utils.TestMetadataFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DashboardService} with real dashboard aggregators, persistence, and caching.
 *
 * @author Vyacheslav Yanin
 */
@SpringBootTest
class DashboardServiceTest {

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

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    @Autowired
    private InstanceRegistry registry;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        deleteApplications();
        for (String name : cacheManager.getCacheNames()) {
            Objects.requireNonNull(cacheManager.getCache(name)).clear();
        }
    }

    @ParameterizedTest // GH-1844
    @ValueSource(
            strings = {
                DashboardCaches.PERSISTENCE,
                DashboardCaches.DEPENDENCIES,
                DashboardCaches.SPRING_PORTFOLIO,
                DashboardCaches.LANGUAGES
            })
    void shouldLoadAndCacheMissingDashboardFromPersistedApplications(String dashboard) {
        // given.
        persistApplication();
        Object expected = computeDashboard(dashboard);

        // when.
        Object result = readDashboard(dashboard);

        // then.
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("computedAt")
                .isEqualTo(expected);
        assertThat(readDashboard(dashboard)).isSameAs(result);
    }

    @ParameterizedTest // GH-1844
    @ValueSource(
            strings = {
                DashboardCaches.PERSISTENCE,
                DashboardCaches.DEPENDENCIES,
                DashboardCaches.SPRING_PORTFOLIO,
                DashboardCaches.LANGUAGES
            })
    void shouldReplaceCachedDashboardWithCurrentPersistedDataOnRefresh(String dashboard) {
        // given.
        persistApplication();
        scheduler.refresh();
        Object previous = readDashboard(dashboard);
        deleteApplications();
        Object expected = computeDashboard(dashboard);

        // when.
        scheduler.refresh();
        Object result = readDashboard(dashboard);

        // then.
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("computedAt")
                .isEqualTo(expected);
        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("computedAt")
                .isNotEqualTo(previous);
        assertThat(readDashboard(dashboard)).isSameAs(result);
    }

    @ParameterizedTest // GH-1844
    @ValueSource(
            strings = {
                DashboardCaches.PERSISTENCE,
                DashboardCaches.DEPENDENCIES,
                DashboardCaches.SPRING_PORTFOLIO,
                DashboardCaches.LANGUAGES
            })
    void shouldPopulateDashboardCachesOnApplicationReady(String dashboard) {
        // given.
        persistApplication();
        Object expected = computeDashboard(dashboard);

        // when.
        context.publishEvent(new ApplicationReadyEvent(
                new SpringApplication(context.getClass()), new String[0], context, Duration.ZERO));

        // then.
        org.springframework.cache.Cache cache = Objects.requireNonNull(cacheManager.getCache(dashboard));
        Object cached = Objects.requireNonNull(cache.get(SimpleKey.EMPTY)).get();
        assertThat(cached)
                .usingRecursiveComparison()
                .ignoringFields("computedAt")
                .isEqualTo(expected);
        assertThat(readDashboard(dashboard)).isSameAs(cached);
    }

    @ParameterizedTest // GH-1844
    @ValueSource(
            strings = {
                DashboardCaches.PERSISTENCE,
                DashboardCaches.DEPENDENCIES,
                DashboardCaches.SPRING_PORTFOLIO,
                DashboardCaches.LANGUAGES
            })
    void shouldLimitEachDashboardCacheToASingleEntry(String dashboard) {
        // given.
        org.springframework.cache.Cache cache = Objects.requireNonNull(cacheManager.getCache(dashboard));

        // when.
        for (int i = 0; i < 20; i++) {
            cache.put("key-" + i, "value-" + i);
        }

        // then.
        assertThat(((Cache<?, ?>) cache.getNativeCache()).asMap()).hasSize(1);
    }

    private void persistApplication() {
        TransactionAggregatedProfile transaction = new TransactionAggregatedProfile()
                .lazyLoadingTargets(List.of())
                .inMemoryPagination(Map.of("owners", 3));
        snapshotService.reloadCurrentStateBulk(List.of(TestMetadataFactory.withPersistenceInsights(
                "com.example", "service-a", new PersistenceInsights().transactions(List.of(transaction)))));
        Instance instance = TestInstanceFactory.create("dashboard-service-test", "com.example", "service-a");
        registry.register(instance);
        dependencyAnalysisService.store(
                instance,
                CycloneDxDocuments.of("com.example:service-a:1.0.0", "ch.qos.logback:logback-classic:1.5.18"));
    }

    private void deleteApplications() {
        jdbcAggregateTemplate.deleteAll(Instance.class);
        jdbcAggregateTemplate.deleteAll(HistoricalApplicationSnapshot.class);
        jdbcAggregateTemplate.deleteAll(ApplicationDependencies.class);
    }

    private Object readDashboard(String dashboard) {
        return switch (dashboard) {
            case DashboardCaches.PERSISTENCE -> subject.getPersistenceDashboard();
            case DashboardCaches.DEPENDENCIES -> subject.getDependenciesDashboard();
            case DashboardCaches.SPRING_PORTFOLIO -> subject.getSpringPortfolio();
            case DashboardCaches.LANGUAGES -> subject.getLanguagesProfile();
            default -> throw new IllegalArgumentException(dashboard);
        };
    }

    private Object computeDashboard(String dashboard) {
        return switch (dashboard) {
            case DashboardCaches.PERSISTENCE -> snapshotService.getPersistenceDashboard();
            case DashboardCaches.DEPENDENCIES -> dependencyAnalysisService.getDashboard();
            case DashboardCaches.SPRING_PORTFOLIO -> springPortfolioService.getSpringPortfolio();
            case DashboardCaches.LANGUAGES -> languagesProfileService.getLanguagesProfile();
            default -> throw new IllegalArgumentException(dashboard);
        };
    }
}
