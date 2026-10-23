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

import java.lang.reflect.Field;
import java.util.concurrent.ScheduledFuture;

import org.jspecify.annotations.Nullable;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.Task;
import org.springframework.scheduling.config.TriggerTask;

/**
 * Decorates the standard {@link ScheduledTask}, and provides additional information
 * about the decorated task, such as the {@link #id} of the task, and the {@link Task}
 * it was registered with, if its schedule was modified.
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Chaerkasov
 * @author Vyacheslav Yanin
 * @author Dmitry Mazurov
 */
public class ManagedScheduledTask implements AutoCloseable {

    /**
     * Reflection field access to the package-private 'future' field in {@link ScheduledTask}.
     */
    private static final Field SCHEDULED_TASK_FUTURE_FIELD;

    /**
     * Unique identifier for the scheduled task, typically derived from the runnable's toString().
     */
    private final String id;

    /**
     * The current Spring scheduled task being managed. It is replaced on every re-scheduling.
     */
    private ScheduledTask scheduledTask;

    /**
     * The {@link Task} the scheduled task was registered with, kept while its schedule is modified.
     * {@code null} if the schedule was not modified, or was brought back to the original one.
     */
    private @Nullable Task originalTask;

    static {
        try {
            SCHEDULED_TASK_FUTURE_FIELD = ScheduledTask.class.getDeclaredField("future");
            SCHEDULED_TASK_FUTURE_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e.getMessage());
        }
    }

    public ManagedScheduledTask(String id, ScheduledTask scheduledTask) {
        this.id = id;
        this.scheduledTask = scheduledTask;
    }

    public String getId() {
        return id;
    }

    public ScheduledTask getScheduledTask() {
        return scheduledTask;
    }

    public Runnable getRunnable() {
        return scheduledTask.getTask().getRunnable();
    }

    public Task getTask() {
        return scheduledTask.getTask();
    }

    /**
     * Optional trigger for custom scheduled tasks, {@code null} for fixed-rate and fixed-delay tasks.
     */
    public @Nullable Trigger getTrigger() {
        if (scheduledTask.getTask() instanceof TriggerTask triggerTask) {
            return triggerTask.getTrigger();
        } else {
            return null;
        }
    }

    public ScheduledFuture<?> getFuture() {
        try {
            return (ScheduledFuture<?>) SCHEDULED_TASK_FUTURE_FIELD.get(scheduledTask);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to get 'future' from ScheduledTask", e);
        }
    }

    public boolean isEnabled() {
        return !getFuture().isCancelled();
    }

    /**
     * @return the {@link Task} the scheduled task was registered with, or {@code null}
     *         if the schedule was not modified.
     */
    public @Nullable Task getOriginalTask() {
        return originalTask;
    }

    /**
     * Replace the internal {@link ScheduledTask} with the new one, cancelling the old one. Keeps track
     * of the original schedule, so that it can be restored later.
     *
     * @param newScheduledTask the new {@link ScheduledTask}, already submitted for execution. If {@code null},
     *                         nothing is replaced and the current {@link ScheduledTask} stays in effect.
     */
    public void replaceScheduledState(@Nullable ScheduledTask newScheduledTask) {
        if (newScheduledTask == null) {
            return;
        }

        Task newTask = newScheduledTask.getTask();
        Task currentTask = this.scheduledTask.getTask();

        if (newTask != currentTask) {
            Task original = this.originalTask != null ? this.originalTask : currentTask;
            boolean backToOriginal = newTask == original || sameSchedule(original, newTask);
            this.originalTask = backToOriginal ? null : original;
        }

        // Cancel the old Spring ScheduledTask
        this.scheduledTask.cancel(false);
        this.scheduledTask = newScheduledTask;
    }

    @Override
    public void close() {
        if (this.scheduledTask != null) {
            this.scheduledTask.cancel();
        }
    }

    private static boolean sameSchedule(Task original, Task current) {
        if (original instanceof CronTask originalCron && current instanceof CronTask currentCron) {
            return originalCron.getExpression().equals(currentCron.getExpression());
        }

        if (original instanceof IntervalTask originalInterval && current instanceof IntervalTask currentInterval) {
            return originalInterval.getIntervalDuration().equals(currentInterval.getIntervalDuration());
        }

        // tasks of different kinds have no comparable schedule
        return false;
    }
}
