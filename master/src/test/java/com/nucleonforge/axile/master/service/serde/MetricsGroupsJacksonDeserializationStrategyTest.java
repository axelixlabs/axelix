package com.nucleonforge.axile.master.service.serde;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.KeyValue;
import com.nucleonforge.axile.common.api.metrics.AxileMetricsGroups;
import com.nucleonforge.axile.master.service.serde.metrics.MetricsGroupsJacksonDeserializationStrategy;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MetricsGroupsJacksonDeserializationStrategy}.
 *
 * @author Sergey Cherkasov
 */
public class MetricsGroupsJacksonDeserializationStrategyTest {
    private final MetricsGroupsJacksonDeserializationStrategy subject =
            new MetricsGroupsJacksonDeserializationStrategy(new ObjectMapper());

    @Test
    void shouldDeserializeAxileMetricsGroups() {
        // language=json
        String response =
                """
            {
              "metricsGroups": [
                {
                  "groupName": "jvm",
                  "metrics": [
                    {
                      "key": "jvm.gc.memory.allocated",
                      "value": "Incremented for an increase in the size of the (young) heap memory pool after one GC to before the next"
                    },
                    {
                      "key": "jvm.memory.usage.after.gc",
                      "value": "The percentage of long-lived heap pool used after the last GC event, in the range [0..1]"
                    },
                    {
                      "key": "jvm.memory.used",
                      "value": "The amount of used memory"
                    }
                  ]
                },
                {
                  "groupName": "process",
                  "metrics": [
                    {
                      "key": "process.cpu.time",
                      "value": "The \\"cpu time\\" used by the Java Virtual Machine process"
                    },
                    {
                      "key": "process.cpu.usage",
                      "value": "The \\"recent cpu usage\\" for the Java Virtual Machine process"
                    }
                  ]
                },
                {
                  "groupName": "tomcat",
                  "metrics": [
                    {
                      "key": "tomcat.sessions.active.current",
                      "value": null
                    },
                    {
                      "key": "tomcat.sessions.active.max",
                      "value": null
                    }
                  ]
                }
              ]
            }
        """;

        // when.
        AxileMetricsGroups metricsGroups = subject.deserialize(response.getBytes(StandardCharsets.UTF_8));

        // then.
        assertThat(metricsGroups.metricsGroups()).isNotEmpty().hasSize(3);

        // jvm
        AxileMetricsGroups.MetricsGroup jvmGroup = getMetricsGroup(metricsGroups, "jvm");
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
        AxileMetricsGroups.MetricsGroup processGroup = getMetricsGroup(metricsGroups, "process");
        assertThat(processGroup.groupName()).isEqualTo("process");
        assertThat(processGroup.metrics())
                .containsOnly(
                        new KeyValue("process.cpu.time", "The \"cpu time\" used by the Java Virtual Machine process"),
                        new KeyValue(
                                "process.cpu.usage", "The \"recent cpu usage\" for the Java Virtual Machine process"));

        // tomcat
        AxileMetricsGroups.MetricsGroup tomcatGroup = getMetricsGroup(metricsGroups, "tomcat");
        assertThat(tomcatGroup.groupName()).isEqualTo("tomcat");
        assertThat(tomcatGroup.metrics())
                .containsOnly(
                        new KeyValue("tomcat.sessions.active.current", null),
                        new KeyValue("tomcat.sessions.active.max", null));
    }

    private AxileMetricsGroups.MetricsGroup getMetricsGroup(AxileMetricsGroups response, String groupName) {
        return response.metricsGroups().stream()
                .filter(group -> group.groupName().equals(groupName))
                .findFirst()
                .get();
    }
}
