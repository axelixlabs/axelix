/*
 * Copyright 2025-present the original author or authors.
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
package com.nucleonforge.axile.sbs.spring.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.common.api.metrics.MetricsGroupsFeed;
import com.nucleonforge.axile.common.api.metrics.MetricsGroupsFeed.MetricsGroup;
import com.nucleonforge.axile.common.api.metrics.MetricsGroupsFeed.MetricsGroup.MetricDescription;

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

        MetricsGroup axile = getMetricsGroup(metricsGroups, "axileMetrics");
        assertThat(axile.groupName()).isEqualTo("axileMetrics");
        assertThat(axile.metrics())
                .containsOnly(
                        new MetricDescription(
                                "axileMetrics.test.metric1",
                                "Test metric belonging to the `axileMetrics` group with a description"),
                        new MetricDescription(
                                "axileMetrics.test.metric2",
                                "Test metric belonging to the `axileMetrics` group with a description"),
                        new MetricDescription("axileMetrics.test.metric3", null));

        MetricsGroup test = getMetricsGroup(metricsGroups, "testMetrics");
        assertThat(test.groupName()).isEqualTo("testMetrics");
        assertThat(test.metrics())
                .containsOnly(
                        new MetricDescription(
                                "testMetrics.axile.metric1",
                                "Test metric belonging to the `testMetrics` group with a description"),
                        new MetricDescription("testMetrics.axile.metric2", null));

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
                Counter.builder("axileMetrics.test.metric1")
                        .description("Test metric belonging to the `axileMetrics` group with a description")
                        .register(registry);

                Counter.builder("axileMetrics.test.metric2")
                        .description("Test metric belonging to the `axileMetrics` group with a description")
                        .register(registry);

                Counter.builder("axileMetrics.test.metric3").register(registry);

                Counter.builder("testMetrics.axile.metric1")
                        .description("Test metric belonging to the `testMetrics` group with a description")
                        .register(registry);

                Counter.builder("testMetrics.axile.metric2").register(registry);

                Counter.builder("standalone")
                        .description(
                                "Test metric belonging to the 'Others' group without a prefix and with a description")
                        .register(registry);
            };
        }
    }
}
