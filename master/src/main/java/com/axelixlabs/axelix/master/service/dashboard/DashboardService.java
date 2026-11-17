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

import org.springframework.cache.annotation.Cacheable;

import com.axelixlabs.axelix.master.api.external.response.dashboard.DependenciesDashboardResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.LanguagesProfileResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.PersistenceDashboardResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.SpringPortfolioResponse;
import com.axelixlabs.axelix.master.service.ecosystem.DependencyAnalysisService;
import com.axelixlabs.axelix.master.service.ecosystem.LanguagesProfileService;
import com.axelixlabs.axelix.master.service.ecosystem.SpringPortfolioService;
import com.axelixlabs.axelix.master.service.state.DatabaseHistoricalApplicationSnapshotService;

/**
 * Serves cached dashboard snapshots, computing missing snapshots on demand.
 * {@link DashboardRefreshScheduler} refreshes the caches on a schedule.
 *
 * @author Vyacheslav Yanin
 */
public class DashboardService {

    private final SpringPortfolioService springPortfolioService;
    private final LanguagesProfileService languagesProfileService;
    private final DatabaseHistoricalApplicationSnapshotService snapshotService;
    private final DependencyAnalysisService dependencyAnalysisService;

    public DashboardService(
            SpringPortfolioService springPortfolioService,
            LanguagesProfileService languagesProfileService,
            DatabaseHistoricalApplicationSnapshotService snapshotService,
            DependencyAnalysisService dependencyAnalysisService) {
        this.springPortfolioService = springPortfolioService;
        this.languagesProfileService = languagesProfileService;
        this.snapshotService = snapshotService;
        this.dependencyAnalysisService = dependencyAnalysisService;
    }

    @Cacheable(cacheNames = DashboardCaches.PERSISTENCE, sync = true)
    public PersistenceDashboardResponse getPersistenceDashboard() {
        return snapshotService.getPersistenceDashboard();
    }

    @Cacheable(cacheNames = DashboardCaches.DEPENDENCIES, sync = true)
    public DependenciesDashboardResponse getDependenciesDashboard() {
        return dependencyAnalysisService.getDashboard();
    }

    @Cacheable(cacheNames = DashboardCaches.SPRING_PORTFOLIO, sync = true)
    public SpringPortfolioResponse getSpringPortfolio() {
        return springPortfolioService.getSpringPortfolio();
    }

    @Cacheable(cacheNames = DashboardCaches.LANGUAGES, sync = true)
    public LanguagesProfileResponse getLanguagesProfile() {
        return languagesProfileService.getLanguagesProfile();
    }
}
