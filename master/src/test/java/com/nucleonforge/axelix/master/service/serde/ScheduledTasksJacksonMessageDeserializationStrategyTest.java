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
package com.nucleonforge.axelix.master.service.serde;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axelix.common.api.ServiceScheduledTasks;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ScheduledTasksJacksonMessageDeserializationStrategy}.
 *
 * @author Sergey Cherkasov
 */
public class ScheduledTasksJacksonMessageDeserializationStrategyTest {

    private final ScheduledTasksJacksonMessageDeserializationStrategy subject =
            new ScheduledTasksJacksonMessageDeserializationStrategy(new ObjectMapper());

    @Test
    void shouldDeserializeServiceScheduledTasks() {
        // language=json
        String response =
                """
            {
              "cron": [
                {
                    "runnable": {
                      "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.alive"
                    },
                    "expression": "0 0 0/3 1/1 * ?",
                    "nextExecution": {
                      "time": "2025-10-14T06:33:49.999631800Z"
                    },
                    "enabled": true
                }
              ],
              "fixedDelay": [
                {
                    "runnable": {
                      "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedDelayTask"
                    },
                    "initialDelay": 0,
                    "interval": 2000,
                    "nextExecution": {
                      "time": "2025-10-14T06:33:49.063630700Z"
                    },
                    "lastExecution": {
                      "exception": null,
                      "time": "2025-10-14T06:33:47.001570800Z",
                      "status": "SUCCESS"
                    },
                    "enabled": true
                }
              ],
              "fixedRate": [
                {
                    "runnable": {
                      "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask"
                    },
                    "initialDelay": 100,
                    "interval": 2000,
                    "nextExecution": {
                      "time": "2025-10-14T06:33:50.086630700Z"
                    },
                    "lastExecution": {
                      "exception": null,
                      "time": "2025-10-14T06:33:48.092631800Z",
                      "status": "ERROR"
                    },
                    "enabled": false
                }
              ],
              "custom": [
                {
                    "runnable": {
                      "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig$$Lambda$1969/0x000001ed01b91ca8@1e1c1634"
                    },
                    "trigger": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig$CustomTrigger@4323cbe0",
                    "nextExecution": {
                      "time": "2025-10-14T06:33:50.086630700Z"
                    },
                    "lastExecution": {
                      "exception": {
                        "message": "Failed while running custom task",
                        "type": "java.lang.IllegalStateException"
                      },
                      "status": "ERROR",
                      "time": "2025-09-18T15:03:34.132500256Z"
                    },
                    "enabled": false
                }
              ]
            }
            """;

        // when.
        ServiceScheduledTasks serviceScheduledTasks = subject.deserialize(response.getBytes(StandardCharsets.UTF_8));

        // CronTask
        ServiceScheduledTasks.CronTask cron = serviceScheduledTasks.cron().get(0);
        assertThat(cron.enabled()).isTrue();
        assertThat(cron.runnable().target())
                .isEqualTo("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.alive");
        assertThat(cron.expression()).isEqualTo("0 0 0/3 1/1 * ?");
        assertThat(cron.nextExecution().time()).isEqualTo("2025-10-14T06:33:49.999631800Z");
        assertThat(cron.lastExecution()).isNull();

        // FixedDelayTask
        ServiceScheduledTasks.FixedDelayTask fixedDelay =
                serviceScheduledTasks.fixedDelay().get(0);
        assertThat(fixedDelay.enabled()).isTrue();
        assertThat(fixedDelay.runnable().target())
                .isEqualTo("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedDelayTask");
        assertThat(fixedDelay.interval()).isEqualTo(2000);
        assertThat(fixedDelay.initialDelay()).isEqualTo(0);
        assertThat(fixedDelay.nextExecution().time()).isEqualTo("2025-10-14T06:33:49.063630700Z");
        assertThat(fixedDelay.lastExecution().status()).isEqualTo("SUCCESS");
        assertThat(fixedDelay.lastExecution().time()).isEqualTo("2025-10-14T06:33:47.001570800Z");
        assertThat(fixedDelay.lastExecution().exception()).isNull();

        // FixedRateTask
        ServiceScheduledTasks.FixedRateTask fixedRate =
                serviceScheduledTasks.fixedRate().get(0);
        assertThat(fixedRate.enabled()).isFalse();
        assertThat(fixedRate.runnable().target())
                .isEqualTo("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");
        assertThat(fixedRate.interval()).isEqualTo(2000);
        assertThat(fixedRate.initialDelay()).isEqualTo(100);
        assertThat(fixedRate.nextExecution().time()).isEqualTo("2025-10-14T06:33:50.086630700Z");
        assertThat(fixedRate.lastExecution().time()).isEqualTo("2025-10-14T06:33:48.092631800Z");
        assertThat(fixedRate.lastExecution().status()).isEqualTo("ERROR");
        assertThat(fixedRate.lastExecution().exception()).isNull();

        // CustomTask
        ServiceScheduledTasks.CustomTask custom = serviceScheduledTasks.custom().get(0);
        assertThat(custom.enabled()).isFalse();
        assertThat(custom.trigger())
                .isEqualTo(
                        "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig$CustomTrigger@4323cbe0");
        assertThat(custom.runnable().target())
                .isEqualTo(
                        "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig$$Lambda$1969/0x000001ed01b91ca8@1e1c1634");
        assertThat(custom.nextExecution().time()).isEqualTo("2025-10-14T06:33:50.086630700Z");
        assertThat(custom.lastExecution().status()).isEqualTo("ERROR");
        assertThat(custom.lastExecution().time()).isEqualTo("2025-09-18T15:03:34.132500256Z");
        assertThat(custom.lastExecution().exception().type()).isEqualTo("java.lang.IllegalStateException");
        assertThat(custom.lastExecution().exception().message()).isEqualTo("Failed while running custom task");
    }
}
