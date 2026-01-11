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
package com.nucleonforge.axelix.sbs.spring.scheduled;

import java.time.Instant;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.scheduling.config.ScheduledTaskHolder;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.test.context.TestPropertySource;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixScheduledTasksEndpoint}
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(AxelixScheduledTasksEndpointTest.ScheduledTasksEndpointExtensionTestConfiguration.class)
@TestPropertySource(properties = {"management.endpoints.web.exposure.include=axelix-scheduledtasks"})
class AxelixScheduledTasksEndpointTest {

    private static final String EXPECTED_JSON =
            // language=json
            """
      {
        "cron": [
         {
             "delegate": {
                  "runnable": {
                     "target": "com.nucleonforge.axelix.sbs.spring.scheduled.AxelixScheduledTasksEndpointTest$ScheduledTasksEndpointExtensionTestConfiguration.testCronTask"
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
                      "target": "com.nucleonforge.axelix.sbs.spring.scheduled.AxelixScheduledTasksEndpointTest$ScheduledTasksEndpointExtensionTestConfiguration.testFixedDelayTask"
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
                      "target": "com.nucleonforge.axelix.sbs.spring.scheduled.AxelixScheduledTasksEndpointTest$ScheduledTasksEndpointExtensionTestConfiguration.testFixedRateTask"
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
                      "target": "com.nucleonforge.axelix.sbs.spring.scheduled.AxelixScheduledTasksEndpointTest$ScheduledTasksEndpointExtensionTestConfiguration$CustomTestTask"
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
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/axelix-scheduledtasks", String.class);

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
        public ScheduledTasksRegistry scheduledTaskRegistry(ScheduledAnnotationBeanPostProcessor processor) {
            return new ScheduledTasksRegistry(processor);
        }

        @Bean
        TaskRescheduler testTriggerBasedTaskRescheduler(TaskScheduler taskScheduler) {
            return new TriggerBasedTaskRescheduler(taskScheduler);
        }

        @Bean
        TaskRescheduler testIntervalBasedTaskRescheduler(TaskScheduler taskScheduler) {
            return new IntervalBasedTaskRescheduler(taskScheduler);
        }

        @Bean
        public ScheduledTaskService scheduledTaskService(
                ScheduledTasksRegistry registry, List<TaskRescheduler> taskReschedulers) {
            return new ScheduledTaskService(registry, taskReschedulers);
        }

        @Bean
        public AxelixScheduledTasksEndpoint scheduledTasksEndpointExtension(
                ObjectProvider<ScheduledTaskHolder> taskHolders, ScheduledTasksRegistry registry) {
            return new AxelixScheduledTasksEndpoint(taskHolders.orderedStream().toList(), registry);
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
                return "com.nucleonforge.axelix.sbs.spring.scheduled.AxelixScheduledTasksEndpointTest$ScheduledTasksEndpointExtensionTestConfiguration$CustomTestTask";
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
