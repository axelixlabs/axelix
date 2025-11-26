package com.nucleonforge.axile.sbs.spring.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.common.api.KeyValue;
import com.nucleonforge.axile.common.api.metrics.AxileMetricsGroups;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultServiceMetricsGroupsAssembler}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest
public class DefaultServiceMetricsGroupsAssemblerTest {

    @Autowired
    ServiceMetricsGroupsAssembler assembler;

    @Test
    void shouldReturnGroupedMetricsWithDescriptions() {
        AxileMetricsGroups metricsGroups = assembler.assemble();

        AxileMetricsGroups.MetricsGroup axile = getMetricsGroup(metricsGroups, "axileMetrics");
        assertThat(axile.groupName()).isEqualTo("axileMetrics");
        assertThat(axile.metrics())
                .containsOnly(
                        new KeyValue(
                                "axileMetrics.test.metric1",
                                "Test metric belonging to the `axileMetrics` group with a description"),
                        new KeyValue(
                                "axileMetrics.test.metric2",
                                "Test metric belonging to the `axileMetrics` group with a description"),
                        new KeyValue("axileMetrics.test.metric3", null));

        AxileMetricsGroups.MetricsGroup test = getMetricsGroup(metricsGroups, "testMetrics");
        assertThat(test.groupName()).isEqualTo("testMetrics");
        assertThat(test.metrics())
                .containsOnly(
                        new KeyValue(
                                "testMetrics.axile.metric1",
                                "Test metric belonging to the `testMetrics` group with a description"),
                        new KeyValue("testMetrics.axile.metric2", null));

        AxileMetricsGroups.MetricsGroup other = getMetricsGroup(metricsGroups, "Others");
        assertThat(other.groupName()).isEqualTo("Others");
        assertThat(other.metrics())
                .contains(new KeyValue(
                        "standalone",
                        "Test metric belonging to the 'Others' group without a prefix and with a description"));
    }

    private AxileMetricsGroups.MetricsGroup getMetricsGroup(AxileMetricsGroups response, String groupName) {
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
