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

import org.springframework.web.bind.annotation.GetMapping;

import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.response.DashboardResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.JavaDashboardResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.PersistenceDashboardResponse;
import com.axelixlabs.axelix.master.api.external.response.dashboard.SpringFrameworkDashboardResponse;
import com.axelixlabs.axelix.master.service.DashboardService;
import com.axelixlabs.axelix.master.service.state.DatabaseHistoricalApplicationSnapshotService;

/**
 * API for rendering the dashboard.
 *
 * @author Mikhail Polivakha
 */
@ExternalApiRestController
public class DashboardApi {

    private final DashboardService dashboardService;
    private final DatabaseHistoricalApplicationSnapshotService databaseHistoricalApplicationSnapshotService;

    public DashboardApi(
            DashboardService dashboardService,
            DatabaseHistoricalApplicationSnapshotService databaseHistoricalApplicationSnapshotService) {
        this.dashboardService = dashboardService;
        this.databaseHistoricalApplicationSnapshotService = databaseHistoricalApplicationSnapshotService;
    }

    /**
     * Retrieve information about the entire ecosystem to render the dashboard.
     */
    @GetMapping(path = ApiPaths.DashboardApi.MAIN)
    public DashboardResponse getDashboard() {
        return dashboardService.getDashboardInfo();
    }

    /**
     * Retrieve the aggregated Java/JVM features adoption across the entire ecosystem.
     */
    @GetMapping(path = ApiPaths.DashboardApi.JAVA)
    public JavaDashboardResponse getJavaDashboard() {
        return databaseHistoricalApplicationSnapshotService.getJavaDashboard();
    }

    /**
     * Retrieve the aggregated Spring Framework features adoption across the entire ecosystem.
     */
    @GetMapping(path = ApiPaths.DashboardApi.SPRING_FRAMEWORK)
    public SpringFrameworkDashboardResponse getSpringFrameworkDashboard() {
        return databaseHistoricalApplicationSnapshotService.getSpringFrameworkDashboard();
    }

    /**
     * Retrieve the aggregated persistence problems across the ecosystem.
     */
    @GetMapping(path = ApiPaths.DashboardApi.PERSISTENCE)
    public PersistenceDashboardResponse getPersistenceDashboard() {
        return databaseHistoricalApplicationSnapshotService.getPersistenceDashboard();
    }
}
