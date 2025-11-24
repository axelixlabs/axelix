package com.nucleonforge.axile.master.service.convert.metrics;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.metrics.MetricsList;
import com.nucleonforge.axile.master.api.response.metrics.MetricsListResponse;
import com.nucleonforge.axile.master.service.convert.response.metrics.GetAllMetricsConverter;

/**
 * Unit test for {@link GetAllMetricsConverter}.
 *
 * @author Mikhail Polivakha
 */
class GetAllMetricsConverterTest {

    private GetAllMetricsConverter subject;

    @BeforeEach
    void setUp() {
        subject = new GetAllMetricsConverter();
    }

    @Test
    void shouldExtractMetricGroups() {
        // when.
        MetricsListResponse response = subject.convertInternal(new MetricsList(metricNames()));

        // then.
        Assertions.assertThat(response.metricsGroups())
                .hasSize(5)
                .extracting(MetricsListResponse.MetricsGroup::groupName)
                .containsOnly("process", "system", "tasks", "tomcat", GetAllMetricsConverter.OTHER_GROUP_NAME);

        Assertions.assertThat(response.metricsGroups())
                .filteredOn(it -> it.groupName().equals("process"))
                .extracting(MetricsListResponse.MetricsGroup::metrics)
                .containsOnly(List.of(
                        "process.cpu.time",
                        "process.cpu.usage",
                        "process.files.max",
                        "process.files.open",
                        "process.start.time",
                        "process.uptime"));
    }

    private static List<String> metricNames() {
        return List.of(
                "some-metric",
                "process.cpu.time",
                "process.cpu.usage",
                "process.files.max",
                "process.files.open",
                "process.start.time",
                "process.uptime",
                "system.cpu.count",
                "system.cpu.usage",
                "system.load.average.1m",
                "tasks.scheduled.execution",
                "tasks.scheduled.execution.active",
                "tomcat.sessions.active.current",
                "tomcat.sessions.active.max",
                "tomcat.sessions.alive.max",
                "tomcat.sessions.created",
                "tomcat.sessions.expired",
                "tomcat.sessions.rejected");
    }
}
