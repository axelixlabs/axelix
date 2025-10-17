package com.nucleonforge.axile.sbs.spring.scheduled;

import java.time.Instant;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ScheduledTaskService}
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 */
@SpringBootTest
@Import(ScheduledTaskServiceTest.ScheduledTaskServiceTestConfiguration.class)
class ScheduledTaskServiceTest {

    private static final String CRON_TASK_ID =
            "com.nucleonforge.axile.sbs.spring.scheduled.ScheduledTaskServiceTest$ScheduledTaskServiceTestConfiguration.testCronTask";
    private static final String FIXED_DELAY_TASK_ID =
            "com.nucleonforge.axile.sbs.spring.scheduled.ScheduledTaskServiceTest$ScheduledTaskServiceTestConfiguration.testFixedDelayTask";
    private static final String FIXED_RATE_TASK_ID =
            "com.nucleonforge.axile.sbs.spring.scheduled.ScheduledTaskServiceTest$ScheduledTaskServiceTestConfiguration.testFixedRateTask";
    private static final String CUSTOM_TASK_ID =
            "com.nucleonforge.axile.sbs.spring.scheduled.ScheduledTaskServiceTest$ScheduledTaskServiceTestConfiguration.testCustomTask";

    @Autowired
    private ScheduledTaskService taskService;

    @Autowired
    private ScheduledTasksRegistry taskRegistry;

    private static volatile boolean cronFlag = false;

    private static volatile boolean fixedDelayFlag = false;

    private static volatile boolean fixedRateFlag = false;

    private static volatile boolean customTaskFlag = false;

    @Test
    void shouldDisabledTask_testCronTask() throws InterruptedException {
        String taskId = CRON_TASK_ID;

        taskService.disableTask(taskId, true);
        Thread.sleep(200);
        cronFlag = false;
        Thread.sleep(1200);

        ManagedScheduledTask task = taskRegistry.find(taskId).orElseThrow();
        assertThat(task.getFuture().isCancelled()).isTrue();
    }

    @Test
    void shouldForceRescheduleEnabledTask_testCronTask() throws InterruptedException {
        String taskId = CRON_TASK_ID;

        taskService.disableTask(taskId, true);
        Thread.sleep(200);
        cronFlag = false;
        Thread.sleep(1200);

        ManagedScheduledTask task = taskRegistry.find(taskId).orElseThrow();
        assertThat(task.getFuture().isCancelled()).isTrue();

        taskService.enableTask(taskId, true);
        Thread.sleep(200);

        task = taskRegistry.find(taskId).orElseThrow();
        assertThat(task.getFuture().isCancelled()).isFalse();
    }

    @Test
    void shouldDisabledTask_testFixedDelayTask() throws InterruptedException {
        String taskId = FIXED_DELAY_TASK_ID;

        taskService.disableTask(taskId, false);
        Thread.sleep(200);
        fixedDelayFlag = false;
        Thread.sleep(200);

        ManagedScheduledTask task = taskRegistry.find(taskId).orElseThrow();
        assertThat(task.getFuture().isCancelled()).isTrue();
    }

    @Test
    void shouldForceRescheduleEnabledTask_testFixedDelayTask() throws InterruptedException {
        String taskId = FIXED_DELAY_TASK_ID;

        taskService.disableTask(taskId, true);
        Thread.sleep(200);
        fixedDelayFlag = false;

        ManagedScheduledTask task = taskRegistry.find(taskId).orElseThrow();
        assertThat(task.getFuture().isCancelled()).isTrue();

        taskService.enableTask(taskId, true);
        Thread.sleep(100);

        task = taskRegistry.find(taskId).orElseThrow();
        assertThat(task.getFuture().isCancelled()).isFalse();
    }

    @Test
    void shouldDisabledTask_testFixedRateTask() throws InterruptedException {
        String taskId = FIXED_RATE_TASK_ID;

        taskService.disableTask(taskId, false);
        Thread.sleep(200);
        fixedRateFlag = false;
        Thread.sleep(200);

        ManagedScheduledTask task = taskRegistry.find(taskId).orElseThrow();
        assertThat(task.getFuture().isCancelled()).isTrue();
    }

    @Test
    void shouldForceRescheduleEnabledTask_testFixedRateTask() throws InterruptedException {
        String taskId = FIXED_RATE_TASK_ID;

        taskService.disableTask(taskId, true);
        Thread.sleep(200);
        fixedRateFlag = false;

        ManagedScheduledTask task = taskRegistry.find(taskId).orElseThrow();
        assertThat(task.getFuture().isCancelled()).isTrue();

        taskService.enableTask(taskId, true);
        Thread.sleep(200);

        task = taskRegistry.find(taskId).orElseThrow();
        assertThat(task.getFuture().isCancelled()).isFalse();
    }

    @Test
    void shouldDisabledTask_testCustomTask() throws InterruptedException {
        String taskId = CUSTOM_TASK_ID;

        taskService.disableTask(taskId, false);
        Thread.sleep(200);
        customTaskFlag = false;
        Thread.sleep(200);

        ManagedScheduledTask task = taskRegistry.find(taskId).orElseThrow();
        assertThat(task.getFuture().isCancelled()).isTrue();
    }

    @Test
    void shouldForceRescheduleEnabledTask_testCustomTask() throws InterruptedException {
        String taskId = CUSTOM_TASK_ID;

        taskService.disableTask(taskId, true);
        Thread.sleep(200);
        customTaskFlag = false;

        ManagedScheduledTask task = taskRegistry.find(taskId).orElseThrow();
        assertThat(task.getFuture().isCancelled()).isTrue();

        taskService.enableTask(taskId, true);
        Thread.sleep(200);

        task = taskRegistry.find(taskId).orElseThrow();
        assertThat(task.getFuture().isCancelled()).isFalse();
    }

    @TestConfiguration
    @EnableScheduling
    static class ScheduledTaskServiceTestConfiguration implements SchedulingConfigurer {

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

        @Scheduled(cron = "*/1 * * * * *")
        public void testCronTask() {
            cronFlag = true;
        }

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
        }
    }
}
