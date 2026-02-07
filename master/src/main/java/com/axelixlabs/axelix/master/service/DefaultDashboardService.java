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
package com.axelixlabs.axelix.master.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.api.transform.BaseUnitParser;
import com.axelixlabs.axelix.common.api.transform.BaseUnitValueTransformer;
import com.axelixlabs.axelix.common.api.transform.TransformedMetricValue;
import com.axelixlabs.axelix.common.api.transform.units.BaseUnit;
import com.axelixlabs.axelix.master.api.external.response.DashboardResponse;
import com.axelixlabs.axelix.master.api.external.response.software.DistributionResponse;
import com.axelixlabs.axelix.master.api.external.response.software.SoftwareDistributions;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

import static com.axelixlabs.axelix.master.api.external.response.DashboardResponse.HealthStatus;
import static com.axelixlabs.axelix.master.api.external.response.DashboardResponse.MemoryUsage;
import static com.axelixlabs.axelix.master.api.external.response.DashboardResponse.MemoryUsageMap;
import static com.axelixlabs.axelix.master.api.external.response.DashboardResponse.Status;
import static com.axelixlabs.axelix.master.utils.VersionTrimmer.getMajorMinorVersion;
import static com.axelixlabs.axelix.master.utils.VersionTrimmer.getMajorVersion;

/**
 * Default implementation of {@link DashboardService}.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
@Service
public class DefaultDashboardService implements DashboardService {

    private final InstanceRegistry instanceRegistry;
    private final MemoryUsageCache memoryUsageCache;
    private final BaseUnitParser baseUnitParser;
    private final Map<BaseUnit, BaseUnitValueTransformer> baseUnitValueTransformers;

    public DefaultDashboardService(
            InstanceRegistry instanceRegistry,
            MemoryUsageCache memoryUsageCache,
            BaseUnitParser baseUnitParser,
            Set<BaseUnitValueTransformer> baseUnitValueTransformers) {
        this.instanceRegistry = instanceRegistry;
        this.memoryUsageCache = memoryUsageCache;
        this.baseUnitParser = baseUnitParser;

        this.baseUnitValueTransformers = baseUnitValueTransformers.stream()
                .collect(Collectors.toMap(BaseUnitValueTransformer::supports, Function.identity()));
    }

    @Override
    public DashboardResponse getDashboardInfo() {
        var statuesMap = new HashMap<Status, Integer>();

        var springBoot = new DistributionResponse(SoftwareDistributions.SPRING_BOOT);
        var springFramework = new DistributionResponse(SoftwareDistributions.SPRING_FRAMEWORK);
        var java = new DistributionResponse(SoftwareDistributions.JAVA);
        var kotlin = new DistributionResponse(SoftwareDistributions.KOTLIN);

        for (Instance instance : instanceRegistry.getAll()) {
            switch (instance.status()) {
                case UP -> statuesMap.compute(Status.UP, counterIncrementFunction());
                case DOWN, RELOAD -> statuesMap.compute(Status.DOWN, counterIncrementFunction());
                case UNKNOWN -> statuesMap.compute(Status.UNKNOWN, counterIncrementFunction());
            }

            java.addVersion(getMajorVersion(instance.javaVersion()));
            springBoot.addVersion(getMajorMinorVersion(instance.springBootVersion()));
            springFramework.addVersion(getMajorMinorVersion(instance.springFrameworkVersion()));

            if (instance.kotlinVersion() != null) {
                kotlin.addVersion(getMajorMinorVersion(instance.kotlinVersion()));
            }
        }

        var healthStatus = new HealthStatus(statuesMap);
        var memoryUsage = buildMemoryUsageMap();
        return new DashboardResponse(List.of(springBoot, springFramework, java, kotlin), healthStatus, memoryUsage);
    }

    private MemoryUsageMap buildMemoryUsageMap() {

        BaseUnitValueTransformer baseUnitValueTransformer = baseUnitParser
                .parse("bytes")
                .map(baseUnitValueTransformers::get)
                .orElse(null);

        double averageHeapSize = memoryUsageCache.getAverageHeapSize();
        double totalHeapSize = memoryUsageCache.getTotalHeapSize();

        if (baseUnitValueTransformer != null) {
            TransformedMetricValue transformedAverageRss = baseUnitValueTransformer.transform(averageHeapSize);
            TransformedMetricValue transformedTotalRss = baseUnitValueTransformer.transform(totalHeapSize);

            return new MemoryUsageMap(
                    new MemoryUsage(transformedAverageRss.baseUnit().getDisplayName(), transformedAverageRss.value()),
                    new MemoryUsage(transformedTotalRss.baseUnit().getDisplayName(), transformedTotalRss.value()));
        }

        return new MemoryUsageMap(new MemoryUsage("bytes", averageHeapSize), new MemoryUsage("bytes", totalHeapSize));
    }

    private static BiFunction<Status, Integer, Integer> counterIncrementFunction() {
        return (status, integer) -> {
            if (integer == null) {
                return 1;
            } else {
                return integer + 1;
            }
        };
    }
}
