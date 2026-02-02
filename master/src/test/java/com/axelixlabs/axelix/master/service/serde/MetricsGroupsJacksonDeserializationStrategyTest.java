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
package com.axelixlabs.axelix.master.service.serde;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.metrics.MetricsGroupsFeed;
import com.axelixlabs.axelix.common.api.metrics.MetricsGroupsFeed.MetricsGroup;
import com.axelixlabs.axelix.common.api.metrics.MetricsGroupsFeed.MetricsGroup.MetricDescription;
import com.axelixlabs.axelix.master.service.serde.metrics.MetricsGroupsJacksonDeserializationStrategy;

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
    void shouldDeserializeAxelixMetricsGroups() {
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
        assertThat(metricsGroups.getMetricsGroups()).isNotEmpty().hasSize(3);

        // jvm
        MetricsGroup jvmGroup = getMetricsGroup(metricsGroups, "jvm");
        assertThat(jvmGroup.getGroupName()).isEqualTo("jvm");
        assertThat(jvmGroup.getMetrics())
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
        assertThat(processGroup.getGroupName()).isEqualTo("process");
        assertThat(processGroup.getMetrics())
                .containsOnly(
                        new MetricDescription(
                                "process.cpu.time", "The \"cpu time\" used by the Java Virtual Machine process"),
                        new MetricDescription(
                                "process.cpu.usage", "The \"recent cpu usage\" for the Java Virtual Machine process"));

        // tomcat
        MetricsGroup tomcatGroup = getMetricsGroup(metricsGroups, "tomcat");
        assertThat(tomcatGroup.getGroupName()).isEqualTo("tomcat");
        assertThat(tomcatGroup.getMetrics())
                .containsOnly(
                        new MetricDescription("tomcat.sessions.active.current", null),
                        new MetricDescription("tomcat.sessions.active.max", null));
    }

    private MetricsGroup getMetricsGroup(MetricsGroupsFeed response, String groupName) {
        return response.getMetricsGroups().stream()
                .filter(group -> group.getGroupName().equals(groupName))
                .findFirst()
                .get();
    }
}
