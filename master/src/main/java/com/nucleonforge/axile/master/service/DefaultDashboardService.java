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
package com.nucleonforge.axile.master.service;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.master.api.response.DashboardResponse;
import com.nucleonforge.axile.master.api.response.software.DistributionResponse;
import com.nucleonforge.axile.master.api.response.software.SoftwareDistributions;
import com.nucleonforge.axile.master.model.instance.Instance;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

import static com.nucleonforge.axile.master.api.response.DashboardResponse.HealthStatus;
import static com.nucleonforge.axile.master.api.response.DashboardResponse.MemoryUsage;
import static com.nucleonforge.axile.master.api.response.DashboardResponse.MemoryUsageMap;
import static com.nucleonforge.axile.master.api.response.DashboardResponse.Status;

/**
 * Default implementation of {@link DashboardService}.
 *
 * @author Mikhail Polivakha
 */
@Service
public class DefaultDashboardService implements DashboardService {

    private final InstanceRegistry instanceRegistry;
    private final MemoryUsageCache memoryUsageCache;

    public DefaultDashboardService(InstanceRegistry instanceRegistry, MemoryUsageCache memoryUsageCache) {
        this.instanceRegistry = instanceRegistry;
        this.memoryUsageCache = memoryUsageCache;
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

            // TODO:
            //  now, here, we're having versions like:
            //  - java = 17.0.19u
            //  - spring-boot = 3.5.2
            //  - spring-framework = 6.0.2 etc.
            //  that is not really a great idea since we probably would have
            //  quite a lot of different Patch versions of spring boot/spring-framework for sure
            //  I guess we need to introduce the abstraction of Version to be able to show only
            //  major/minor versions pair, or even just major.
            java.addVersion(instance.javaVersion());
            springBoot.addVersion(instance.springBootVersion());
            springFramework.addVersion(instance.springFrameworkVersion());
            jdkVendor.addVersion(instance.jdkVendor());

            if (instance.kotlinVersion() != null) {
                kotlin.addVersion(instance.kotlinVersion());
            }
        }

        var healthStatus = new HealthStatus(statuesMap);
        var memoryUsage = buildMemoryUsageMap();
        return new DashboardResponse(
                List.of(springBoot, springFramework, java, kotlin, jdkVendor), healthStatus, memoryUsage);
    }

    private MemoryUsageMap buildMemoryUsageMap() {
        // TODO:
        //  We need to apply MemoryBaseUnit conversions as it is done in the
        //  metrics endpoint, see AbstractMemoryBaseUnitValueTransformer
        return new MemoryUsageMap(
                new MemoryUsage("bytes", memoryUsageCache.getAverageRss()),
                new MemoryUsage("bytes", memoryUsageCache.getTotalRss()));
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
