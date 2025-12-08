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
package com.nucleonforge.axile.sbs.spring.metrics;

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

import com.nucleonforge.axile.common.api.metrics.MetricProfile;
import com.nucleonforge.axile.common.api.metrics.MetricsGroupsFeed;
import com.nucleonforge.axile.common.api.metrics.MetricsGroupsFeed.MetricsGroup.MetricDescription;
import com.nucleonforge.axile.sbs.spring.metrics.transform.BaseUnitParser;
import com.nucleonforge.axile.sbs.spring.metrics.transform.BytesMemoryBaseUnitValueTransformer;
import com.nucleonforge.axile.sbs.spring.metrics.transform.KilobytesMemoryBaseUnitValueTransformer;
import com.nucleonforge.axile.sbs.spring.metrics.transform.units.MegabytesMemoryBaseUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxileMetricsEndpoint}.
 *
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({
    AxileMetricsEndpoint.class,
    MetricsEndpoint.class,
    DefaultServiceMetricsGroupsAssembler.class,
    BaseUnitParser.class,
    KilobytesMemoryBaseUnitValueTransformer.class,
    BytesMemoryBaseUnitValueTransformer.class
})
class AxileMetricsEndpointTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void shouldProduceOnlyValidCombinationsOfTags() {
        // when.
        String metricName = "jvm.memory.max";
        ResponseEntity<MetricProfile> response =
                testRestTemplate.getForEntity("/actuator/axile-metrics/" + metricName, MetricProfile.class);

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
                testRestTemplate.getForEntity("/actuator/axile-metrics/" + metricName, MetricProfile.class);

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
                testRestTemplate.getForEntity("/actuator/axile-metrics/" + metricName, MetricProfile.class);

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
                testRestTemplate.getForEntity("/actuator/axile-metrics", MetricsGroupsFeed.class);

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
                        "axileMetrics",
                        "axileMetrics.test.metric1",
                        "Test metric belonging to the `axileMetrics` group with a description"),
                Arguments.of(
                        "axileMetrics",
                        "axileMetrics.test.metric2",
                        "Test metric belonging to the `axileMetrics` group with a description"),
                Arguments.of("axileMetrics", "axileMetrics.test.metric3", null),
                Arguments.of(
                        "testMetrics",
                        "testMetrics.axile.metric1",
                        "Test metric belonging to the `testMetrics` group with a description"),
                Arguments.of(
                        "testMetrics",
                        "testMetrics.axile.metric2",
                        "Test metric belonging to the `testMetrics` group with a description"),
                Arguments.of(
                        "Others",
                        "standalone",
                        "Test metric belonging to the 'Others' group without a prefix and with a description"));
    }

    @TestConfiguration
    static class AxileMetricsEndpointTestConfiguration {

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

                Gauge.builder(
                                "for.value.transformations", () -> 5480079 // ~ 5.22 MB
                                )
                        .baseUnit("bytes")
                        .register(registry);
            };
        }
    }
}
