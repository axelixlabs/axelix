package com.nucleonforge.axile.sbs.spring.scheduled;

import java.time.Instant;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.scheduling.ScheduledTasksEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.test.context.TestPropertySource;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ScheduledTasksEndpointExtension}
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ScheduledTasksEndpointExtensionTest.ScheduledTasksEndpointExtensionTestConfiguration.class)
@TestPropertySource(properties = {"management.endpoints.web.exposure.include=scheduledtasks"})
class ScheduledTasksEndpointExtensionTest {

    private static final String EXPECTED_JSON =
            // language=json
            """
      {
        "cron": [
         {
             "delegate": {
                  "runnable": {
                     "target": "com.nucleonforge.axile.sbs.spring.scheduled.ScheduledTasksEndpointExtensionTest$ScheduledTasksEndpointExtensionTestConfiguration.testCronTask"
                 },
                  "expression": "*/1 * * * * *"
              },
              "enabled": true
          }
        ],
       "fixedDelay": [
          {
              "delegate": {
                  "runnable": {
                      "target": "com.nucleonforge.axile.sbs.spring.scheduled.ScheduledTasksEndpointExtensionTest$ScheduledTasksEndpointExtensionTestConfiguration.testFixedDelayTask"
                  },
                  "initialDelay": 0,
                  "interval": 1000
              },
              "enabled": true
         }
       ],
        "fixedRate": [
          {
              "delegate": {
                  "runnable": {
                      "target": "com.nucleonforge.axile.sbs.spring.scheduled.ScheduledTasksEndpointExtensionTest$ScheduledTasksEndpointExtensionTestConfiguration.testFixedRateTask"
                  },
                 "initialDelay": 100,
                  "interval": 1000
              },
             "enabled": true
          }
       ],
        "custom": [
          {
              "delegate": {
                  "runnable": {
                      "target": "com.nucleonforge.axile.sbs.spring.scheduled.ScheduledTasksEndpointExtensionTest$ScheduledTasksEndpointExtensionTestConfiguration$CustomTestTask"
                  },
                  "trigger": "CustomTestTrigger"
              },
             "enabled": true
          }
        ]
     }
    """;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnExpectedJson() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/scheduledtasks", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        String body = response.getBody();
        assertThatJson(body).when(IGNORING_ARRAY_ORDER, IGNORING_EXTRA_FIELDS).isEqualTo(EXPECTED_JSON);
    }

    @TestConfiguration
    @EnableScheduling
    static class ScheduledTasksEndpointExtensionTestConfiguration implements SchedulingConfigurer {

        @Bean
        public TaskScheduler taskScheduler() {
            return new ConcurrentTaskScheduler();
        }

        @Bean
        public ScheduledTasksRegistry scheduledTaskRegistry(
                ScheduledAnnotationBeanPostProcessor processor, TaskScheduler scheduler) {
            return new ScheduledTasksRegistry(processor, scheduler);
        }

        @Bean
        public ScheduledTaskService scheduledTaskService(ScheduledTasksRegistry registry) {
            return new ScheduledTaskService(registry);
        }

        @Bean
        public ScheduledTasksEndpointExtension scheduledTasksEndpointExtension(
                ScheduledTasksEndpoint delegate, ScheduledTasksRegistry registry) {
            return new ScheduledTasksEndpointExtension(delegate, registry);
        }

        @Scheduled(cron = "*/1 * * * * *")
        public void testCronTask() {}

        @Scheduled(fixedDelay = 1000)
        public void testFixedDelayTask() {}

        @Scheduled(fixedRate = 1000, initialDelay = 100)
        public void testFixedRateTask() {}

        @Override
        public void configureTasks(ScheduledTaskRegistrar registrar) {
            registrar.addTriggerTask(new CustomTestTask(), new CustomTestTrigger());
        }

        static class CustomTestTask implements Runnable {
            @Override
            public void run() {}

            @Override
            public String toString() {
                return "com.nucleonforge.axile.sbs.spring.scheduled.ScheduledTasksEndpointExtensionTest$ScheduledTasksEndpointExtensionTestConfiguration$CustomTestTask";
            }
        }

        static class CustomTestTrigger implements Trigger {
            @Override
            @Nullable
            public Instant nextExecution(@NonNull TriggerContext triggerContext) {
                return Instant.now().plusSeconds(1);
            }

            @Override
            public String toString() {
                return "CustomTestTrigger";
            }
        }
    }
}
