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

import org.jspecify.annotations.Nullable;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.TriggerTask;

/**
 * Provides the {@link TaskScheduler} to re-schedule the tasks on, and schedules the tasks on it.
 *
 * @since 20.10.2026
 * @author Dmitry Mazurov
 */
public interface TaskSchedulerProvider {

    /**
     * Returns the scheduler that the scheduled tasks of the application run on. It runs a task on the scheduler
     * named in its {@code @Scheduled(scheduler = ...)}, as long as the runnable of the task is passed to it as is,
     * not wrapped.
     */
    TaskScheduler getTaskScheduler();

    /**
     * Schedules the given {@link FixedDelayTask} on the scheduler of the application.
     *
     * @return the handle of the scheduled task, or {@code null} if no new handle was created for the task.
     * @throws IllegalStateException if the scheduler of the application is not available
     */
    @Nullable
    ScheduledTask scheduleFixedDelayTask(FixedDelayTask task);

    /**
     * Schedules the given {@link FixedRateTask} on the scheduler of the application.
     *
     * @return the handle of the scheduled task, or {@code null} if no new handle was created for the task.
     * @throws IllegalStateException if the scheduler of the application is not available
     */
    @Nullable
    ScheduledTask scheduleFixedRateTask(FixedRateTask task);

    /**
     * Schedules the given {@link TriggerTask} (including the cron-based one) on the scheduler of the application.
     *
     * @return the handle of the scheduled task, or {@code null} if no new handle was created for the task.
     * @throws IllegalStateException if the scheduler of the application is not available
     */
    @Nullable
    ScheduledTask scheduleTriggerTask(TriggerTask task);
}
