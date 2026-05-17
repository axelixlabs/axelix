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
package com.axelixlabs.axelix.sbs.spring.core.scheduled;

import java.time.Instant;
import java.util.Date;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import com.axelixlabs.axelix.common.api.scheduledtask.ScheduledTaskCronExpressionModifyRequest;
import com.axelixlabs.axelix.common.api.scheduledtask.ScheduledTaskExecuteRequest;
import com.axelixlabs.axelix.common.api.scheduledtask.ScheduledTaskIntervalModifyRequest;
import com.axelixlabs.axelix.common.api.scheduledtask.ScheduledTaskToggleRequest;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.sbs.spring.core.Main;
import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.AxelixScheduledTasksEndpointTest.Configuration;
import com.axelixlabs.axelix.sbs.spring.core.shared.TestRestTemplateAuthInstaller;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

// TODO: Revisit this test design.
/**
 * Integration tests for {@link AxelixScheduledTasksEndpoint}.
 *
 * <p>This class owns its own scheduled-task fixtures (see {@link Configuration} below) so it runs in its own
 * Spring {@code ApplicationContext}, isolated from the rest of the endpoint suite. {@code @Scheduled} methods,
 * the custom trigger task, the firing flags and the {@link TaskScheduler} all live on the inner configuration
 * and are not visible to any other test class.
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = Main.class)
@Import({JwtAuthTestConfiguration.class, Configuration.class})
class AxelixScheduledTasksEndpointTest {

    // Cron
    private static final String CRON_TASK_ID = Configuration.class.getName() + ".testCronTask";
    private static final String CRON_TASK_ID_FOR_MODIFY = Configuration.class.getName() + ".testCronTaskForModify";

    // FixedDelay
    private static final String FIXED_DELAY_TASK_ID = Configuration.class.getName() + ".testFixedDelayTask";
    private static final String FIXED_DELAY_TASK_ID_FOR_MODIFY =
            Configuration.class.getName() + ".testFixedDelayTaskForModify";
    private static final String FIXED_DELAY_TASK_ID_FOR_EXECUTE =
            Configuration.class.getName() + ".testFixedDelayTaskForExecute";

    // FixedRate
    private static final String FIXED_RATE_TASK_ID = Configuration.class.getName() + ".testFixedRateTask";
    private static final String FIXED_RATE_TASK_ID_FOR_MODIFY =
            Configuration.class.getName() + ".testFixedRateTaskForModify";
    private static final String FIXED_RATE_TASK_ID_FOR_EXECUTE =
            Configuration.class.getName() + ".testFixedRateTaskForExecute";

    // Custom
    private static final String CUSTOM_TASK_ID = Configuration.CustomTestTask.class.getName();
    private static final String CUSTOM_TRIGGER = Configuration.CUSTOM_TRIGGER_NAME;

    // Original schedules declared on {@link Configuration}. Test methods may disable tasks or mutate their
    // schedules; {@link #restoreTaskState()} puts everything back so subsequent test methods see a clean slate.
    private static final String ORIGINAL_CRON_FOR_MODIFY = "*/2 * * * * *";
    private static final long ORIGINAL_INTERVAL_FOR_MODIFY = 20_000_000L;

    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    void restoreTaskState() {
        enableScheduledTask(CRON_TASK_ID);
        enableScheduledTask(FIXED_DELAY_TASK_ID);
        enableScheduledTask(FIXED_DELAY_TASK_ID_FOR_EXECUTE);
        enableScheduledTask(FIXED_RATE_TASK_ID);
        enableScheduledTask(CUSTOM_TASK_ID);

        modifyCronExpression(CRON_TASK_ID_FOR_MODIFY, ORIGINAL_CRON_FOR_MODIFY);
        modifyInterval(FIXED_DELAY_TASK_ID_FOR_MODIFY, ORIGINAL_INTERVAL_FOR_MODIFY);
        modifyInterval(FIXED_RATE_TASK_ID_FOR_MODIFY, ORIGINAL_INTERVAL_FOR_MODIFY);
    }

    @Test
    void shouldEnableDisabledTask_testCronTask() throws InterruptedException {
        String taskId = CRON_TASK_ID;

        forceDisableTask(taskId);
        Thread.sleep(200);
        Configuration.cronFlag = false;
        Thread.sleep(1200);
        assertThat(Configuration.cronFlag).isFalse();

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
        Configuration.cronFlag = false;
        Thread.sleep(1200);
        assertThat(Configuration.cronFlag).isFalse();

        assertThatJson(getScheduledTasks()).node("cron").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });

        enableScheduledTask(taskId);
        Thread.sleep(1200);
        assertThat(Configuration.cronFlag).isTrue();

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
        Configuration.fixedDelayFlag = false;
        Thread.sleep(200);
        assertThat(Configuration.fixedDelayFlag).isFalse();

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
        Configuration.fixedDelayFlag = false;
        Thread.sleep(200);
        assertThat(Configuration.fixedDelayFlag).isFalse();

        assertThatJson(getScheduledTasks()).node("fixedDelay").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });

        enableScheduledTask(taskId);
        Thread.sleep(200);
        assertThat(Configuration.fixedDelayFlag).isTrue();

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
        Configuration.fixedRateFlag = false;
        Thread.sleep(200);
        assertThat(Configuration.fixedRateFlag).isFalse();

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
        Configuration.fixedRateFlag = false;
        Thread.sleep(200);
        assertThat(Configuration.fixedRateFlag).isFalse();

        assertThatJson(getScheduledTasks()).node("fixedRate").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });

        enableScheduledTask(taskId);
        Thread.sleep(200);
        assertThat(Configuration.fixedRateFlag).isTrue();

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
        Configuration.customTaskFlag = false;
        Thread.sleep(200);
        assertThat(Configuration.customTaskFlag).isFalse();

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
        Configuration.customTaskFlag = false;
        Thread.sleep(200);
        assertThat(Configuration.customTaskFlag).isFalse();

        assertThatJson(getScheduledTasks()).node("custom").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });

        enableScheduledTask(taskId);
        Thread.sleep(200);
        assertThat(Configuration.customTaskFlag).isTrue();

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
    void shouldReturnBadRequest_modifyCronExpressionForCronTask() {
        // language=json
        String request = String.format(
                "{\n" + "  \"trigger\" : \"%s\",\n" + "  \"cronExpression\": \"invalid value\"\n" + "}",
                CRON_TASK_ID_FOR_MODIFY);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/actuator/axelix-scheduled-tasks/modify/cron-expression", defaultJsonEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.BAD_REQUEST, ResponseEntity::getStatusCode);
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
    void shouldReturnBadRequest_modifyIntervalForFixedDelay() {
        // language=json
        String request = String.format(
                "{\n" + "  \"trigger\" : \"%s\",\n" + "  \"cronExpression\": \"invalid value\"\n" + "}",
                FIXED_DELAY_TASK_ID_FOR_MODIFY);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/actuator/axelix-scheduled-tasks/modify/interval", defaultJsonEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.BAD_REQUEST, ResponseEntity::getStatusCode);
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
    void shouldReturnBadRequest_modifyIntervalForFixedRate() {
        // language=json
        String request = String.format(
                "{\n" + "  \"trigger\" : \"%s\",\n" + "  \"cronExpression\": \"invalid value\"\n" + "}",
                FIXED_RATE_TASK_ID_FOR_MODIFY);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/actuator/axelix-scheduled-tasks/modify/interval", defaultJsonEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.BAD_REQUEST, ResponseEntity::getStatusCode);
    }

    @Test
    void shouldExecuteWithDisableTask_testFixedDelay() {
        forceDisableTask(FIXED_DELAY_TASK_ID_FOR_EXECUTE);
        ScheduledTaskExecuteRequest request = new ScheduledTaskExecuteRequest(FIXED_DELAY_TASK_ID_FOR_EXECUTE);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/actuator/axelix-scheduled-tasks/execute", defaultJsonEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.NO_CONTENT, ResponseEntity::getStatusCode);
        assertThatJson(getScheduledTasks()).node("fixedDelay").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(FIXED_DELAY_TASK_ID_FOR_EXECUTE);
            assertThatJson(task).node("interval").isEqualTo(2000000000);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });
        assertThat(Configuration.fixedDelayFlag).isTrue();
    }

    @Test
    void shouldExecuteTask_testFixedRate() {
        ScheduledTaskExecuteRequest request = new ScheduledTaskExecuteRequest(FIXED_RATE_TASK_ID_FOR_EXECUTE);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/actuator/axelix-scheduled-tasks/execute", defaultJsonEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.NO_CONTENT, ResponseEntity::getStatusCode);
        assertThatJson(getScheduledTasks()).node("fixedRate").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(FIXED_RATE_TASK_ID_FOR_EXECUTE);
            assertThatJson(task).node("interval").isEqualTo(2000000000);
            assertThatJson(task).node("enabled").isEqualTo(true);
        });
        assertThat(Configuration.fixedRateFlag).isTrue();
    }

    private void enableScheduledTask(String target) {
        ScheduledTaskToggleRequest request = new ScheduledTaskToggleRequest(target);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/actuator/axelix-scheduled-tasks/enable", defaultJsonEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.NO_CONTENT, ResponseEntity::getStatusCode);
    }

    private void modifyCronExpression(String target, String cronExpression) {
        ScheduledTaskCronExpressionModifyRequest request =
                new ScheduledTaskCronExpressionModifyRequest(target, cronExpression);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/actuator/axelix-scheduled-tasks/modify/cron-expression", defaultJsonEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.NO_CONTENT, ResponseEntity::getStatusCode);
    }

    private void modifyInterval(String target, long interval) {
        ScheduledTaskIntervalModifyRequest request = new ScheduledTaskIntervalModifyRequest(target, interval);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/actuator/axelix-scheduled-tasks/modify/interval", defaultJsonEntity(request), Void.class);

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

    /**
     * Per-test scheduled-task fixtures. Owns the firing flags, the {@code @Scheduled} methods, the custom trigger
     * task and the {@link TaskScheduler}, so this class's Spring context contains a live scheduler that no other
     * test class shares.
     */
    @TestConfiguration
    static class Configuration implements SchedulingConfigurer {

        // Firing flags - set by the @Scheduled methods below, observed by individual test methods.
        public static volatile boolean cronFlag = false;
        public static volatile boolean fixedDelayFlag = false;
        public static volatile boolean fixedRateFlag = false;
        public static volatile boolean customTaskFlag = false;

        public static final String CUSTOM_TRIGGER_NAME = "CustomTestTrigger";

        @Bean
        public TestRestTemplateAuthInstaller testRestTemplateAuthInstaller(
                TestRestTemplate testRestTemplate, JwtEncoderService jwtEncoderService) {
            return new TestRestTemplateAuthInstaller(testRestTemplate, jwtEncoderService);
        }

        @Bean
        public TaskScheduler taskScheduler() {
            return new ConcurrentTaskScheduler();
        }

        @Scheduled(cron = "*/1 * * * * *")
        public void testCronTask() {
            cronFlag = true;
        }

        @Scheduled(cron = "*/2 * * * * *")
        public void testCronTaskForModify() {
            // intentionally empty
        }

        @Scheduled(fixedDelay = 100)
        public void testFixedDelayTask() {
            fixedDelayFlag = true;
        }

        @Scheduled(fixedDelay = 20000000)
        public void testFixedDelayTaskForModify() {
            // intentionally empty
        }

        @Scheduled(fixedDelay = 2000000000)
        public void testFixedDelayTaskForExecute() {
            fixedDelayFlag = true;
        }

        @Scheduled(fixedRate = 100, initialDelay = 50)
        public void testFixedRateTask() {
            fixedRateFlag = true;
        }

        @Scheduled(fixedRate = 20000000)
        public void testFixedRateTaskForModify() {
            // intentionally empty
        }

        @Scheduled(fixedRate = 2000000000)
        public void testFixedRateTaskForExecute() {
            fixedRateFlag = true;
        }

        @Override
        public void configureTasks(@NonNull ScheduledTaskRegistrar registrar) {
            registrar.addTriggerTask(new CustomTestTask(), new CustomTestTrigger());
        }

        public static class CustomTestTask implements Runnable {
            @Override
            public void run() {
                customTaskFlag = true;
            }

            @Override
            public String toString() {
                return CustomTestTask.class.getName();
            }
        }

        public static class CustomTestTrigger implements Trigger {
            @Override
            public Date nextExecutionTime(@NonNull TriggerContext triggerContext) {
                return Date.from(Instant.now().plusMillis(100));
            }

            @Override
            public String toString() {
                return CUSTOM_TRIGGER_NAME;
            }
        }
    }
}
