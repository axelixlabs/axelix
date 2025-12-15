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

import java.util.List;

import org.assertj.core.data.Percentage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.master.api.response.DashboardResponse;
import com.nucleonforge.axile.master.api.response.software.DistributionResponse;
import com.nucleonforge.axile.master.api.response.software.SoftwareDistributions;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.state.InMemoryInstanceRegistry;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

import static com.nucleonforge.axile.master.utils.TestObjectFactory.createInstance;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DefaultDashboardService}.
 *
 * @author Mikhail Polivakha
 */
class DefaultDashboardServiceTest {

    private DefaultDashboardService subject;

    @BeforeEach
    void setUp() {
        InstanceRegistry instanceRegistry = createInMemoryInstanceRegistry();
        MemoryUsageCache memoryUsageCache = createInMemoryUsageCache();
        subject = new DefaultDashboardService(instanceRegistry, memoryUsageCache);
    }

    private static @NotNull InMemoryInstanceRegistry createInMemoryInstanceRegistry() {
        var registry = new InMemoryInstanceRegistry();
        registry.register(createInstance("123", "21", "3.5.2", "6.1.1", "BellSoft", "2.0.2"));
        registry.register(createInstance("456", "25", "3.4.1", "6.2.0", "BellSoft", null));
        registry.register(createInstance("789", "21", "4.0.0", "7.0.1", "Oracle", null));

        return registry;
    }

    private static InMemoryMemoryUsageCache createInMemoryUsageCache() {
        var cache = new InMemoryMemoryUsageCache();
        cache.putRss(InstanceId.of("123"), 300d);
        cache.putRss(InstanceId.of("456"), 250d);
        cache.putRss(InstanceId.of("789"), 410d);

        return cache;
    }

    @Test
    void shouldReturnValidDashboard() {
        // when.
        DashboardResponse dashboardInfo = subject.getDashboardInfo();

        // then.
        var healthStatus = dashboardInfo.healthStatus();
        assertThat(healthStatus.statuses())
                .hasSize(1)
                .extractingByKey(DashboardResponse.Status.UP)
                .isEqualTo(3);

        var memoryUsageMap = dashboardInfo.memoryUsage();
        assertThat(memoryUsageMap.averageRss().value())
                .isCloseTo((300d + 250d + 410d) / 3, Percentage.withPercentage(0.5));
        assertThat(memoryUsageMap.totalRss().value()).isCloseTo(300d + 250d + 410d, Percentage.withPercentage(0.5));

        var distributions = dashboardInfo.distributions();

        DistributionResponse java = findDistribution(distributions, SoftwareDistributions.JAVA);
        assertThat(java.getVersions()).hasSize(2);
        assertThat(java.getVersions()).extractingByKey("21").isEqualTo(2L);
        assertThat(java.getVersions()).extractingByKey("25").isEqualTo(1L);

        DistributionResponse jdkVendor = findDistribution(distributions, SoftwareDistributions.JDK_VENDOR);
        assertThat(jdkVendor.getVersions()).hasSize(2);
        assertThat(jdkVendor.getVersions()).extractingByKey("BellSoft").isEqualTo(2L);
        assertThat(jdkVendor.getVersions()).extractingByKey("Oracle").isEqualTo(1L);

        DistributionResponse kotlin = findDistribution(distributions, SoftwareDistributions.KOTLIN);
        assertThat(kotlin.getVersions()).hasSize(1);
        assertThat(kotlin.getVersions()).extractingByKey("2.0.2").isEqualTo(1L);
    }

    private DistributionResponse findDistribution(List<DistributionResponse> distributions, String name) {
        return distributions.stream()
                .filter(it -> it.getSoftwareComponentName().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
