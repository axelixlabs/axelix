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

import com.axelixlabs.axelix.master.api.external.response.dashboard.DependenciesDashboardResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.LanguagesProfileResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.PersistenceDashboardResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.SpringPortfolioResponse;
import com.axelixlabs.axelix.master.service.ecosystem.DependencyAnalysisService;
import com.axelixlabs.axelix.master.service.ecosystem.LanguagesProfileService;
import com.axelixlabs.axelix.master.service.ecosystem.SpringPortfolioService;
import com.axelixlabs.axelix.master.service.state.DatabaseHistoricalApplicationSnapshotService;

/**
 * Serves the dashboards from a shared cache, computing each of them on the first request and on every refresh.
 *
 * @author Vyacheslav Yanin
 */
public class DashboardService {

    private final DashboardCache cache;
    private final SpringPortfolioService springPortfolioService;
    private final LanguagesProfileService languagesProfileService;
    private final DatabaseHistoricalApplicationSnapshotService snapshotService;
    private final DependencyAnalysisService dependencyAnalysisService;

    public DashboardService(
            DashboardCache cache,
            SpringPortfolioService springPortfolioService,
            LanguagesProfileService languagesProfileService,
            DatabaseHistoricalApplicationSnapshotService snapshotService,
            DependencyAnalysisService dependencyAnalysisService) {
        this.cache = cache;
        this.springPortfolioService = springPortfolioService;
        this.languagesProfileService = languagesProfileService;
        this.snapshotService = snapshotService;
        this.dependencyAnalysisService = dependencyAnalysisService;
    }

    public PersistenceDashboardResponse getPersistenceDashboard() {
        return cache.get(
                DashboardKey.PERSISTENCE,
                PersistenceDashboardResponse.class,
                snapshotService::getPersistenceDashboard);
    }

    public DependenciesDashboardResponse getDependenciesDashboard() {
        return cache.get(
                DashboardKey.DEPENDENCIES,
                DependenciesDashboardResponse.class,
                dependencyAnalysisService::getDashboard);
    }

    public SpringPortfolioResponse getSpringPortfolio() {
        return cache.get(
                DashboardKey.SPRING_PORTFOLIO,
                SpringPortfolioResponse.class,
                springPortfolioService::getSpringPortfolio);
    }

    public LanguagesProfileResponse getLanguagesProfile() {
        return cache.get(
                DashboardKey.LANGUAGES,
                LanguagesProfileResponse.class,
                languagesProfileService::getLanguagesProfile);
    }

    /**
     * Recomputes every dashboard and replaces the cached values.
     */
    public void refreshAll() {
        cache.put(DashboardKey.PERSISTENCE, snapshotService.getPersistenceDashboard());
        cache.put(DashboardKey.DEPENDENCIES, dependencyAnalysisService.getDashboard());
        cache.put(DashboardKey.SPRING_PORTFOLIO, springPortfolioService.getSpringPortfolio());
        cache.put(DashboardKey.LANGUAGES, languagesProfileService.getLanguagesProfile());
    }
}
