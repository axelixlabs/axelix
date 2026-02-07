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
package com.axelixlabs.axelix.master.api.external.response;

import java.util.List;
import java.util.Map;

import com.axelixlabs.axelix.master.api.external.response.software.DistributionResponse;

/**
 * @param distributions the list of software distributions in use along with counters.
 *                      This would, among other things, include the Java version usage
 *                      by each version, the JDK vendor usage by vendor name, Spring Boot
 *                      usage by version etc.
 * @author Mikhail Polivakha
 */
public record DashboardResponse(
        List<DistributionResponse> distributions, HealthStatus healthStatus, MemoryUsageMap memoryUsage) {

    /**
     * @param averageHeapSize the average heap size among all the managed instances
     * @param totalHeapSize the total heap size among all the managed instances
     */
    public record MemoryUsageMap(MemoryUsage averageHeapSize, MemoryUsage totalHeapSize) {}

    /**
     * @param unit the unit in which the {@link #value()} is measured (MB/GB etc.)
     * @param value the actual value of memory usage, represented in {@link #unit units}.
     */
    public record MemoryUsage(String unit, double value) {}

    /**
     * The health status of the entire ecosystem.
     *
     * @param statuses map that contains the status to the counter, of how
     *                 many instances are in that status.
     */
    public record HealthStatus(Map<Status, Integer> statuses) {}

    public enum Status {
        UP,
        DOWN,
        UNKNOWN
    }
}
