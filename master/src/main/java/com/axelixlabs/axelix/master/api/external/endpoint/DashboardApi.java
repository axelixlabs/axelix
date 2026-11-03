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
package com.axelixlabs.axelix.master.api.external.endpoint;

import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.response.dashboard.DependenciesDashboardResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.DependencyLibrariesResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.LanguagesProfileResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.PersistenceDashboardResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.SpringPortfolioResponse;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.Ecosystem;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SupportStatus;
import com.axelixlabs.axelix.master.service.dashboard.DashboardService;
import com.axelixlabs.axelix.master.service.ecosystem.DependencyAnalysisService;
import com.axelixlabs.axelix.master.service.ecosystem.DependencyLibrariesQuery;

/**
 * API for rendering the dashboard.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 * @author Vyacheslav Yanin
 */
@ExternalApiRestController
public class DashboardApi {

    private final DashboardService dashboardService;
    private final DependencyAnalysisService dependencyAnalysisService;

    public DashboardApi(DashboardService dashboardService, DependencyAnalysisService dependencyAnalysisService) {
        this.dashboardService = dashboardService;
        this.dependencyAnalysisService = dependencyAnalysisService;
    }

    /**
     * Retrieve the aggregated persistence problems across the ecosystem.
     */
    @GetMapping(path = ApiPaths.DashboardApi.PERSISTENCE)
    public PersistenceDashboardResponse getPersistenceDashboard() {
        return dashboardService.getPersistenceDashboard();
    }

    /**
     * Retrieve the aggregated dependency health across the ecosystem.
     */
    @GetMapping(path = ApiPaths.DashboardApi.DEPENDENCIES)
    public DependenciesDashboardResponse getDependenciesDashboard() {
        return dashboardService.getDependenciesDashboard();
    }

    /**
     * Retrieve a page of the libraries used across the ecosystem, each with its version distribution.
     *
     * @param search     a case-insensitive part of the {@code groupId:artifactId} to look for.
     * @param statuses   the support statuses to keep, all when absent.
     * @param ecosystems the ecosystems to keep, all when absent.
     * @param page       the zero-based number of the page.
     * @param size       the maximum number of libraries on a page, between 1 and 100.
     */
    @GetMapping(path = ApiPaths.DashboardApi.DEPENDENCIES_LIBRARIES)
    public DependencyLibrariesResponse getDependencyLibraries(
            @RequestParam(value = "search", required = false) @Nullable String search,
            @RequestParam(value = "status", required = false) @Nullable Set<SupportStatus> statuses,
            @RequestParam(value = "ecosystem", required = false) @Nullable Set<Ecosystem> ecosystems,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return dependencyAnalysisService.getLibraries(new DependencyLibrariesQuery(
                search,
                statuses != null ? statuses : Set.of(),
                ecosystems != null ? ecosystems : Set.of(),
                page,
                size));
    }

    /**
     * Retrieve the fleet-wide Spring Boot / Spring Framework version portfolio.
     */
    @GetMapping(path = ApiPaths.DashboardApi.SPRING_PORTFOLIO)
    public SpringPortfolioResponse getSpringPortfolio() {
        return dashboardService.getSpringPortfolio();
    }

    /**
     * Retrieve the fleet-wide languages profile: Java releases, JDK builds and Kotlin adoption across the ecosystem.
     */
    @GetMapping(path = ApiPaths.DashboardApi.LANGUAGES)
    public LanguagesProfileResponse getLanguagesProfile() {
        return dashboardService.getLanguagesProfile();
    }
}
