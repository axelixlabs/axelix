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
package com.axelixlabs.axelix.master.api.external.response.dashboard;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.axelixlabs.axelix.master.domain.ecosystem.projects.SoftwareProject;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SupportStatus;

/**
 * The aggregated dependency health of the ecosystem.
 *
 * @param totalServices                 the number of services that reported their SBOM.
 * @param servicesWithRiskyDependencies the number of services that use at least one project in a status other
 *                                      than {@link SupportStatus#ACTIVE}.
 * @param servicesByStatus              the number of services that use at least one project in the given status.
 * @param trackedProjects               the curated projects used by the services, the most widespread first.
 * @param lastScannedAt                 when the dashboard was scanned.
 *
 * @author Nikita Kirillov
 */
public record DependenciesDashboardResponse(
        long totalServices,
        long servicesWithRiskyDependencies,
        Map<SupportStatus, Long> servicesByStatus,
        List<TrackedProject> trackedProjects,
        Instant lastScannedAt) {

    /**
     * A curated project used by the services.
     *
     * @param softwareProject the curated project.
     * @param serviceCount    the number of services that use the project.
     * @param share           the percentage of {@code totalServices} that use the project.
     */
    public record TrackedProject(SoftwareProject softwareProject, long serviceCount, double share) {}
}
