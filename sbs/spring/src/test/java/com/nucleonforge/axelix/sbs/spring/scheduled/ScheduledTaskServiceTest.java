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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ScheduledTaskService}
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@SpringBootTest
@Import(ScheduledTaskServiceTest.ScheduledTaskServiceTestConfiguration.class)
class ScheduledTaskServiceTest {

    // Cron
    private static final String CRON_TASK_ID =
            ScheduledTaskServiceTest.ScheduledTaskServiceTestConfiguration.class.getName() + ".testCronTask";
    private static final String CRON_TASK_ID_FOR_MODIFY =
            ScheduledTaskServiceTest.ScheduledTaskServiceTestConfiguration.class.getName() + ".testCronTaskForModify";

    // FixedDelay
    private static final String FIXED_DELAY_TASK_ID =
            ScheduledTaskServiceTest.ScheduledTaskServiceTestConfiguration.class.getName() + ".testFixedDelayTask";
    private static final String FIXED_DELAY_TASK_ID_FOR_MODIFY =
            ScheduledTaskServiceTest.ScheduledTaskServiceTestConfiguration.class.getName()
                    + ".testFixedDelayTaskForModify";

    // FixedRate
    private static final String FIXED_RATE_TASK_ID =
            ScheduledTaskServiceTest.ScheduledTaskServiceTestConfiguration.class.getName() + ".testFixedRateTask";
    private static final String FIXED_RATE_TASK_ID_FOR_MODIFY =
            ScheduledTaskServiceTest.ScheduledTaskServiceTestConfiguration.class.getName()
                    + ".testFixedRateTaskForModify";
    private static final String FIXED_RATE_TASK_ID_FOR_RUN_NOW =
            ScheduledTaskServiceTest.ScheduledTaskServiceTestConfiguration.class.getName()
                    + ".testFixedRateTaskForRunNow";

    // Custom
    private static final String CUSTOM_TASK_ID =
            ScheduledTaskServiceTest.ScheduledTaskServiceTestConfiguration.class.getName() + ".testCustomTask";

    @Autowired
    private ScheduledTaskService taskService;

    @Autowired
    private ScheduledTasksRegistry taskRegistry;

    private static volatile boolean cronFlag = false;

    private static volatile boolean fixedDelayFlag = false;

    private static volatile boolean fixedRateFlag = false;

    private static volatile boolean fixedRateFlagForRunNow = false;

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

        taskService.enableTask(taskId);
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

        taskService.enableTask(taskId);
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

        taskService.enableTask(taskId);
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

        taskService.enableTask(taskId);
        Thread.sleep(200);

        task = taskRegistry.find(taskId).orElseThrow();
        assertThat(task.getFuture().isCancelled()).isFalse();
    }

    @Test
    void shouldModifyCronExpression_testCronTask() {
        String newCronExpression = "*/5 * * * * *";

        taskService.modifyCronExpression(CRON_TASK_ID_FOR_MODIFY, newCronExpression);

        ManagedScheduledTask task = taskRegistry.find(CRON_TASK_ID_FOR_MODIFY).orElseThrow();
        assertThat(((CronTrigger) task.getTrigger()).getExpression()).isEqualTo(newCronExpression);
    }

    @Test
    void shouldModifyInterval_testFixedDelay() {
        String newInterval = "555555";

        taskService.modifyInterval(FIXED_DELAY_TASK_ID_FOR_MODIFY, newInterval);

        ManagedScheduledTask task =
                taskRegistry.find(FIXED_DELAY_TASK_ID_FOR_MODIFY).orElseThrow();
        assertThat(((IntervalTask) task.getTask()).getInterval()).isEqualTo(Long.parseLong(newInterval));
    }

    @Test
    void shouldModifyInterval_testFixedRate() {
        String newInterval = "777777";

        taskService.modifyInterval(FIXED_RATE_TASK_ID_FOR_MODIFY, newInterval);

        ManagedScheduledTask task =
                taskRegistry.find(FIXED_RATE_TASK_ID_FOR_MODIFY).orElseThrow();
        assertThat(((IntervalTask) task.getTask()).getInterval()).isEqualTo(Long.parseLong(newInterval));
    }

    @Test
    void shouldRunNowTask_testFixedRate() {
        taskService.runNowTask(FIXED_RATE_TASK_ID_FOR_RUN_NOW);

        ManagedScheduledTask task =
                taskRegistry.find(FIXED_RATE_TASK_ID_FOR_RUN_NOW).orElseThrow();
        assertThat(fixedRateFlagForRunNow).isTrue();
    }

    @TestConfiguration
    @EnableScheduling
    static class ScheduledTaskServiceTestConfiguration implements SchedulingConfigurer {

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

        // FixedRate
        @Scheduled(fixedRate = 100, initialDelay = 50)
        public void testFixedRateTask() {
            fixedRateFlag = true;
        }

        @Scheduled(fixedRate = 200)
        public void testFixedRateTaskForModify() {}

        @Scheduled(fixedRate = 2000000000)
        public void testFixedRateTaskForRunNow() {
            fixedRateFlagForRunNow = true;
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
        }
    }
}
