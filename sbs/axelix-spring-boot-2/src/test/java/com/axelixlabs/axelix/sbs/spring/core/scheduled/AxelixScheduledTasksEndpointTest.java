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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.api.scheduledtask.ScheduledTaskCronExpressionModifyRequest;
import com.axelixlabs.axelix.common.api.scheduledtask.ScheduledTaskExecuteRequest;
import com.axelixlabs.axelix.common.api.scheduledtask.ScheduledTaskIntervalModifyRequest;
import com.axelixlabs.axelix.common.api.scheduledtask.ScheduledTaskToggleRequest;
import com.axelixlabs.axelix.sbs.spring.core.shared.AbstractEndpointTest;
import com.axelixlabs.axelix.sbs.spring.core.shared.SharedEndpointTestConfiguration;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

// TODO: Revisit this test design.
/**
 * Integration tests for {@link AxelixScheduledTasksEndpoint}
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
class AxelixScheduledTasksEndpointTest extends AbstractEndpointTest {

    // Cron
    private static final String CRON_TASK_ID = SharedEndpointTestConfiguration.class.getName() + ".testCronTask";
    private static final String CRON_TASK_ID_FOR_MODIFY =
            SharedEndpointTestConfiguration.class.getName() + ".testCronTaskForModify";

    // FixedDelay
    private static final String FIXED_DELAY_TASK_ID =
            SharedEndpointTestConfiguration.class.getName() + ".testFixedDelayTask";
    private static final String FIXED_DELAY_TASK_ID_FOR_MODIFY =
            SharedEndpointTestConfiguration.class.getName() + ".testFixedDelayTaskForModify";
    private static final String FIXED_DELAY_TASK_ID_FOR_EXECUTE =
            SharedEndpointTestConfiguration.class.getName() + ".testFixedDelayTaskForExecute";

    // FixedRate
    private static final String FIXED_RATE_TASK_ID =
            SharedEndpointTestConfiguration.class.getName() + ".testFixedRateTask";
    private static final String FIXED_RATE_TASK_ID_FOR_MODIFY =
            SharedEndpointTestConfiguration.class.getName() + ".testFixedRateTaskForModify";
    private static final String FIXED_RATE_TASK_ID_FOR_EXECUTE =
            SharedEndpointTestConfiguration.class.getName() + ".testFixedRateTaskForExecute";

    // Custom
    private static final String CUSTOM_TASK_ID = SharedEndpointTestConfiguration.CustomTestTask.class.getName();

    private static final String CUSTOM_TRIGGER = SharedEndpointTestConfiguration.CUSTOM_TRIGGER_NAME;

    // Original schedules declared on SharedEndpointTestConfiguration. The shared Spring context is reused across the
    // whole endpoint-test suite, so tests in this class must restore any task they disable or whose schedule they
    // mutate, otherwise subsequent test methods (and other endpoint tests) see leaked state.
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
        SharedEndpointTestConfiguration.cronFlag = false;
        Thread.sleep(1200);
        assertThat(SharedEndpointTestConfiguration.cronFlag).isFalse();

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
        SharedEndpointTestConfiguration.cronFlag = false;
        Thread.sleep(1200);
        assertThat(SharedEndpointTestConfiguration.cronFlag).isFalse();

        assertThatJson(getScheduledTasks()).node("cron").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });

        enableScheduledTask(taskId);
        Thread.sleep(1200);
        assertThat(SharedEndpointTestConfiguration.cronFlag).isTrue();

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
        SharedEndpointTestConfiguration.fixedDelayFlag = false;
        Thread.sleep(200);
        assertThat(SharedEndpointTestConfiguration.fixedDelayFlag).isFalse();

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
        SharedEndpointTestConfiguration.fixedDelayFlag = false;
        Thread.sleep(200);
        assertThat(SharedEndpointTestConfiguration.fixedDelayFlag).isFalse();

        assertThatJson(getScheduledTasks()).node("fixedDelay").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });

        enableScheduledTask(taskId);
        Thread.sleep(200);
        assertThat(SharedEndpointTestConfiguration.fixedDelayFlag).isTrue();

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
        SharedEndpointTestConfiguration.fixedRateFlag = false;
        Thread.sleep(200);
        assertThat(SharedEndpointTestConfiguration.fixedRateFlag).isFalse();

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
        SharedEndpointTestConfiguration.fixedRateFlag = false;
        Thread.sleep(200);
        assertThat(SharedEndpointTestConfiguration.fixedRateFlag).isFalse();

        assertThatJson(getScheduledTasks()).node("fixedRate").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });

        enableScheduledTask(taskId);
        Thread.sleep(200);
        assertThat(SharedEndpointTestConfiguration.fixedRateFlag).isTrue();

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
        SharedEndpointTestConfiguration.customTaskFlag = false;
        Thread.sleep(200);
        assertThat(SharedEndpointTestConfiguration.customTaskFlag).isFalse();

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
        SharedEndpointTestConfiguration.customTaskFlag = false;
        Thread.sleep(200);
        assertThat(SharedEndpointTestConfiguration.customTaskFlag).isFalse();

        assertThatJson(getScheduledTasks()).node("custom").isArray().anySatisfy(task -> {
            assertThatJson(task).node("runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });

        enableScheduledTask(taskId);
        Thread.sleep(200);
        assertThat(SharedEndpointTestConfiguration.customTaskFlag).isTrue();

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
        assertThat(SharedEndpointTestConfiguration.fixedDelayFlag).isTrue();
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
        assertThat(SharedEndpointTestConfiguration.fixedRateFlag).isTrue();
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
}
