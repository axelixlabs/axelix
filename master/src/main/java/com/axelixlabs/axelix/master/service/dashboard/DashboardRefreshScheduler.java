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
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;

import com.axelixlabs.axelix.master.service.ecosystem.DependencyAnalysisService;
import com.axelixlabs.axelix.master.service.ecosystem.LanguagesProfileService;
import com.axelixlabs.axelix.master.service.ecosystem.SpringPortfolioService;
import com.axelixlabs.axelix.master.service.state.DatabaseHistoricalApplicationSnapshotService;

/**
 * Reloads every dashboard cache on {@code axelix.master.dashboard.refresh-schedule}.
 *
 * <p>Computes replacements before updating the caches, so requests can keep reading the previous results
 * during a refresh or after a failed computation.
 *
 * @author Vyacheslav Yanin
 */
public class DashboardRefreshScheduler {

    private static final Logger log = LoggerFactory.getLogger(DashboardRefreshScheduler.class);

    private final SpringPortfolioService springPortfolioService;
    private final LanguagesProfileService languagesProfileService;
    private final DatabaseHistoricalApplicationSnapshotService snapshotService;
    private final DependencyAnalysisService dependencyAnalysisService;
    private final CacheManager cacheManager;

    public DashboardRefreshScheduler(
            SpringPortfolioService springPortfolioService,
            LanguagesProfileService languagesProfileService,
            DatabaseHistoricalApplicationSnapshotService snapshotService,
            DependencyAnalysisService dependencyAnalysisService,
            CacheManager cacheManager) {
        this.springPortfolioService = springPortfolioService;
        this.languagesProfileService = languagesProfileService;
        this.snapshotService = snapshotService;
        this.dependencyAnalysisService = dependencyAnalysisService;
        this.cacheManager = cacheManager;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "${axelix.master.dashboard.refresh-schedule}")
    public void refresh() {
        reload(DashboardCaches.PERSISTENCE, snapshotService::getPersistenceDashboard);
        reload(DashboardCaches.DEPENDENCIES, dependencyAnalysisService::getDashboard);
        reload(DashboardCaches.SPRING_PORTFOLIO, springPortfolioService::getSpringPortfolio);
        reload(DashboardCaches.LANGUAGES, languagesProfileService::getLanguagesProfile);
    }

    private void reload(String cacheName, Supplier<?> load) {
        try {
            Cache cache = Objects.requireNonNull(cacheManager.getCache(cacheName));
            cache.put(SimpleKey.EMPTY, load.get());
        } catch (RuntimeException exception) {
            log.warn("Failed to refresh the {} dashboard cache", cacheName, exception);
        }
    }
}
