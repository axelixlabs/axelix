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

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.api.metrics.MetricProfile;
import com.axelixlabs.axelix.common.api.metrics.MetricsGroupsFeed;
import com.axelixlabs.axelix.common.api.metrics.MetricsGroupsFeed.MetricsGroup.MetricDescription;
import com.axelixlabs.axelix.common.api.transform.BaseUnitParser;
import com.axelixlabs.axelix.common.api.transform.BytesMemoryBaseUnitValueTransformer;
import com.axelixlabs.axelix.common.api.transform.KilobytesMemoryBaseUnitValueTransformer;
import com.axelixlabs.axelix.common.api.transform.units.MegabytesMemoryBaseUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixMetricsEndpoint}.
 *
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({
    AxelixMetricsEndpoint.class,
    MetricsEndpoint.class,
    DefaultServiceMetricsGroupsAssembler.class,
    BaseUnitParser.class,
    KilobytesMemoryBaseUnitValueTransformer.class,
    BytesMemoryBaseUnitValueTransformer.class
})
class AxelixMetricsEndpointTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void shouldProduceOnlyValidCombinationsOfTags() {
        // when.
        String metricName = "jvm.memory.max";
        ResponseEntity<MetricProfile> response =
                testRestTemplate.getForEntity("/actuator/axelix-metrics/" + metricName, MetricProfile.class);

        // then.
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        MetricProfile metricProfile = response.getBody();

        assertThat(metricProfile.name()).isEqualTo(metricName);
        assertThat(metricProfile.validTagCombinations())
                .containsOnly(
                        Map.of(
                                "area", "nonheap",
                                "id", "Compressed Class Space"),
                        Map.of(
                                "area", "nonheap",
                                "id", "CodeHeap 'non-profiled nmethods'"),
                        Map.of(
                                "area", "heap",
                                "id", "G1 Old Gen"),
                        Map.of(
                                "area", "heap",
                                "id", "G1 Eden Space"),
                        Map.of(
                                "area", "nonheap",
                                "id", "CodeHeap 'profiled nmethods'"),
                        Map.of(
                                "area", "nonheap",
                                "id", "CodeHeap 'non-nmethods'"),
                        Map.of(
                                "area", "nonheap",
                                "id", "Metaspace"),
                        Map.of(
                                "area", "heap",
                                "id", "G1 Survivor Space"));
    }

    @Test
    void shouldApplyValueTransformationsForGivenValue() {
        // when.
        String metricName = "for.value.transformations";
        ResponseEntity<MetricProfile> response =
                testRestTemplate.getForEntity("/actuator/axelix-metrics/" + metricName, MetricProfile.class);

        // then.
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        MetricProfile metricProfile = response.getBody();

        assertThat(metricProfile.name()).isEqualTo(metricName);
        assertThat(metricProfile.baseUnit()).isEqualTo(MegabytesMemoryBaseUnit.INSTANCE.getDisplayName());
        assertThat(metricProfile.measurements().get(0).value()).isCloseTo(5.22, Percentage.withPercentage(1));
    }

    @Test
    void shouldProduceEmptyValidTagCombinationsArrayInCaseNoTagsArePresent() {
        // when.
        String metricName = "jvm.gc.overhead";
        ResponseEntity<MetricProfile> response =
                testRestTemplate.getForEntity("/actuator/axelix-metrics/" + metricName, MetricProfile.class);

        // then.
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        MetricProfile metricProfile = response.getBody();

        assertThat(metricProfile.name()).isEqualTo(metricName);
        assertThat(metricProfile.validTagCombinations()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("metricsGroups")
    void shouldReturnGroupedMetricsWithDescriptions(String groupName, String metricName, String metricDescription) {
        // when.
        ResponseEntity<MetricsGroupsFeed> response =
                testRestTemplate.getForEntity("/actuator/axelix-metrics", MetricsGroupsFeed.class);

        // then.
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        MetricsGroupsFeed metricsGroups = response.getBody();

        assertThat(metricsGroups.metricsGroups())
                // metrics group
                .filteredOn(group -> group.groupName().equals(groupName))
                .first()
                .satisfies(group -> {
                    List<MetricDescription> metrics = group.metrics();

                    // metric name
                    assertThat(metrics)
                            .extracting(MetricDescription::metricName)
                            .contains(metricName);

                    // metric description
                    assertThat(metrics)
                            .extracting(MetricDescription::description)
                            .contains(metricDescription);
                });
    }

    private static Stream<Arguments> metricsGroups() {
        return Stream.of(
                Arguments.of(
                        "axelixMetrics",
                        "axelixMetrics.test.metric1",
                        "Test metric belonging to the `axelixMetrics` group with a description"),
                Arguments.of(
                        "axelixMetrics",
                        "axelixMetrics.test.metric2",
                        "Test metric belonging to the `axelixMetrics` group with a description"),
                Arguments.of("axelixMetrics", "axelixMetrics.test.metric3", null),
                Arguments.of(
                        "testMetrics",
                        "testMetrics.axelix.metric1",
                        "Test metric belonging to the `testMetrics` group with a description"),
                Arguments.of(
                        "testMetrics",
                        "testMetrics.axelix.metric2",
                        "Test metric belonging to the `testMetrics` group with a description"),
                Arguments.of(
                        "Others",
                        "standalone",
                        "Test metric belonging to the 'Others' group without a prefix and with a description"));
    }

    @TestConfiguration
    static class AxelixMetricsEndpointTestConfiguration {

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

                Gauge.builder(
                                "for.value.transformations", () -> 5480079 // ~ 5.22 MB
                                )
                        .baseUnit("bytes")
                        .register(registry);
            };
        }
    }
}
