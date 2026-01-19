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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.test.context.TestPropertySource;

import com.nucleonforge.axelix.common.api.request.ScheduledTaskCronExpressionModifyRequest;
import com.nucleonforge.axelix.common.api.request.ScheduledTaskExecuteRequest;
import com.nucleonforge.axelix.common.api.request.ScheduledTaskIntervalModifyRequest;
import com.nucleonforge.axelix.common.api.request.ScheduledTaskToggleRequest;

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

    // Cron
    private static final String CRON_TASK_ID =
            ScheduledTaskManagementEndpointTest.ScheduledTaskManagementEndpointTestConfiguration.class.getName()
                    + ".testCronTask";
    private static final String CRON_TASK_ID_FOR_MODIFY =
            ScheduledTaskManagementEndpointTest.ScheduledTaskManagementEndpointTestConfiguration.class.getName()
                    + ".testCronTaskForModify";

    // FixedDelay
    private static final String FIXED_DELAY_TASK_ID =
            ScheduledTaskManagementEndpointTest.ScheduledTaskManagementEndpointTestConfiguration.class.getName()
                    + ".testFixedDelayTask";
    private static final String FIXED_DELAY_TASK_ID_FOR_MODIFY =
            ScheduledTaskManagementEndpointTest.ScheduledTaskManagementEndpointTestConfiguration.class.getName()
                    + ".testFixedDelayTaskForModify";
    private static final String FIXED_DELAY_TASK_ID_FOR_EXECUTE =
            ScheduledTaskManagementEndpointTest.ScheduledTaskManagementEndpointTestConfiguration.class.getName()
                    + ".testFixedDelayTaskForExecute";

    // FixedRate
    private static final String FIXED_RATE_TASK_ID =
            ScheduledTaskManagementEndpointTest.ScheduledTaskManagementEndpointTestConfiguration.class.getName()
                    + ".testFixedRateTask";
    private static final String FIXED_RATE_TASK_ID_FOR_MODIFY =
            ScheduledTaskManagementEndpointTest.ScheduledTaskManagementEndpointTestConfiguration.class.getName()
                    + ".testFixedRateTaskForModify";
    private static final String FIXED_RATE_TASK_ID_FOR_EXECUTE =
            ScheduledTaskManagementEndpointTest.ScheduledTaskManagementEndpointTestConfiguration.class.getName()
                    + ".testFixedRateTaskForExecute";

    // Custom
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
    void shouldModifyCronExpression_testCronTask() {
        String newCronExpression = "*/5 * * * * *";

        ScheduledTaskCronExpressionModifyRequest request =
                new ScheduledTaskCronExpressionModifyRequest(CRON_TASK_ID_FOR_MODIFY, newCronExpression);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/actuator/axelix-scheduled-tasks/modify/cron-expression", defaultJsonEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.NO_CONTENT, ResponseEntity::getStatusCode);
        assertThatJson(getScheduledTasks()).node("cron").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(CRON_TASK_ID_FOR_MODIFY);
            assertThatJson(task).node("expression").isEqualTo(newCronExpression);
        });
    }

    @Test
    void shouldModifyInterval_testFixedDelay() {
        Long newInterval = 555555L;

        ScheduledTaskIntervalModifyRequest request =
                new ScheduledTaskIntervalModifyRequest(FIXED_DELAY_TASK_ID_FOR_MODIFY, newInterval);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/actuator/axelix-scheduled-tasks/modify/interval", defaultJsonEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.NO_CONTENT, ResponseEntity::getStatusCode);
        assertThatJson(getScheduledTasks()).node("fixedDelay").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(FIXED_DELAY_TASK_ID_FOR_MODIFY);
            assertThatJson(task).node("interval").isEqualTo(newInterval);
        });
    }

    @Test
    void shouldModifyInterval_testFixedRate() {
        Long newInterval = 777777L;

        ScheduledTaskIntervalModifyRequest request =
                new ScheduledTaskIntervalModifyRequest(FIXED_RATE_TASK_ID_FOR_MODIFY, newInterval);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/actuator/axelix-scheduled-tasks/modify/interval", defaultJsonEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.NO_CONTENT, ResponseEntity::getStatusCode);
        assertThatJson(getScheduledTasks()).node("fixedRate").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(FIXED_RATE_TASK_ID_FOR_MODIFY);
            assertThatJson(task).node("interval").isEqualTo(newInterval);
        });
    }

    @Test
    void shouldExecuteWithDisableTask_testFixedDelay() {
        forceDisableTask(FIXED_DELAY_TASK_ID_FOR_EXECUTE);
        ScheduledTaskExecuteRequest request = new ScheduledTaskExecuteRequest(FIXED_DELAY_TASK_ID_FOR_EXECUTE);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/actuator/axelix-scheduled-tasks/run-now", defaultJsonEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.NO_CONTENT, ResponseEntity::getStatusCode);
        assertThatJson(getScheduledTasks()).node("fixedRate").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(FIXED_DELAY_TASK_ID_FOR_EXECUTE);
            assertThatJson(task).node("interval").isEqualTo(2000000000);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });
        assertThat(fixedDelayFlag).isTrue();
    }

    @Test
    void shouldExecuteTask_testFixedRate() {
        ScheduledTaskExecuteRequest request = new ScheduledTaskExecuteRequest(FIXED_RATE_TASK_ID_FOR_EXECUTE);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/actuator/axelix-scheduled-tasks/run-now", defaultJsonEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.NO_CONTENT, ResponseEntity::getStatusCode);
        assertThatJson(getScheduledTasks()).node("fixedRate").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(FIXED_RATE_TASK_ID_FOR_EXECUTE);
            assertThatJson(task).node("interval").isEqualTo(2000000000);
            assertThatJson(task).node("enabled").isEqualTo(true);
        });
        assertThat(fixedRateFlag).isTrue();
    }

    private void enableScheduledTask(String target) {
        ScheduledTaskToggleRequest request = new ScheduledTaskToggleRequest(target);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/actuator/axelix-scheduled-tasks/enable", defaultJsonEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.NO_CONTENT, ResponseEntity::getStatusCode);
    }

    private void forceDisableTask(String targetScheduledTask) {
        ScheduledTaskToggleRequest request = new ScheduledTaskToggleRequest(targetScheduledTask);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/actuator/axelix-scheduled-tasks/disable?force=true", defaultJsonEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.NO_CONTENT, ResponseEntity::getStatusCode);
    }

    private String getScheduledTasks() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/axelix-scheduled-tasks", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        return response.getBody();
    }

    private <T> HttpEntity<T> defaultJsonEntity(T request) {
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

        // Cron
        @Scheduled(cron = "*/1 * * * * *")
        public void testCronTask() {
            cronFlag = true;
        }

        @Scheduled(cron = "*/2 * * * * *")
        public void testCronTaskForModify() {}

        // FixedDelay
        @Scheduled(fixedDelay = 100)
        public void testFixedDelayTask() {
            fixedDelayFlag = true;
        }

        @Scheduled(fixedDelay = 200)
        public void testFixedDelayTaskForModify() {}

        @Scheduled(fixedRate = 2000000000)
        public void testFixedDelayTaskForExecute() {
            fixedDelayFlag = true;
        }

        // FixedRate
        @Scheduled(fixedRate = 100, initialDelay = 50)
        public void testFixedRateTask() {
            fixedRateFlag = true;
        }

        @Scheduled(fixedRate = 200)
        public void testFixedRateTaskForModify() {}

        @Scheduled(fixedRate = 2000000000)
        public void testFixedRateTaskForExecute() {
            fixedRateFlag = true;
        }

        // Custom
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
