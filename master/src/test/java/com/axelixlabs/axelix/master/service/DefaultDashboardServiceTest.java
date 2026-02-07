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

import java.util.List;
import java.util.Set;

import org.assertj.core.data.Percentage;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.transform.BaseUnitParser;
import com.axelixlabs.axelix.common.api.transform.BaseUnitValueTransformer;
import com.axelixlabs.axelix.common.api.transform.BytesMemoryBaseUnitValueTransformer;
import com.axelixlabs.axelix.common.api.transform.KilobytesMemoryBaseUnitValueTransformer;
import com.axelixlabs.axelix.master.api.external.response.DashboardResponse;
import com.axelixlabs.axelix.master.api.external.response.software.DistributionResponse;
import com.axelixlabs.axelix.master.api.external.response.software.SoftwareDistributions;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.state.InMemoryInstanceRegistry;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

import static com.axelixlabs.axelix.master.utils.TestObjectFactory.createInstance;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DefaultDashboardService}.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
class DefaultDashboardServiceTest {

    private DefaultDashboardService subject;

    @BeforeEach
    void setUp() {
        InstanceRegistry instanceRegistry = createInMemoryInstanceRegistry();
        MemoryUsageCache memoryUsageCache = createInMemoryUsageCache();
        BaseUnitParser baseUnitParser = new BaseUnitParser();
        Set<BaseUnitValueTransformer> transformerSet =
                Set.of(new BytesMemoryBaseUnitValueTransformer(), new KilobytesMemoryBaseUnitValueTransformer());

        subject = new DefaultDashboardService(instanceRegistry, memoryUsageCache, baseUnitParser, transformerSet);
    }

    private static @NotNull InMemoryInstanceRegistry createInMemoryInstanceRegistry() {
        var registry = new InMemoryInstanceRegistry();
        registry.register(createInstance("123", "21.0.0", "3.5.2", "6.1.1", "BellSoft", "2.0.2"));
        registry.register(createInstance("456", "25.0.1", "3.4.1", "6.2.0", "BellSoft", null));
        registry.register(createInstance("789", "21", "4.0.0", "7.0.1", "Oracle", null));

        return registry;
    }

    private static InMemoryMemoryUsageCache createInMemoryUsageCache() {
        var cache = new InMemoryMemoryUsageCache();
        cache.putHeapSize(InstanceId.of("123"), 300d);
        cache.putHeapSize(InstanceId.of("456"), 550d);
        cache.putHeapSize(InstanceId.of("789"), 410d);

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
        assertThat(memoryUsageMap.averageHeapSize().value())
                .isCloseTo((300d + 550d + 410d) / 3, Percentage.withPercentage(0.5));
        assertThat(memoryUsageMap.averageHeapSize().unit()).isEqualTo("bytes");
        assertThat(memoryUsageMap.totalHeapSize().value())
                .isCloseTo((300d + 550d + 410d) / 1024, Percentage.withPercentage(0.5));
        assertThat(memoryUsageMap.totalHeapSize().unit()).isEqualTo("KB");

        var distributions = dashboardInfo.distributions();

        DistributionResponse java = findDistribution(distributions, SoftwareDistributions.JAVA);
        assertThat(java.getVersions()).hasSize(2);
        assertThat(java.getVersions()).extractingByKey("21").isEqualTo(2L);
        assertThat(java.getVersions()).extractingByKey("25").isEqualTo(1L);

        DistributionResponse springBoot = findDistribution(distributions, SoftwareDistributions.SPRING_BOOT);
        assertThat(springBoot.getVersions()).hasSize(3);
        assertThat(springBoot.getVersions()).extractingByKey("3.4").isEqualTo(1L);
        assertThat(springBoot.getVersions()).extractingByKey("3.5").isEqualTo(1L);
        assertThat(springBoot.getVersions()).extractingByKey("4.0").isEqualTo(1L);

        DistributionResponse springFramework = findDistribution(distributions, SoftwareDistributions.SPRING_FRAMEWORK);
        assertThat(springFramework.getVersions()).hasSize(3);
        assertThat(springFramework.getVersions()).extractingByKey("6.1").isEqualTo(1L);
        assertThat(springFramework.getVersions()).extractingByKey("6.2").isEqualTo(1L);
        assertThat(springFramework.getVersions()).extractingByKey("7.0").isEqualTo(1L);

        DistributionResponse kotlin = findDistribution(distributions, SoftwareDistributions.KOTLIN);
        assertThat(kotlin.getVersions()).hasSize(1);
        assertThat(kotlin.getVersions()).extractingByKey("2.0").isEqualTo(1L);
    }

    private DistributionResponse findDistribution(List<DistributionResponse> distributions, String name) {
        return distributions.stream()
                .filter(it -> it.getSoftwareComponentName().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
