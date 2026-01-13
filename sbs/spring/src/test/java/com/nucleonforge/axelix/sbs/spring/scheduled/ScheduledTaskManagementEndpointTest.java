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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
import static org.assertj.core.api.Assertions.assertThat;

// TODO: Revisit this test design.
/**
 * Integration tests for enable/disable capabilities of {@link AxelixScheduledTasksEndpoint}.
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
// TODO: This test should be merged into AxelixScheduledTasksEndpointTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ScheduledTaskManagementEndpointTest.ScheduledTaskManagementEndpointTestConfiguration.class)
@TestPropertySource(properties = {"management.endpoints.web.exposure.include=axelix-scheduled-tasks"})
class ScheduledTaskManagementEndpointTest {

    private static final String CRON_TASK_ID =
            ScheduledTaskManagementEndpointTest.ScheduledTaskManagementEndpointTestConfiguration.class.getName()
                    + ".testCronTask";
    private static final String CRON_TASK_ID_FOR_MUTATION =
            ScheduledTaskManagementEndpointTest.ScheduledTaskManagementEndpointTestConfiguration.class.getName()
                    + ".testCronTaskForMutation";
    private static final String FIXED_DELAY_TASK_ID =
            ScheduledTaskManagementEndpointTest.ScheduledTaskManagementEndpointTestConfiguration.class.getName()
                    + ".testFixedDelayTask";
    private static final String FIXED_RATE_TASK_ID =
            ScheduledTaskManagementEndpointTest.ScheduledTaskManagementEndpointTestConfiguration.class.getName()
                    + ".testFixedRateTask";
    private static final String CUSTOM_TASK_ID =
            ScheduledTaskManagementEndpointTest.ScheduledTaskManagementEndpointTestConfiguration.CustomTestTask.class
                    .getName();

    private static final String CUSTOM_TRIGGER = "CustomTestTrigger";

    @Autowired
    private TestRestTemplate restTemplate;

    private static volatile boolean cronFlag = false;

    private static volatile boolean fixedDelayFlag = false;

    private static volatile boolean fixedRateFlag = false;

    private static volatile boolean customTaskFlag = false;

    @Test
    void shouldEnableDisabledTask_testCronTask() throws InterruptedException {
        String taskId = CRON_TASK_ID;

        forceDisableTask(taskId);
        Thread.sleep(200);
        cronFlag = false;
        Thread.sleep(1200);
        assertThat(cronFlag).isFalse();

        assertThatJson(getScheduledTasks()).node("cron").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });
    }

    @Test
    void shouldForceRescheduleEnabledTask_testCronTask() throws InterruptedException {
        String taskId = CRON_TASK_ID;

        forceDisableTask(taskId);
        Thread.sleep(200);
        cronFlag = false;
        Thread.sleep(1200);
        assertThat(cronFlag).isFalse();

        assertThatJson(getScheduledTasks()).node("cron").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });

        enableScheduledTask(taskId);
        Thread.sleep(1200);
        assertThat(cronFlag).isTrue();

        assertThatJson(getScheduledTasks()).node("cron").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(true);
        });
    }

    @Test
    void shouldEnableDisabledTask_testFixedDelayTask() throws InterruptedException {
        String taskId = FIXED_DELAY_TASK_ID;

        forceDisableTask(taskId);
        Thread.sleep(200);
        fixedDelayFlag = false;
        Thread.sleep(200);
        assertThat(fixedDelayFlag).isFalse();

        assertThatJson(getScheduledTasks()).node("fixedDelay").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });
    }

    @Test
    void shouldForceRescheduleEnabledTask_testFixedDelayTask() throws InterruptedException {
        String taskId = FIXED_DELAY_TASK_ID;

        forceDisableTask(taskId);
        Thread.sleep(200);
        fixedDelayFlag = false;
        Thread.sleep(200);
        assertThat(fixedDelayFlag).isFalse();

        assertThatJson(getScheduledTasks()).node("fixedDelay").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });

        enableScheduledTask(taskId);
        Thread.sleep(200);
        assertThat(fixedDelayFlag).isTrue();

        assertThatJson(getScheduledTasks()).node("fixedDelay").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(true);
        });
    }

    @Test
    void shouldEnableDisabledTask_testFixedRateTask() throws InterruptedException {
        String taskId = FIXED_RATE_TASK_ID;

        forceDisableTask(taskId);
        Thread.sleep(200);
        fixedRateFlag = false;
        Thread.sleep(200);
        assertThat(fixedRateFlag).isFalse();

        assertThatJson(getScheduledTasks()).node("fixedRate").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });
    }

    @Test
    void shouldForceRescheduleEnabledTask_testFixedRateTask() throws InterruptedException {
        String taskId = FIXED_RATE_TASK_ID;

        forceDisableTask(taskId);
        Thread.sleep(200);
        fixedRateFlag = false;
        Thread.sleep(200);
        assertThat(fixedRateFlag).isFalse();

        assertThatJson(getScheduledTasks()).node("fixedRate").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });

        enableScheduledTask(taskId);
        Thread.sleep(200);
        assertThat(fixedRateFlag).isTrue();

        assertThatJson(getScheduledTasks()).node("fixedRate").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(true);
        });
    }

    @Test
    void shouldEnableDisabledTask_customTestTask() throws InterruptedException {
        String taskId = CUSTOM_TASK_ID;

        forceDisableTask(taskId);
        Thread.sleep(200);
        customTaskFlag = false;
        Thread.sleep(200);
        assertThat(customTaskFlag).isFalse();

        assertThatJson(getScheduledTasks()).node("custom").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("trigger").isEqualTo(CUSTOM_TRIGGER);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });
    }

    @Test
    void shouldForceRescheduleEnabledTask_customTestTask() throws InterruptedException {
        String taskId = CUSTOM_TASK_ID;

        forceDisableTask(taskId);
        Thread.sleep(200);
        customTaskFlag = false;
        Thread.sleep(200);
        assertThat(customTaskFlag).isFalse();

        assertThatJson(getScheduledTasks()).node("custom").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });

        enableScheduledTask(taskId);
        Thread.sleep(200);
        assertThat(customTaskFlag).isTrue();

        assertThatJson(getScheduledTasks()).node("custom").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(true);
        });
    }

    @Test
    void shouldMutateCronExpression_testCronTask() throws InterruptedException {
        String newCronExpression = "*/5 * * * * *";
        ScheduledTaskMutationRequest request =
                new ScheduledTaskMutationRequest(CRON_TASK_ID_FOR_MUTATION, newCronExpression);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/actuator/axelix-scheduled-tasks", defaultEntityForMutation(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.NO_CONTENT, ResponseEntity::getStatusCode);
        assertThatJson(getScheduledTasks()).node("cron").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(CRON_TASK_ID_FOR_MUTATION);
            assertThatJson(task).node("expression").isEqualTo(newCronExpression);
        });
    }

    private void enableScheduledTask(String target) {
        ScheduledTaskToggleRequest request = new ScheduledTaskToggleRequest(target);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/actuator/axelix-scheduled-tasks/enable", defaultEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.NO_CONTENT, ResponseEntity::getStatusCode);
    }

    private void forceDisableTask(String targetScheduledTask) {
        ScheduledTaskToggleRequest request = new ScheduledTaskToggleRequest(targetScheduledTask);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/actuator/axelix-scheduled-tasks/disable?force=true", defaultEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.NO_CONTENT, ResponseEntity::getStatusCode);
    }

    private String getScheduledTasks() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/axelix-scheduled-tasks", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        return response.getBody();
    }

    private HttpEntity<ScheduledTaskToggleRequest> defaultEntity(ScheduledTaskToggleRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(request, headers);
    }

    private HttpEntity<ScheduledTaskMutationRequest> defaultEntityForMutation(ScheduledTaskMutationRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(request, headers);
    }

    @TestConfiguration
    @EnableScheduling
    static class ScheduledTaskManagementEndpointTestConfiguration implements SchedulingConfigurer {

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
        public ServiceScheduledTasksAssembler serviceScheduledTasksAssembler(
                ObjectProvider<ScheduledTaskHolder> taskHolders, ScheduledTaskService service) {
            return new DefaultServiceScheduledTasksAssembler(
                    taskHolders.orderedStream().toList(), service);
        }

        @Bean
        public AxelixScheduledTasksEndpoint scheduledTasksEndpointExtension(
                ScheduledTaskService service, ServiceScheduledTasksAssembler serviceScheduledTasksAssembler) {
            return new AxelixScheduledTasksEndpoint(service, serviceScheduledTasksAssembler);
        }

        @Scheduled(cron = "*/1 * * * * *")
        public void testCronTask() {
            cronFlag = true;
        }

        @Scheduled(cron = "*/1 * * * * *")
        public void testCronTaskForMutation() {}

        @Scheduled(fixedDelay = 100)
        public void testFixedDelayTask() {
            fixedDelayFlag = true;
        }

        @Scheduled(fixedRate = 100, initialDelay = 50)
        public void testFixedRateTask() {
            fixedRateFlag = true;
        }

        @Override
        public void configureTasks(ScheduledTaskRegistrar registrar) {
            registrar.addTriggerTask(new CustomTestTask(), new CustomTestTrigger());
        }

        static class CustomTestTask implements Runnable {
            @Override
            public void run() {
                customTaskFlag = true;
            }

            @Override
            public String toString() {
                return CUSTOM_TASK_ID;
            }
        }

        static class CustomTestTrigger implements Trigger {
            @Override
            @Nullable
            public Instant nextExecution(@NonNull TriggerContext triggerContext) {
                return Instant.now().plusMillis(100);
            }

            @Override
            public String toString() {
                return CUSTOM_TRIGGER;
            }
        }
    }
}
