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
package com.nucleonforge.axile.master.service.serde;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.metrics.MetricsGroupsFeed;
import com.nucleonforge.axile.common.api.metrics.MetricsGroupsFeed.MetricsGroup;
import com.nucleonforge.axile.common.api.metrics.MetricsGroupsFeed.MetricsGroup.MetricDescription;
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
                      "metricName": "jvm.gc.memory.allocated",
                      "description": "Incremented for an increase in the size of the (young) heap memory pool after one GC to before the next"
                    },
                    {
                      "metricName": "jvm.memory.usage.after.gc",
                      "description": "The percentage of long-lived heap pool used after the last GC event, in the range [0..1]"
                    },
                    {
                      "metricName": "jvm.memory.used",
                      "description": "The amount of used memory"
                    }
                  ]
                },
                {
                  "groupName": "process",
                  "metrics": [
                    {
                      "metricName": "process.cpu.time",
                      "description": "The \\"cpu time\\" used by the Java Virtual Machine process"
                    },
                    {
                      "metricName": "process.cpu.usage",
                      "description": "The \\"recent cpu usage\\" for the Java Virtual Machine process"
                    }
                  ]
                },
                {
                  "groupName": "tomcat",
                  "metrics": [
                    {
                      "metricName": "tomcat.sessions.active.current",
                      "description": null
                    },
                    {
                      "metricName": "tomcat.sessions.active.max",
                      "description": null
                    }
                  ]
                }
              ]
            }
        """;

        // when.
        MetricsGroupsFeed metricsGroups = subject.deserialize(response.getBytes(StandardCharsets.UTF_8));

        // then.
        assertThat(metricsGroups.metricsGroups()).isNotEmpty().hasSize(3);

        // jvm
        MetricsGroup jvmGroup = getMetricsGroup(metricsGroups, "jvm");
        assertThat(jvmGroup.groupName()).isEqualTo("jvm");
        assertThat(jvmGroup.metrics())
                .containsOnly(
                        new MetricDescription(
                                "jvm.gc.memory.allocated",
                                "Incremented for an increase in the size of the (young) heap memory pool after one GC to before the next"),
                        new MetricDescription(
                                "jvm.memory.usage.after.gc",
                                "The percentage of long-lived heap pool used after the last GC event, in the range [0..1]"),
                        new MetricDescription("jvm.memory.used", "The amount of used memory"));

        // process
        MetricsGroup processGroup = getMetricsGroup(metricsGroups, "process");
        assertThat(processGroup.groupName()).isEqualTo("process");
        assertThat(processGroup.metrics())
                .containsOnly(
                        new MetricDescription(
                                "process.cpu.time", "The \"cpu time\" used by the Java Virtual Machine process"),
                        new MetricDescription(
                                "process.cpu.usage", "The \"recent cpu usage\" for the Java Virtual Machine process"));

        // tomcat
        MetricsGroup tomcatGroup = getMetricsGroup(metricsGroups, "tomcat");
        assertThat(tomcatGroup.groupName()).isEqualTo("tomcat");
        assertThat(tomcatGroup.metrics())
                .containsOnly(
                        new MetricDescription("tomcat.sessions.active.current", null),
                        new MetricDescription("tomcat.sessions.active.max", null));
    }

    private MetricsGroup getMetricsGroup(MetricsGroupsFeed response, String groupName) {
        return response.metricsGroups().stream()
                .filter(group -> group.groupName().equals(groupName))
                .findFirst()
                .get();
    }
}
