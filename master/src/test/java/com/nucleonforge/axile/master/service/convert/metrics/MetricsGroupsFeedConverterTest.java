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
package com.nucleonforge.axile.master.service.convert.metrics;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.metrics.MetricsGroupsFeed;
import com.nucleonforge.axile.master.api.response.metrics.MetricsGroupsFeedResponse;
import com.nucleonforge.axile.master.service.convert.response.metrics.MetricsGroupsFeedConverter;

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

        // return -> AxileMetricsGroups
        return new MetricsGroupsFeed(List.of(jvmGroup, processGroup, tomcatGroup));
    }
}
