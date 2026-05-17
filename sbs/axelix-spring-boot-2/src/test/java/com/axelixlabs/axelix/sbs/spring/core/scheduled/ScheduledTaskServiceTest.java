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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import com.axelixlabs.axelix.sbs.spring.core.Main;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.ScheduledTaskServiceTest.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ScheduledTaskService}.
 *
 * <p>This class owns its own scheduled-task fixtures (see {@link Configuration} below) so it runs in its own
 * Spring {@code ApplicationContext}, isolated from the rest of the endpoint suite. {@code @Scheduled} methods,
 * the custom trigger task, the firing flags and the {@link TaskScheduler} all live on the inner configuration
 * and are not visible to any other test class.
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@SpringBootTest(classes = Main.class)
@Import(Configuration.class)
class ScheduledTaskServiceTest {

    // Cron
    private static final String CRON_TASK_ID = Configuration.class.getName() + ".testCronTask";
    private static final String CRON_TASK_ID_FOR_MODIFY = Configuration.class.getName() + ".testCronTaskForModify";

    // FixedDelay
    private static final String FIXED_DELAY_TASK_ID = Configuration.class.getName() + ".testFixedDelayTask";
    private static final String FIXED_DELAY_TASK_ID_FOR_MODIFY =
            Configuration.class.getName() + ".testFixedDelayTaskForModify";

    // FixedRate
    private static final String FIXED_RATE_TASK_ID = Configuration.class.getName() + ".testFixedRateTask";
    private static final String FIXED_RATE_TASK_ID_FOR_MODIFY =
            Configuration.class.getName() + ".testFixedRateTaskForModify";
    private static final String FIXED_RATE_TASK_ID_FOR_EXECUTE =
            Configuration.class.getName() + ".testFixedRateTaskForExecute";

    // Custom
    private static final String CUSTOM_TASK_ID = Configuration.CustomTestTask.class.getName();

    // Original schedules declared on {@link Configuration}. Test methods may disable tasks or mutate their
    // schedules; {@link #restoreTaskState()} puts everything back so subsequent test methods see a clean slate.
    private static final String ORIGINAL_CRON_FOR_MODIFY = "*/2 * * * * *";
    private static final long ORIGINAL_INTERVAL_FOR_MODIFY = 20_000_000L;

    @Autowired
    private ScheduledTaskService taskService;

    @Autowired
    private ScheduledTasksRegistry taskRegistry;

    @AfterEach
    void restoreTaskState() {
        taskService.enableTask(CRON_TASK_ID);
        taskService.enableTask(FIXED_DELAY_TASK_ID);
        taskService.enableTask(FIXED_RATE_TASK_ID);
        taskService.enableTask(FIXED_RATE_TASK_ID_FOR_EXECUTE);
        taskService.enableTask(CUSTOM_TASK_ID);

        taskService.modifyCronExpression(CRON_TASK_ID_FOR_MODIFY, ORIGINAL_CRON_FOR_MODIFY);
        taskService.modifyInterval(FIXED_DELAY_TASK_ID_FOR_MODIFY, ORIGINAL_INTERVAL_FOR_MODIFY);
        taskService.modifyInterval(FIXED_RATE_TASK_ID_FOR_MODIFY, ORIGINAL_INTERVAL_FOR_MODIFY);
    }

    @Test
    void shouldDisabledTask_testCronTask() throws InterruptedException {
        String taskId = CRON_TASK_ID;

        taskService.disableTask(taskId, true);
        Thread.sleep(200);
        Configuration.cronFlag = false;
        Thread.sleep(1200);

        ManagedScheduledTask task = taskRegistry.find(taskId).orElseThrow();
        assertThat(task.getFuture().isCancelled()).isTrue();
    }

    @Test
    void shouldForceRescheduleEnabledTask_testCronTask() throws InterruptedException {
        String taskId = CRON_TASK_ID;

        taskService.disableTask(taskId, true);
        Thread.sleep(200);
        Configuration.cronFlag = false;
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
        Configuration.fixedDelayFlag = false;
        Thread.sleep(200);

        ManagedScheduledTask task = taskRegistry.find(taskId).orElseThrow();
        assertThat(task.getFuture().isCancelled()).isTrue();
    }

    @Test
    void shouldForceRescheduleEnabledTask_testFixedDelayTask() throws InterruptedException {
        String taskId = FIXED_DELAY_TASK_ID;

        taskService.disableTask(taskId, true);
        Thread.sleep(200);
        Configuration.fixedDelayFlag = false;

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
        Configuration.fixedRateFlag = false;
        Thread.sleep(200);

        ManagedScheduledTask task = taskRegistry.find(taskId).orElseThrow();
        assertThat(task.getFuture().isCancelled()).isTrue();
    }

    @Test
    void shouldForceRescheduleEnabledTask_testFixedRateTask() throws InterruptedException {
        String taskId = FIXED_RATE_TASK_ID;

        taskService.disableTask(taskId, true);
        Thread.sleep(200);
        Configuration.fixedRateFlag = false;

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
        Configuration.customTaskFlag = false;
        Thread.sleep(200);

        ManagedScheduledTask task = taskRegistry.find(taskId).orElseThrow();
        assertThat(task.getFuture().isCancelled()).isTrue();
    }

    @Test
    void shouldForceRescheduleEnabledTask_testCustomTask() throws InterruptedException {
        String taskId = CUSTOM_TASK_ID;

        taskService.disableTask(taskId, true);
        Thread.sleep(200);
        Configuration.customTaskFlag = false;

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
        long newInterval = 555555L;

        taskService.modifyInterval(FIXED_DELAY_TASK_ID_FOR_MODIFY, newInterval);

        ManagedScheduledTask task =
                taskRegistry.find(FIXED_DELAY_TASK_ID_FOR_MODIFY).orElseThrow();
        assertThat(((IntervalTask) task.getTask()).getInterval()).isEqualTo(newInterval);
    }

    @Test
    void shouldModifyInterval_testFixedRate() {
        long newInterval = 777777L;

        taskService.modifyInterval(FIXED_RATE_TASK_ID_FOR_MODIFY, newInterval);

        ManagedScheduledTask task =
                taskRegistry.find(FIXED_RATE_TASK_ID_FOR_MODIFY).orElseThrow();
        assertThat(((IntervalTask) task.getTask()).getInterval()).isEqualTo(newInterval);
    }

    @Test
    void shouldExecuteScheduledTask_testFixedRate() throws InterruptedException {
        Configuration.fixedRateFlag = false;

        // when.
        taskService.executeScheduledTask(FIXED_RATE_TASK_ID_FOR_EXECUTE);

        // {@code executeScheduledTask} submits the run to the task executor and returns immediately, so wait for
        // the runnable to set the flag before asserting.
        Thread.sleep(200);

        // then task exists and was executed
        taskRegistry.find(FIXED_RATE_TASK_ID_FOR_EXECUTE).orElseThrow();
        assertThat(Configuration.fixedRateFlag).isTrue();
    }

    /**
     * Per-test scheduled-task fixtures. Owns the firing flags, the {@code @Scheduled} methods, the custom trigger
     * task and the {@link TaskScheduler}, so this class's Spring context contains a live scheduler that no other
     * test class shares.
     */
    @TestConfiguration
    static class Configuration implements SchedulingConfigurer {

        public static volatile boolean cronFlag = false;
        public static volatile boolean fixedDelayFlag = false;
        public static volatile boolean fixedRateFlag = false;
        public static volatile boolean customTaskFlag = false;

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
        }
    }
}
