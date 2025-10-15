package com.nucleonforge.axile.master.service.serde;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.ServiceScheduledTasks;

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
                  "delegate": {
                    "runnable": {
                      "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.alive"
                    },
                    "expression": "0 0 0/3 1/1 * ?",
                    "nextExecution": {
                      "time": "2025-10-14T06:33:49.999631800Z"
                    }
                  },
                  "enabled": true
                }
              ],
              "fixedDelay": [
                {
                  "delegate": {
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
                    }
                  },
                  "enabled": true
                }
              ],
              "fixedRate": [
                {
                  "delegate": {
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
                    }
                  },
                  "enabled": false
                }
              ],
              "custom": [
                {
                  "delegate": {
                    "runnable": {
                      "target": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig$$Lambda$1969/0x000001ed01b91ca8@1e1c1634"
                    },
                    "trigger": "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig$CustomTrigger@4323cbe0",
                    "lastExecution": {
                      "exception": {
                        "message": "Failed while running custom task",
                        "type": "java.lang.IllegalStateException"
                      },
                      "status": "ERROR",
                      "time": "2025-09-18T15:03:34.132500256Z"
                    }
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
        assertThat(cron.delegate().runnable().target())
                .isEqualTo("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.alive");
        assertThat(cron.delegate().expression()).isEqualTo("0 0 0/3 1/1 * ?");
        assertThat(cron.delegate().nextExecution().time()).isEqualTo("2025-10-14T06:33:49.999631800Z");
        assertThat(cron.delegate().lastExecution()).isNull();

        // FixedDelayTask
        ServiceScheduledTasks.FixedDelayTask fixedDelay =
                serviceScheduledTasks.fixedDelay().get(0);
        assertThat(fixedDelay.enabled()).isTrue();
        assertThat(fixedDelay.delegate().runnable().target())
                .isEqualTo("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedDelayTask");
        assertThat(fixedDelay.delegate().interval()).isEqualTo(2000);
        assertThat(fixedDelay.delegate().initialDelay()).isEqualTo(0);
        assertThat(fixedDelay.delegate().nextExecution().time()).isEqualTo("2025-10-14T06:33:49.063630700Z");
        assertThat(fixedDelay.delegate().lastExecution().status()).isEqualTo("SUCCESS");
        assertThat(fixedDelay.delegate().lastExecution().time()).isEqualTo("2025-10-14T06:33:47.001570800Z");
        assertThat(fixedDelay.delegate().lastExecution().exception()).isNull();

        // FixedRateTask
        ServiceScheduledTasks.FixedRateTask fixedRate =
                serviceScheduledTasks.fixedRate().get(0);
        assertThat(fixedRate.enabled()).isFalse();
        assertThat(fixedRate.delegate().runnable().target())
                .isEqualTo("org.springframework.samples.petclinic.scheduled.SchedulerTestConfig.fixedRateTask");
        assertThat(fixedRate.delegate().interval()).isEqualTo(2000);
        assertThat(fixedRate.delegate().initialDelay()).isEqualTo(100);
        assertThat(fixedRate.delegate().nextExecution().time()).isEqualTo("2025-10-14T06:33:50.086630700Z");
        assertThat(fixedRate.delegate().lastExecution().time()).isEqualTo("2025-10-14T06:33:48.092631800Z");
        assertThat(fixedRate.delegate().lastExecution().status()).isEqualTo("ERROR");
        assertThat(fixedRate.delegate().lastExecution().exception()).isNull();

        // CustomTask
        ServiceScheduledTasks.CustomTask custom = serviceScheduledTasks.custom().get(0);
        assertThat(custom.enabled()).isFalse();
        assertThat(custom.delegate().trigger())
                .isEqualTo(
                        "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig$CustomTrigger@4323cbe0");
        assertThat(custom.delegate().runnable().target())
                .isEqualTo(
                        "org.springframework.samples.petclinic.scheduled.SchedulerTestConfig$$Lambda$1969/0x000001ed01b91ca8@1e1c1634");
        assertThat(custom.delegate().lastExecution().status()).isEqualTo("ERROR");
        assertThat(custom.delegate().lastExecution().time()).isEqualTo("2025-09-18T15:03:34.132500256Z");
        assertThat(custom.delegate().lastExecution().exception().type()).isEqualTo("java.lang.IllegalStateException");
        assertThat(custom.delegate().lastExecution().exception().message())
                .isEqualTo("Failed while running custom task");
    }
}
