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
package com.axelixlabs.axelix.master.service.convert.metrics;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.metrics.MetricsGroupsFeed;
import com.axelixlabs.axelix.master.api.external.response.metrics.MetricsGroupsFeedResponse;
import com.axelixlabs.axelix.master.service.convert.response.metrics.MetricsGroupsFeedConverter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link MetricsGroupsFeedConverter}.
 *
 * @author Sergey Cherkasov
 */
class MetricsGroupsFeedConverterTest {

    private MetricsGroupsFeedConverter subject;

    @BeforeEach
    void setUp() {
        subject = new MetricsGroupsFeedConverter();
    }

    @Test
    void testConvertHappyPath() {
        // when.
        MetricsGroupsFeedResponse response = subject.convertInternal(metricsGroupsFeed());

        // then.
        assertThat(response.metricsGroups()).isNotEmpty().hasSize(3);

        // jvm
        MetricsGroupsFeedResponse.MetricsGroup jvmGroup = getMetricsGroup(response, "jvm");
        assertThat(jvmGroup.groupName()).isEqualTo("jvm");
        assertThat(jvmGroup.metrics())
                .containsOnly(
                        new MetricsGroupsFeedResponse.MetricsGroup.MetricDescription(
                                "jvm.gc.memory.allocated",
                                "Incremented for an increase in the size of the (young) heap memory pool after one GC to before the next"),
                        new MetricsGroupsFeedResponse.MetricsGroup.MetricDescription(
                                "jvm.memory.usage.after.gc",
                                "The percentage of long-lived heap pool used after the last GC event, in the range [0..1]"),
                        new MetricsGroupsFeedResponse.MetricsGroup.MetricDescription(
                                "jvm.memory.used", "The amount of used memory"));

        // process
        MetricsGroupsFeedResponse.MetricsGroup processGroup = getMetricsGroup(response, "process");
        assertThat(processGroup.groupName()).isEqualTo("process");
        assertThat(processGroup.metrics())
                .containsOnly(
                        new MetricsGroupsFeedResponse.MetricsGroup.MetricDescription(
                                "process.cpu.time", "The \"cpu time\" used by the Java Virtual Machine process"),
                        new MetricsGroupsFeedResponse.MetricsGroup.MetricDescription(
                                "process.cpu.usage", "The \"recent cpu usage\" for the Java Virtual Machine process"));

        // tomcat
        MetricsGroupsFeedResponse.MetricsGroup tomcatGroup = getMetricsGroup(response, "tomcat");
        assertThat(tomcatGroup.groupName()).isEqualTo("tomcat");
        assertThat(tomcatGroup.metrics())
                .containsOnly(
                        new MetricsGroupsFeedResponse.MetricsGroup.MetricDescription(
                                "tomcat.sessions.active.current", null),
                        new MetricsGroupsFeedResponse.MetricsGroup.MetricDescription(
                                "tomcat.sessions.active.max", null));
    }

    private MetricsGroupsFeedResponse.MetricsGroup getMetricsGroup(
            MetricsGroupsFeedResponse response, String groupName) {
        return response.metricsGroups().stream()
                .filter(group -> group.groupName().equals(groupName))
                .findFirst()
                .get();
    }

    private static MetricsGroupsFeed metricsGroupsFeed() {
        // jvm
        List<MetricsGroupsFeed.MetricsGroup.MetricDescription> jvm = List.of(
                new MetricsGroupsFeed.MetricsGroup.MetricDescription(
                        "jvm.gc.memory.allocated",
                        "Incremented for an increase in the size of the (young) heap memory pool after one GC to before the next"),
                new MetricsGroupsFeed.MetricsGroup.MetricDescription(
                        "jvm.memory.usage.after.gc",
                        "The percentage of long-lived heap pool used after the last GC event, in the range [0..1]"),
                new MetricsGroupsFeed.MetricsGroup.MetricDescription("jvm.memory.used", "The amount of used memory"));

        MetricsGroupsFeed.MetricsGroup jvmGroup = new MetricsGroupsFeed.MetricsGroup("jvm", jvm);

        // process
        List<MetricsGroupsFeed.MetricsGroup.MetricDescription> process = List.of(
                new MetricsGroupsFeed.MetricsGroup.MetricDescription(
                        "process.cpu.time", "The \"cpu time\" used by the Java Virtual Machine process"),
                new MetricsGroupsFeed.MetricsGroup.MetricDescription(
                        "process.cpu.usage", "The \"recent cpu usage\" for the Java Virtual Machine process"));

        MetricsGroupsFeed.MetricsGroup processGroup = new MetricsGroupsFeed.MetricsGroup("process", process);

        // tomcat
        List<MetricsGroupsFeed.MetricsGroup.MetricDescription> tomcat = List.of(
                new MetricsGroupsFeed.MetricsGroup.MetricDescription("tomcat.sessions.active.current", null),
                new MetricsGroupsFeed.MetricsGroup.MetricDescription("tomcat.sessions.active.max", null));

        MetricsGroupsFeed.MetricsGroup tomcatGroup = new MetricsGroupsFeed.MetricsGroup("tomcat", tomcat);

        // return -> AxelixMetricsGroups
        return new MetricsGroupsFeed(List.of(jvmGroup, processGroup, tomcatGroup));
    }
}
