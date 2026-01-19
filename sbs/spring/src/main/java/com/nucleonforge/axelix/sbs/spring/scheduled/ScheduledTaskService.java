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

import java.time.Duration;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.Task;
import org.springframework.scheduling.support.CronTrigger;

/**
 * Service for managing scheduled tasks with enable/disable functionality.
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Chaerkasov
 */
public final class ScheduledTaskService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskService.class);

    private final ScheduledTasksRegistry registry;
    private final List<TaskRescheduler> taskReschedulers;
    private final ThreadPoolTaskExecutor taskExecutor;

    public ScheduledTaskService(
            ScheduledTasksRegistry registry,
            List<TaskRescheduler> taskReschedulers,
            ThreadPoolTaskExecutor taskExecutor) {
        this.registry = registry;
        this.taskReschedulers = taskReschedulers;
        this.taskExecutor = taskExecutor;
    }

    /**
     * Enable the task with the given id. The task will resume its
     * execution according to it's configured schedule.
     *
     * @param taskId the id of the task to enable
     */
    public void enableTask(String taskId) {
        try {
            ManagedScheduledTask task = registry.findRequired(taskId);

            for (TaskRescheduler taskRescheduler : taskReschedulers) {
                if (taskRescheduler.supports(task)) {
                    taskRescheduler.reschedule(task);
                }
            }
        } catch (ScheduledTaskNotFoundException e) {
            log.info("Failed to enable task: {}", taskId, e);
            throw e;
        }
    }

    /**
     * Find the task by the provided id.
     *
     * @param taskId the id of the task to find.
     */
    public Optional<ManagedScheduledTask> find(String taskId) {
        return registry.find(taskId);
    }

    /**
     * Disable the task with the given id.
     *
     * @param taskId the id of the task to disable.
     * @param force if {@code true}, then thread that is running the task may be
     *              interrupted (if the thread is currently executing). If {@code false},
     *              then executing thread will be waited for until the end of execution.
     */
    public void disableTask(String taskId, boolean force) {
        try {
            ManagedScheduledTask task = registry.findRequired(taskId);
            cancelTask(task, force);
            log.info("Disabled scheduled task: {}(force: {})", taskId, force);
        } catch (ScheduledTaskNotFoundException e) {
            log.info("Failed to disable task: {}", taskId, e);
            throw e;
        }
    }

    /**
     * Modify the cron expression for the task with the given id.
     *
     * @param taskId            the id of the task to re-schedule.
     * @param newCronExpression the new cron expression to apply.
     */
    public void modifyCronExpression(String taskId, String newCronExpression) {
        try {
            ManagedScheduledTask task = registry.findRequired(taskId);

            CronTask cronTask =
                    new CronTask(task.getRunnable(), new CronTrigger(newCronExpression, ZoneId.systemDefault()));

            for (TaskRescheduler taskRescheduler : taskReschedulers) {
                if (taskRescheduler.supports(task)) {
                    taskRescheduler.reschedule(task, cronTask);
                }
            }
        } catch (ScheduledTaskNotFoundException e) {
            log.info("Failed to modify CronExpression: {}", taskId, e);
            throw e;
        }
    }

    /**
     * Modify the interval for the task with the given id.
     *
     * @param taskId            the id of the task to re-schedule.
     * @param newInterval       the new interval to apply.
     */
    public void modifyInterval(String taskId, Duration newInterval) {
        try {
            ManagedScheduledTask task = registry.findRequired(taskId);

            IntervalTask updatedTask = recreateIntervalTask(task.getTask(), newInterval);

            for (TaskRescheduler taskRescheduler : taskReschedulers) {
                if (taskRescheduler.supports(task)) {
                    taskRescheduler.reschedule(task, updatedTask);
                }
            }
        } catch (ScheduledTaskNotFoundException e) {
            log.info("Failed to Modify Interval: {}", taskId, e);
            throw e;
        }
    }

    /**
     * Allows forcing a task to run without affecting its schedule.
     *
     * @param taskId  the id of the task to run forcibly.
     */
    public void executeScheduledTask(String taskId) {
        try {
            ManagedScheduledTask task = registry.findRequired(taskId);

            taskExecutor.execute(() -> {
                try {
                    task.getRunnable().run();
                } catch (Exception ex) {
                    log.info("Failed to Run: {}", taskId, ex);
                }
            });

        } catch (ScheduledTaskNotFoundException e) {
            log.warn("Task '{}' not found, cannot run now", taskId, e);
        }
    }

    private IntervalTask recreateIntervalTask(Task task, Duration interval) {
        if (task instanceof FixedDelayTask t) {
            return new FixedDelayTask(t.getRunnable(), interval, t.getInitialDelayDuration());
        }
        if (task instanceof FixedRateTask t) {
            return new FixedRateTask(t.getRunnable(), interval, t.getInitialDelayDuration());
        }
        throw new IllegalArgumentException("Unsupported IntervalTask type: " + task.getClass());
    }

    private void cancelTask(ManagedScheduledTask managedTask, boolean force) {
        ScheduledFuture<?> future = managedTask.getFuture();
        if (future != null) {
            boolean cancelled = future.cancel(force);
            log.debug(
                    "Cancelled task future: {} (mayInterrupt: {}, success: {})", managedTask.getId(), force, cancelled);
        } else {
            log.debug("No future to cancel for task: {}", managedTask.getId());
        }
    }
}
