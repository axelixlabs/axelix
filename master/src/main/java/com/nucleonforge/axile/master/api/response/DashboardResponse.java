/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.master.api.response;

import java.util.List;
import java.util.Map;

import com.nucleonforge.axile.master.api.response.software.DistributionResponse;

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
     * @param averageRss the average RSS consumed by average microservice
     * @param totalRss the total RSS consumed by the entire spring microservices' ecosystem.
     */
    public record MemoryUsageMap(MemoryUsage averageRss, MemoryUsage totalRss) {}

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
