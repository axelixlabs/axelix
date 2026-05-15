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
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.support.CronTrigger;

import com.axelixlabs.axelix.sbs.spring.core.shared.AbstractEndpointTest;
import com.axelixlabs.axelix.sbs.spring.core.shared.SharedEndpointTestConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ScheduledTaskService}
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
class ScheduledTaskServiceTest extends AbstractEndpointTest {

    // Cron
    private static final String CRON_TASK_ID = SharedEndpointTestConfiguration.class.getName() + ".testCronTask";
    private static final String CRON_TASK_ID_FOR_MODIFY =
            SharedEndpointTestConfiguration.class.getName() + ".testCronTaskForModify";

    // FixedDelay
    private static final String FIXED_DELAY_TASK_ID =
            SharedEndpointTestConfiguration.class.getName() + ".testFixedDelayTask";
    private static final String FIXED_DELAY_TASK_ID_FOR_MODIFY =
            SharedEndpointTestConfiguration.class.getName() + ".testFixedDelayTaskForModify";

    // FixedRate
    private static final String FIXED_RATE_TASK_ID =
            SharedEndpointTestConfiguration.class.getName() + ".testFixedRateTask";
    private static final String FIXED_RATE_TASK_ID_FOR_MODIFY =
            SharedEndpointTestConfiguration.class.getName() + ".testFixedRateTaskForModify";
    private static final String FIXED_RATE_TASK_ID_FOR_EXECUTE =
            SharedEndpointTestConfiguration.class.getName() + ".testFixedRateTaskForExecute";

    // Custom
    private static final String CUSTOM_TASK_ID = SharedEndpointTestConfiguration.CustomTestTask.class.getName();

    // Original schedules declared on SharedEndpointTestConfiguration. The shared Spring context is reused across the
    // whole endpoint-test suite, so tests in this class must restore any task they disable or whose schedule they
    // mutate, otherwise subsequent test methods (and other endpoint tests) see leaked state.
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
        SharedEndpointTestConfiguration.cronFlag = false;
        Thread.sleep(1200);

        ManagedScheduledTask task = taskRegistry.find(taskId).orElseThrow();
        assertThat(task.getFuture().isCancelled()).isTrue();
    }

    @Test
    void shouldForceRescheduleEnabledTask_testCronTask() throws InterruptedException {
        String taskId = CRON_TASK_ID;

        taskService.disableTask(taskId, true);
        Thread.sleep(200);
        SharedEndpointTestConfiguration.cronFlag = false;
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
        SharedEndpointTestConfiguration.fixedDelayFlag = false;
        Thread.sleep(200);

        ManagedScheduledTask task = taskRegistry.find(taskId).orElseThrow();
        assertThat(task.getFuture().isCancelled()).isTrue();
    }

    @Test
    void shouldForceRescheduleEnabledTask_testFixedDelayTask() throws InterruptedException {
        String taskId = FIXED_DELAY_TASK_ID;

        taskService.disableTask(taskId, true);
        Thread.sleep(200);
        SharedEndpointTestConfiguration.fixedDelayFlag = false;

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
        SharedEndpointTestConfiguration.fixedRateFlag = false;
        Thread.sleep(200);

        ManagedScheduledTask task = taskRegistry.find(taskId).orElseThrow();
        assertThat(task.getFuture().isCancelled()).isTrue();
    }

    @Test
    void shouldForceRescheduleEnabledTask_testFixedRateTask() throws InterruptedException {
        String taskId = FIXED_RATE_TASK_ID;

        taskService.disableTask(taskId, true);
        Thread.sleep(200);
        SharedEndpointTestConfiguration.fixedRateFlag = false;

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
        SharedEndpointTestConfiguration.customTaskFlag = false;
        Thread.sleep(200);

        ManagedScheduledTask task = taskRegistry.find(taskId).orElseThrow();
        assertThat(task.getFuture().isCancelled()).isTrue();
    }

    @Test
    void shouldForceRescheduleEnabledTask_testCustomTask() throws InterruptedException {
        String taskId = CUSTOM_TASK_ID;

        taskService.disableTask(taskId, true);
        Thread.sleep(200);
        SharedEndpointTestConfiguration.customTaskFlag = false;

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
        SharedEndpointTestConfiguration.fixedRateFlag = false;

        // when.
        taskService.executeScheduledTask(FIXED_RATE_TASK_ID_FOR_EXECUTE);

        // {@code executeScheduledTask} submits the run to the task executor and returns immediately, so wait for
        // the runnable to set the flag before asserting.
        Thread.sleep(200);

        // then task exists and was executed
        taskRegistry.find(FIXED_RATE_TASK_ID_FOR_EXECUTE).orElseThrow();
        assertThat(SharedEndpointTestConfiguration.fixedRateFlag).isTrue();
    }
}
