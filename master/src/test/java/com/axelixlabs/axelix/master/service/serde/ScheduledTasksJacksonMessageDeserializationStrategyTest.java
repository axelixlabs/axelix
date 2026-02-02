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

import com.axelixlabs.axelix.common.api.ServiceScheduledTasks;

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
        ServiceScheduledTasks.CronTask cron = serviceScheduledTasks.getCron().get(0);
        assertThat(cron.isEnabled()).isTrue();
        assertThat(cron.getRunnable().getTarget())
                .isEqualTo("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.alive");
        assertThat(cron.getExpression()).isEqualTo("0 0 0/3 1/1 * ?");
        assertThat(cron.getNextExecution().getTime()).isEqualTo("2025-10-14T06:33:49.999631800Z");
        assertThat(cron.getLastExecution()).isNull();

        // FixedDelayTask
        ServiceScheduledTasks.FixedDelayTask fixedDelay =
                serviceScheduledTasks.getFixedDelay().get(0);
        assertThat(fixedDelay.isEnabled()).isTrue();
        assertThat(fixedDelay.getRunnable().getTarget())
                .isEqualTo("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedDelayTask");
        assertThat(fixedDelay.getInterval()).isEqualTo(2000);
        assertThat(fixedDelay.getInitialDelay()).isEqualTo(0);
        assertThat(fixedDelay.getNextExecution().getTime()).isEqualTo("2025-10-14T06:33:49.063630700Z");
        assertThat(fixedDelay.getLastExecution().getStatus()).isEqualTo("SUCCESS");
        assertThat(fixedDelay.getLastExecution().getTime()).isEqualTo("2025-10-14T06:33:47.001570800Z");
        assertThat(fixedDelay.getLastExecution().getException()).isNull();

        // FixedRateTask
        ServiceScheduledTasks.FixedRateTask fixedRate =
                serviceScheduledTasks.getFixedRate().get(0);
        assertThat(fixedRate.isEnabled()).isFalse();
        assertThat(fixedRate.getRunnable().getTarget())
                .isEqualTo("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");
        assertThat(fixedRate.getInterval()).isEqualTo(2000);
        assertThat(fixedRate.getInitialDelay()).isEqualTo(100);
        assertThat(fixedRate.getNextExecution().getTime()).isEqualTo("2025-10-14T06:33:50.086630700Z");
        assertThat(fixedRate.getLastExecution().getTime()).isEqualTo("2025-10-14T06:33:48.092631800Z");
        assertThat(fixedRate.getLastExecution().getStatus()).isEqualTo("ERROR");
        assertThat(fixedRate.getLastExecution().getException()).isNull();

        // CustomTask
        ServiceScheduledTasks.CustomTask custom =
                serviceScheduledTasks.getCustom().get(0);
        assertThat(custom.isEnabled()).isFalse();
        assertThat(custom.getTrigger())
                .isEqualTo(
                        "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig$CustomTrigger@4323cbe0");
        assertThat(custom.getRunnable().getTarget())
                .isEqualTo(
                        "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig$$Lambda$1969/0x000001ed01b91ca8@1e1c1634");
        assertThat(custom.getNextExecution().getTime()).isEqualTo("2025-10-14T06:33:50.086630700Z");
        assertThat(custom.getLastExecution().getStatus()).isEqualTo("ERROR");
        assertThat(custom.getLastExecution().getTime()).isEqualTo("2025-09-18T15:03:34.132500256Z");
        assertThat(custom.getLastExecution().getException().getType()).isEqualTo("java.lang.IllegalStateException");
        assertThat(custom.getLastExecution().getException().getMessage()).isEqualTo("Failed while running custom task");
    }
}
