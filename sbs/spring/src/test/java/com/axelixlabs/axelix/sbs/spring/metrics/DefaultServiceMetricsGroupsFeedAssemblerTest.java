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
package com.axelixlabs.axelix.sbs.spring.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.common.api.metrics.MetricsGroupsFeed;
import com.axelixlabs.axelix.common.api.metrics.MetricsGroupsFeed.MetricsGroup;
import com.axelixlabs.axelix.common.api.metrics.MetricsGroupsFeed.MetricsGroup.MetricDescription;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultServiceMetricsGroupsAssembler}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest
public class DefaultServiceMetricsGroupsFeedAssemblerTest {

    @Autowired
    ServiceMetricsGroupsAssembler assembler;

    @Test
    void shouldReturnGroupedMetricsWithDescriptions() {
        MetricsGroupsFeed metricsGroups = assembler.assemble();

        MetricsGroup axelix = getMetricsGroup(metricsGroups, "axelixMetrics");
        assertThat(axelix.groupName()).isEqualTo("axelixMetrics");
        assertThat(axelix.metrics())
                .containsOnly(
                        new MetricDescription(
                                "axelixMetrics.test.metric1",
                                "Test metric belonging to the `axelixMetrics` group with a description"),
                        new MetricDescription(
                                "axelixMetrics.test.metric2",
                                "Test metric belonging to the `axelixMetrics` group with a description"),
                        new MetricDescription("axelixMetrics.test.metric3", null));

        MetricsGroup test = getMetricsGroup(metricsGroups, "testMetrics");
        assertThat(test.groupName()).isEqualTo("testMetrics");
        assertThat(test.metrics())
                .containsOnly(
                        new MetricDescription(
                                "testMetrics.axelix.metric1",
                                "Test metric belonging to the `testMetrics` group with a description"),
                        new MetricDescription("testMetrics.axelix.metric2", null));

        MetricsGroup other = getMetricsGroup(metricsGroups, "Others");
        assertThat(other.groupName()).isEqualTo("Others");
        assertThat(other.metrics())
                .contains(new MetricDescription(
                        "standalone",
                        "Test metric belonging to the 'Others' group without a prefix and with a description"));
    }

    private MetricsGroup getMetricsGroup(MetricsGroupsFeed response, String groupName) {
        return response.metricsGroups().stream()
                .filter(group -> group.groupName().equals(groupName))
                .findFirst()
                .get();
    }

    @TestConfiguration
    static class DefaultServiceMetricsGroupsAssemblerTestConfiguration {

        @Bean
        public ServiceMetricsGroupsAssembler defaultMetricsGroupsAssembler(MeterRegistry registry) {
            return new DefaultServiceMetricsGroupsAssembler(registry);
        }

        @Bean
        public MeterBinder groupingMetrics() {
            return registry -> {
                Counter.builder("axelixMetrics.test.metric1")
                        .description("Test metric belonging to the `axelixMetrics` group with a description")
                        .register(registry);

                Counter.builder("axelixMetrics.test.metric2")
                        .description("Test metric belonging to the `axelixMetrics` group with a description")
                        .register(registry);

                Counter.builder("axelixMetrics.test.metric3").register(registry);

                Counter.builder("testMetrics.axelix.metric1")
                        .description("Test metric belonging to the `testMetrics` group with a description")
                        .register(registry);

                Counter.builder("testMetrics.axelix.metric2").register(registry);

                Counter.builder("standalone")
                        .description(
                                "Test metric belonging to the 'Others' group without a prefix and with a description")
                        .register(registry);
            };
        }
    }
}
