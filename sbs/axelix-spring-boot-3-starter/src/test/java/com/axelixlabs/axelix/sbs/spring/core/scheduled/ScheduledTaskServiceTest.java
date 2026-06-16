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

import java.time.Duration;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.support.CronTrigger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ScheduledTaskService}
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 * @author Artemiy Degtyarev
 */
class ScheduledTaskServiceTest extends AbstractScheduledTasksIntegrationTest {

    // Cron
    private static final String CRON_TASK_ID =
            SharedScheduledTasksTestConfiguration.class.getName() + ".testCronTask";
    private static final String CRON_TASK_ID_FOR_MODIFY =
            SharedScheduledTasksTestConfiguration.class.getName() + ".testCronTaskForModify";

    // FixedDelay
    private static final String FIXED_DELAY_TASK_ID =
            SharedScheduledTasksTestConfiguration.class.getName() + ".testFixedDelayTask";
    private static final String FIXED_DELAY_TASK_ID_FOR_MODIFY =
            SharedScheduledTasksTestConfiguration.class.getName() + ".testFixedDelayTaskForModify";

    // FixedRate
    private static final String FIXED_RATE_TASK_ID =
            SharedScheduledTasksTestConfiguration.class.getName() + ".testFixedRateTask";
    private static final String FIXED_RATE_TASK_ID_FOR_MODIFY =
            SharedScheduledTasksTestConfiguration.class.getName() + ".testFixedRateTaskForModify";
    private static final String FIXED_RATE_TASK_ID_FOR_EXECUTE =
            SharedScheduledTasksTestConfiguration.class.getName() + ".testFixedDelayTaskForExecute";

    // Custom
    private static final String CUSTOM_TASK_ID =
            SharedScheduledTasksTestConfiguration.class.getName() + ".testCustomTask";

    @Autowired
    private ScheduledTaskService taskService;

    @Autowired
    private ScheduledTasksRegistry taskRegistry;

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
        Duration newInterval = Duration.ofMillis(555555L);

        taskService.modifyInterval(FIXED_DELAY_TASK_ID_FOR_MODIFY, newInterval);

        ManagedScheduledTask task =
                taskRegistry.find(FIXED_DELAY_TASK_ID_FOR_MODIFY).orElseThrow();
        assertThat(((IntervalTask) task.getTask()).getIntervalDuration()).isEqualTo(newInterval);
    }

    @Test
    void shouldModifyInterval_testFixedRate() {
        Duration newInterval = Duration.ofMillis(777777L);

        taskService.modifyInterval(FIXED_RATE_TASK_ID_FOR_MODIFY, newInterval);

        ManagedScheduledTask task =
                taskRegistry.find(FIXED_RATE_TASK_ID_FOR_MODIFY).orElseThrow();
        assertThat(((IntervalTask) task.getTask()).getIntervalDuration()).isEqualTo(newInterval);
    }

    @Test
    void shouldExecuteScheduledTask_testFixedRate() {
        // when.
        taskService.executeScheduledTask(FIXED_RATE_TASK_ID_FOR_EXECUTE);

        // then task exists and was executed
        taskRegistry.find(FIXED_RATE_TASK_ID_FOR_EXECUTE).orElseThrow();
        assertThat(fixedRateFlagForExecute).isTrue();
    }
}
