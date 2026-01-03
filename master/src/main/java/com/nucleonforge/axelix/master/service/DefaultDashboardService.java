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
package com.nucleonforge.axelix.master.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.nucleonforge.axelix.common.api.transform.BaseUnitParser;
import com.nucleonforge.axelix.common.api.transform.BaseUnitValueTransformer;
import com.nucleonforge.axelix.common.api.transform.TransformedMetricValue;
import com.nucleonforge.axelix.common.api.transform.units.BaseUnit;
import com.nucleonforge.axelix.master.api.response.DashboardResponse;
import com.nucleonforge.axelix.master.api.response.software.DistributionResponse;
import com.nucleonforge.axelix.master.api.response.software.SoftwareDistributions;
import com.nucleonforge.axelix.master.model.instance.Instance;
import com.nucleonforge.axelix.master.service.state.InstanceRegistry;

import static com.nucleonforge.axelix.master.api.response.DashboardResponse.HealthStatus;
import static com.nucleonforge.axelix.master.api.response.DashboardResponse.MemoryUsage;
import static com.nucleonforge.axelix.master.api.response.DashboardResponse.MemoryUsageMap;
import static com.nucleonforge.axelix.master.api.response.DashboardResponse.Status;
import static com.nucleonforge.axelix.master.service.versions.VersionTrimmer.getMajorMinorVersion;
import static com.nucleonforge.axelix.master.service.versions.VersionTrimmer.getMajorVersion;

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
        var jdkVendor = new DistributionResponse(SoftwareDistributions.JDK_VENDOR);

        for (Instance instance : instanceRegistry.getAll()) {
            switch (instance.status()) {
                case UP -> statuesMap.compute(Status.UP, counterIncrementFunction());
                case DOWN, RELOAD -> statuesMap.compute(Status.DOWN, counterIncrementFunction());
                case UNKNOWN -> statuesMap.compute(Status.UNKNOWN, counterIncrementFunction());
            }

            java.addVersion(getMajorVersion(instance.javaVersion()));
            springBoot.addVersion(getMajorMinorVersion(instance.springBootVersion()));
            springFramework.addVersion(getMajorMinorVersion(instance.springFrameworkVersion()));
            jdkVendor.addVersion(instance.jdkVendor());

            if (instance.kotlinVersion() != null) {
                kotlin.addVersion(getMajorMinorVersion(instance.kotlinVersion()));
            }
        }

        var healthStatus = new HealthStatus(statuesMap);
        var memoryUsage = buildMemoryUsageMap();
        return new DashboardResponse(
                List.of(springBoot, springFramework, java, kotlin, jdkVendor), healthStatus, memoryUsage);
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
