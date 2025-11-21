package com.nucleonforge.axile.sbs.spring.metrics;

import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;

import com.nucleonforge.axile.common.api.metrics.MetricProfile;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({AxileMetricsEndpoint.class, MetricsEndpoint.class})
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
}
