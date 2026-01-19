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
package com.nucleonforge.axelix.sbs.spring.scheduled;

import java.time.Instant;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
 * @author Sergey Cherkasov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(AxelixScheduledTasksEndpointTest.ScheduledTasksEndpointExtensionTestConfiguration.class)
@TestPropertySource(properties = {"management.endpoints.web.exposure.include=axelix-scheduled-tasks"})
class AxelixScheduledTasksEndpointTest {

    private static final String EXPECTED_JSON =
            // language=json
            """
      {
        "cron": [
         {
              "runnable": {
               "target": "com.nucleonforge.axelix.sbs.spring.scheduled.AxelixScheduledTasksEndpointTest$ScheduledTasksEndpointExtensionTestConfiguration.testCronTask"
              },
              "expression": "*/1 * * * * *",
              "enabled": true
          }
        ],
       "fixedDelay": [
          {
              "runnable": {
               "target": "com.nucleonforge.axelix.sbs.spring.scheduled.AxelixScheduledTasksEndpointTest$ScheduledTasksEndpointExtensionTestConfiguration.testFixedDelayTask"
              },
              "initialDelay": 0,
              "interval": 1000,
              "enabled": true
         }
       ],
        "fixedRate": [
          {
             "runnable": {
               "target": "com.nucleonforge.axelix.sbs.spring.scheduled.AxelixScheduledTasksEndpointTest$ScheduledTasksEndpointExtensionTestConfiguration.testFixedRateTask"
             },
             "initialDelay": 100,
             "interval": 1000,
             "enabled": true
          }
       ],
        "custom": [
          {
             "runnable": {
               "target": "com.nucleonforge.axelix.sbs.spring.scheduled.AxelixScheduledTasksEndpointTest$ScheduledTasksEndpointExtensionTestConfiguration$CustomTestTask"
             },
             "trigger": "CustomTestTrigger",
             "enabled": true
          }
        ]
     }
    """;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnExpectedJson() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/axelix-scheduled-tasks", String.class);

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
            return new ScheduledTasksRegistry(List.of(processor));
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
                ScheduledTasksRegistry registry,
                List<TaskRescheduler> taskReschedulers,
                ThreadPoolTaskExecutor taskExecutor) {
            return new ScheduledTaskService(registry, taskReschedulers, taskExecutor);
        }

        @Bean
        public ScheduledTasksAssembler serviceScheduledTasksAssembler(ScheduledTasksRegistry scheduledTasksRegistry) {
            return new DefaultScheduledTasksAssembler(scheduledTasksRegistry);
        }

        @Bean
        public AxelixScheduledTasksEndpoint scheduledTasksEndpointExtension(
                ScheduledTaskService service, ScheduledTasksAssembler scheduledTasksAssembler) {
            return new AxelixScheduledTasksEndpoint(service, scheduledTasksAssembler);
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
