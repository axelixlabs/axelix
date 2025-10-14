package com.nucleonforge.axile.spring.scheduled;

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
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.test.context.TestPropertySource;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ScheduledTaskManagementEndpoint}
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ScheduledTaskManagementEndpointTest.ScheduledTaskManagementEndpointTestConfiguration.class)
@TestPropertySource(properties = {"management.endpoints.web.exposure.include=scheduledtasks, scheduled-tasks-management"})
class ScheduledTaskManagementEndpointTest {

    private static final String CRON_TASK_ID =
            "com.nucleonforge.axile.spring.scheduled.ScheduledTaskManagementEndpointTest$ScheduledTaskManagementEndpointTestConfiguration.testCronTask";
    private static final String FIXED_DELAY_TASK_ID =
            "com.nucleonforge.axile.spring.scheduled.ScheduledTaskManagementEndpointTest$ScheduledTaskManagementEndpointTestConfiguration.testFixedDelayTask";
    private static final String FIXED_RATE_TASK_ID =
            "com.nucleonforge.axile.spring.scheduled.ScheduledTaskManagementEndpointTest$ScheduledTaskManagementEndpointTestConfiguration.testFixedRateTask";
    private static final String CUSTOM_TASK_ID =
            "com.nucleonforge.axile.spring.scheduled.ScheduledTaskManagementEndpointTest$ScheduledTaskManagementEndpointTestConfiguration$CustomTestTask";
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

        disableScheduledTask(taskId, false);
        Thread.sleep(1500);
        cronFlag = false;
        Thread.sleep(1500);
        assertThat(cronFlag).isFalse();

        assertThatJson(getScheduledTasks()).node("cron").isArray().anySatisfy(task -> {
            assertThatJson(task).node("delegate.runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });
    }

    @Test
    void shouldForceRescheduleEnabledTask_testCronTask() throws InterruptedException {
        String taskId = CRON_TASK_ID;

        disableScheduledTask(taskId, true);
        Thread.sleep(1500);
        cronFlag = false;
        Thread.sleep(1500);
        assertThat(cronFlag).isFalse();

        assertThatJson(getScheduledTasks()).node("cron").isArray().anySatisfy(task -> {
            assertThatJson(task).node("delegate.runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });

        enableScheduledTask(taskId, true);
        Thread.sleep(1500);
        assertThat(cronFlag).isTrue();

        assertThatJson(getScheduledTasks()).node("cron").isArray().anySatisfy(task -> {
            assertThatJson(task).node("delegate.runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(true);
        });
    }

    @Test
    void shouldEnableDisabledTask_testFixedDelayTask() throws InterruptedException {
        String taskId = FIXED_DELAY_TASK_ID;

        disableScheduledTask(taskId, true);
        Thread.sleep(1500);
        fixedDelayFlag = false;
        Thread.sleep(1500);
        assertThat(fixedDelayFlag).isFalse();

        assertThatJson(getScheduledTasks()).node("fixedDelay").isArray().anySatisfy(task -> {
            assertThatJson(task).node("delegate.runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });
    }

    @Test
    void shouldForceRescheduleEnabledTask_testFixedDelayTask() throws InterruptedException {
        String taskId = FIXED_DELAY_TASK_ID;

        disableScheduledTask(taskId, true);
        Thread.sleep(1500);
        fixedDelayFlag = false;
        Thread.sleep(1500);
        assertThat(fixedDelayFlag).isFalse();

        assertThatJson(getScheduledTasks()).node("fixedDelay").isArray().anySatisfy(task -> {
            assertThatJson(task).node("delegate.runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });

        enableScheduledTask(taskId, false);
        Thread.sleep(1500);
        assertThat(fixedDelayFlag).isTrue();

        assertThatJson(getScheduledTasks()).node("fixedDelay").isArray().anySatisfy(task -> {
            assertThatJson(task).node("delegate.runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(true);
        });
    }

    @Test
    void shouldEnableDisabledTask_testFixedRateTask() throws InterruptedException {
        String taskId = FIXED_RATE_TASK_ID;

        disableScheduledTask(taskId, true);
        Thread.sleep(1500);
        fixedRateFlag = false;
        Thread.sleep(1500);
        assertThat(fixedRateFlag).isFalse();

        assertThatJson(getScheduledTasks()).node("fixedRate").isArray().anySatisfy(task -> {
            assertThatJson(task).node("delegate.runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });
    }

    @Test
    void shouldForceRescheduleEnabledTask_testFixedRateTask() throws InterruptedException {
        String taskId = FIXED_RATE_TASK_ID;

        disableScheduledTask(taskId, true);
        Thread.sleep(1500);
        fixedRateFlag = false;
        Thread.sleep(1500);
        assertThat(fixedRateFlag).isFalse();

        assertThatJson(getScheduledTasks()).node("fixedRate").isArray().anySatisfy(task -> {
            assertThatJson(task).node("delegate.runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });

        enableScheduledTask(taskId, false);
        Thread.sleep(1500);
        assertThat(fixedRateFlag).isTrue();

        assertThatJson(getScheduledTasks()).node("fixedRate").isArray().anySatisfy(task -> {
            assertThatJson(task).node("delegate.runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(true);
        });
    }

    @Test
    void shouldEnableDisabledTask_customTestTask() throws InterruptedException {
        String taskId = CUSTOM_TASK_ID;

        disableScheduledTask(taskId, true);
        Thread.sleep(1500);
        customTaskFlag = false;
        Thread.sleep(1500);
        assertThat(customTaskFlag).isFalse();

        assertThatJson(getScheduledTasks()).node("custom").isArray().anySatisfy(task -> {
            assertThatJson(task).node("delegate.runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("delegate.trigger").isEqualTo(CUSTOM_TRIGGER);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });
    }

    @Test
    void shouldForceRescheduleEnabledTask_customTestTask() throws InterruptedException {
        String taskId = CUSTOM_TASK_ID;

        disableScheduledTask(taskId, true);
        Thread.sleep(1500);
        customTaskFlag = false;
        Thread.sleep(1500);
        assertThat(customTaskFlag).isFalse();

        assertThatJson(getScheduledTasks()).node("custom").isArray().anySatisfy(task -> {
            assertThatJson(task).node("delegate.runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(false);
        });

        enableScheduledTask(taskId, false);
        Thread.sleep(1500);
        assertThat(customTaskFlag).isTrue();

        assertThatJson(getScheduledTasks()).node("custom").isArray().anySatisfy(task -> {
            assertThatJson(task).node("delegate.runnable.target").isEqualTo(taskId);
            assertThatJson(task).node("enabled").isEqualTo(true);
        });
    }

    private void enableScheduledTask(String target, boolean force) {
        ScheduledTaskToggleRequest request = new ScheduledTaskToggleRequest(target, force);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/actuator/scheduled-tasks-management/enable", defaultEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.NO_CONTENT, ResponseEntity::getStatusCode);
    }

    private void disableScheduledTask(String targetScheduledTask, boolean force) {
        ScheduledTaskToggleRequest request = new ScheduledTaskToggleRequest(targetScheduledTask, force);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/actuator/scheduled-tasks-management/disable", defaultEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.NO_CONTENT, ResponseEntity::getStatusCode);
    }

    private String getScheduledTasks() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/scheduledtasks", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);

        return response.getBody();
    }

    private HttpEntity<ScheduledTaskToggleRequest> defaultEntity(ScheduledTaskToggleRequest request) {
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

        @Bean
        public ScheduledTaskManagementEndpoint scheduledTaskManagementEndpoint(ScheduledTaskService service) {
            return new ScheduledTaskManagementEndpoint(service);
        }

        @Scheduled(cron = "*/1 * * * * *")
        public void testCronTask() {
            cronFlag = true;
        }

        @Scheduled(fixedDelay = 1000)
        public void testFixedDelayTask() {
            fixedDelayFlag = true;
        }

        @Scheduled(fixedRate = 1000, initialDelay = 100)
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
                return Instant.now().plusSeconds(1);
            }

            @Override
            public String toString() {
                return CUSTOM_TRIGGER;
            }
        }
    }
}
