package com.nucleonforge.axile.master.service.convert.metrics;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.KeyValue;
import com.nucleonforge.axile.common.api.metrics.AxileMetricsGroups;
import com.nucleonforge.axile.master.api.response.metrics.MetricsGroupsResponse;
import com.nucleonforge.axile.master.service.convert.response.metrics.AxileMetricsGroupsConverter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link AxileMetricsGroupsConverter}.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
class AxileMetricsGroupsConverterTest {

    private AxileMetricsGroupsConverter subject;

    @BeforeEach
    void setUp() {
        subject = new AxileMetricsGroupsConverter();
    }

    @Test
    void testConvertHappyPath() {
        // when.
        MetricsGroupsResponse response = subject.convertInternal(metricGroup());

        // then.
        assertThat(response.metricsGroups()).isNotEmpty().hasSize(3);

        // jvm
        MetricsGroupsResponse.MetricsGroup jvmGroup = getMetricsGroup(response, "jvm");
        assertThat(jvmGroup.groupName()).isEqualTo("jvm");
        assertThat(jvmGroup.metrics())
                .containsOnly(
                        new KeyValue(
                                "jvm.gc.memory.allocated",
                                "Incremented for an increase in the size of the (young) heap memory pool after one GC to before the next"),
                        new KeyValue(
                                "jvm.memory.usage.after.gc",
                                "The percentage of long-lived heap pool used after the last GC event, in the range [0..1]"),
                        new KeyValue("jvm.memory.used", "The amount of used memory"));

        // process
        MetricsGroupsResponse.MetricsGroup processGroup = getMetricsGroup(response, "process");
        assertThat(processGroup.groupName()).isEqualTo("process");
        assertThat(processGroup.metrics())
                .containsOnly(
                        new KeyValue("process.cpu.time", "The \"cpu time\" used by the Java Virtual Machine process"),
                        new KeyValue(
                                "process.cpu.usage", "The \"recent cpu usage\" for the Java Virtual Machine process"));

        // tomcat
        MetricsGroupsResponse.MetricsGroup tomcatGroup = getMetricsGroup(response, "tomcat");
        assertThat(tomcatGroup.groupName()).isEqualTo("tomcat");
        assertThat(tomcatGroup.metrics())
                .containsOnly(
                        new KeyValue("tomcat.sessions.active.current", null),
                        new KeyValue("tomcat.sessions.active.max", null));
    }

    private MetricsGroupsResponse.MetricsGroup getMetricsGroup(MetricsGroupsResponse response, String groupName) {
        return response.metricsGroups().stream()
                .filter(group -> group.groupName().equals(groupName))
                .findFirst()
                .get();
    }

    private static AxileMetricsGroups metricGroup() {
        // jvm
        List<KeyValue> jvm = List.of(
                new KeyValue(
                        "jvm.gc.memory.allocated",
                        "Incremented for an increase in the size of the (young) heap memory pool after one GC to before the next"),
                new KeyValue(
                        "jvm.memory.usage.after.gc",
                        "The percentage of long-lived heap pool used after the last GC event, in the range [0..1]"),
                new KeyValue("jvm.memory.used", "The amount of used memory"));

        AxileMetricsGroups.MetricsGroup jvmGroup = new AxileMetricsGroups.MetricsGroup("jvm", jvm);

        // process
        List<KeyValue> process = List.of(
                new KeyValue("process.cpu.time", "The \"cpu time\" used by the Java Virtual Machine process"),
                new KeyValue("process.cpu.usage", "The \"recent cpu usage\" for the Java Virtual Machine process"));

        AxileMetricsGroups.MetricsGroup processGroup = new AxileMetricsGroups.MetricsGroup("process", process);

        // tomcat
        List<KeyValue> tomcat = List.of(
                new KeyValue("tomcat.sessions.active.current", null), new KeyValue("tomcat.sessions.active.max", null));

        AxileMetricsGroups.MetricsGroup tomcatGroup = new AxileMetricsGroups.MetricsGroup("tomcat", tomcat);

        // return -> AxileMetricsGroups
        return new AxileMetricsGroups(List.of(jvmGroup, processGroup, tomcatGroup));
    }
}
