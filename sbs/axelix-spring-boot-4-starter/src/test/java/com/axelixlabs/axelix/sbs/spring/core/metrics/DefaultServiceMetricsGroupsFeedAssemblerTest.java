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
package com.axelixlabs.axelix.sbs.spring.core.metrics;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import com.axelixlabs.axelix.sbs.spring.core.contract.metrics.MetricDescription;
import com.axelixlabs.axelix.sbs.spring.core.contract.metrics.MetricsGroup;
import com.axelixlabs.axelix.sbs.spring.core.contract.metrics.MetricsGroupsFeed;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultServiceMetricsGroupsAssembler}.
 *
 * @author Sergey Cherkasov
 * @author Artemiy Degtyarev
 */
class DefaultServiceMetricsGroupsFeedAssemblerTest extends AbstractMetricsSharedContextTest {

    @Autowired
    ServiceMetricsGroupsAssembler assembler;

    @Test
    void shouldReturnGroupedMetricsWithDescriptions() {
        MetricsGroupsFeed metricsGroups = assembler.assemble();

        MetricsGroup axelix = getMetricsGroup(metricsGroups, "axelixMetrics");
        assertThat(axelix.getGroupName()).isEqualTo("axelixMetrics");
        assertThat(axelix.getMetrics())
                .containsOnly(
                        new MetricDescription()
                                .metricName("axelixMetrics.test.metric1")
                                .description("Test metric belonging to the `axelixMetrics` group with a description"),
                        new MetricDescription()
                                .metricName("axelixMetrics.test.metric2")
                                .description("Test metric belonging to the `axelixMetrics` group with a description"),
                        new MetricDescription()
                                .metricName("axelixMetrics.test.metric3")
                                .description(null));

        MetricsGroup test = getMetricsGroup(metricsGroups, "testMetrics");
        assertThat(test.getGroupName()).isEqualTo("testMetrics");
        assertThat(test.getMetrics())
                .containsOnly(
                        new MetricDescription()
                                .metricName("testMetrics.axelix.metric1")
                                .description("Test metric belonging to the `testMetrics` group with a description"),
                        new MetricDescription()
                                .metricName("testMetrics.axelix.metric2")
                                .description(null));

        MetricsGroup other = getMetricsGroup(metricsGroups, "Others");
        assertThat(other.getGroupName()).isEqualTo("Others");
        assertThat(other.getMetrics())
                .contains(new MetricDescription()
                        .metricName("standalone")
                        .description(
                                "Test metric belonging to the 'Others' group without a prefix and with a description"));
    }

    private MetricsGroup getMetricsGroup(MetricsGroupsFeed response, String groupName) {
        return response.getMetricsGroups().stream()
                .filter(group -> group.getGroupName().equals(groupName))
                .findFirst()
                .get();
    }
}
