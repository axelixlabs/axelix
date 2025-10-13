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
              "cron" : [ {
                "expression" : "0 0 0/3 1/1 * ?",
                "nextExecution" : {
                  "time" : "2025-09-18T17:59:59.999098218Z"
                },
                "runnable" : {
                  "target" : "com.example.Processor.processOrders"
                }
              }],
              "custom" : [ {
                "lastExecution" : {
                  "exception" : {
                    "message" : "Failed while running custom task",
                    "type" : "java.lang.IllegalStateException"
                  },
                  "status" : "ERROR",
                  "time" : "2025-09-18T15:03:34.132500256Z"
                },
                "runnable" : {
                  "target" : "com.example.Processor$CustomTriggeredRunnable@438fc55e"
                },
                "trigger" : "com.example.Processor$CustomTrigger@56567e9b"
              }],
              "fixedDelay" : [ {
                "initialDelay" : 0,
                "interval" : 5000,
                "lastExecution" : {
                  "status" : "SUCCESS",
                  "time" : "2025-09-18T15:03:34.113091965Z"
                },
                "nextExecution" : {
                  "time" : "2025-09-18T15:03:39.117492423Z"
                },
                "runnable" : {
                  "target" : "com.example.Processor.purge"
                }
              } ],
              "fixedRate" : [ {
                "initialDelay" : 10000,
                "interval" : 3000,
                "nextExecution" : {
                  "time" : "2025-09-18T15:03:44.102073608Z"
                },
                "runnable" : {
                  "target" : "com.example.Processor.retrieveIssues"
                }
              } ]
            }
          """;

        // when.
        ServiceScheduledTasks serviceScheduledTasks = subject.deserialize(response.getBytes(StandardCharsets.UTF_8));

        // Cron
        ServiceScheduledTasks.Cron cron = serviceScheduledTasks.cron().get(0);
        assertThat(cron.runnable().target()).isEqualTo("com.example.Processor.processOrders");
        assertThat(cron.expression()).isEqualTo("0 0 0/3 1/1 * ?");
        assertThat(cron.nextExecution().time()).isEqualTo("2025-09-18T17:59:59.999098218Z");
        assertThat(cron.lastExecution()).isNull();

        // Custom
        ServiceScheduledTasks.Custom custom = serviceScheduledTasks.custom().get(0);
        assertThat(custom.trigger()).isEqualTo("com.example.Processor$CustomTrigger@56567e9b");
        assertThat(custom.runnable().target()).isEqualTo("com.example.Processor$CustomTriggeredRunnable@438fc55e");
        assertThat(custom.lastExecution().status()).isEqualTo("ERROR");
        assertThat(custom.lastExecution().time()).isEqualTo("2025-09-18T15:03:34.132500256Z");
        assertThat(custom.lastExecution().exception().type()).isEqualTo("java.lang.IllegalStateException");
        assertThat(custom.lastExecution().exception().message()).isEqualTo("Failed while running custom task");

        // FixedDelay
        ServiceScheduledTasks.FixedDelay fixedDelay =
                serviceScheduledTasks.fixedDelay().get(0);
        assertThat(fixedDelay.runnable().target()).isEqualTo("com.example.Processor.purge");
        assertThat(fixedDelay.interval()).isEqualTo(5000);
        assertThat(fixedDelay.initialDelay()).isEqualTo(0);
        assertThat(fixedDelay.nextExecution().time()).isEqualTo("2025-09-18T15:03:39.117492423Z");
        assertThat(fixedDelay.lastExecution().status()).isEqualTo("SUCCESS");
        assertThat(fixedDelay.lastExecution().time()).isEqualTo("2025-09-18T15:03:34.113091965Z");
        assertThat(fixedDelay.lastExecution().exception()).isNull();

        // FixedRate
        ServiceScheduledTasks.FixedRate fixedRate =
                serviceScheduledTasks.fixedRate().get(0);
        assertThat(fixedRate.runnable().target()).isEqualTo("com.example.Processor.retrieveIssues");
        assertThat(fixedRate.interval()).isEqualTo(3000);
        assertThat(fixedRate.initialDelay()).isEqualTo(10000);
        assertThat(fixedRate.nextExecution().time()).isEqualTo("2025-09-18T15:03:44.102073608Z");
        assertThat(fixedRate.lastExecution()).isNull();
    }
}
