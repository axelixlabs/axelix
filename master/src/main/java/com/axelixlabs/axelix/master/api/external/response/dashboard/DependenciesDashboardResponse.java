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
import java.util.Map;

import com.axelixlabs.axelix.master.domain.ecosystem.projects.Ecosystem;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SupportStatus;

/**
 * The aggregated dependency health of the ecosystem. The libraries themselves are served page by page, see
 * {@link DependencyLibrariesResponse}.
 *
 * @param totalApplications                 the number of applications that reported their SBOM.
 * @param applicationsWithRiskyDependencies the number of applications that use at least one library in a status other
 *                                          than {@link SupportStatus#ACTIVE}.
 * @param applicationsByStatus              the number of applications that use at least one library in the given
 *                                          status.
 * @param totalLibraries                    the number of distinct libraries used across the applications, including
 *                                          the ones Axelix knows nothing about (e.g. the organization's internal
 *                                          libraries).
 * @param librariesByStatus                 the number of libraries in use per status. Only the libraries Axelix
 *                                          curates have a status.
 * @param riskyLibrariesByEcosystem         the number of libraries in use per ecosystem, broken down by every status
 *                                          other than {@link SupportStatus#ACTIVE}. Ecosystems without such libraries
 *                                          are absent.
 * @param computedAt                        when the dashboard was computed.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Vyacheslav Yanin
 */
public record DependenciesDashboardResponse(
        long totalApplications,
        long applicationsWithRiskyDependencies,
        Map<SupportStatus, Long> applicationsByStatus,
        long totalLibraries,
        Map<SupportStatus, Long> librariesByStatus,
        Map<Ecosystem, Map<SupportStatus, Long>> riskyLibrariesByEcosystem,
        Instant computedAt)
        implements DashboardResponse {}
