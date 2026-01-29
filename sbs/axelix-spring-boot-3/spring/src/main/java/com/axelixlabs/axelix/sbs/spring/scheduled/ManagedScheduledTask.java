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
package com.axelixlabs.axelix.sbs.spring.scheduled;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ScheduledFuture;

import org.jspecify.annotations.Nullable;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.Task;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.util.ReflectionUtils;

/**
 * Decorates the standard {@link ScheduledTask}, and provides additional information
 * about the decorated task, such as the {@link #id} of the task.
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Chaerkasov
 */
public class ManagedScheduledTask {

    /**
     * Reflection field access to the package-private 'future' field in {@link ScheduledTask}.
     */
    private static final Field SCHEDULED_TASK_FUTURE_FIELD;

    /**
     * Unique identifier for the scheduled task, typically derived from the runnable's toString().
     */
    private final String id;

    /**
     * The original Spring scheduled task being managed.
     */
    private ScheduledTask scheduledTask;

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
     * Replace the internal {@link ManagedScheduledTask} with the new one, created from passed parameters.
     *
     * @param newExecutionSchedule execution schedule handle. May be null, which typically (in Spring Framework) means
     *                             that the trigger won't fire anymore.
     * @param newTask the new {@link Task}.
     */
    public void replaceScheduledState(@Nullable ScheduledFuture<?> newExecutionSchedule, Task newTask) {
        try {
            // Cancel the old Spring ScheduledTask
            this.scheduledTask.cancel(false);

            // Construct a new Spring ScheduledTask
            Constructor<ScheduledTask> constructor = ScheduledTask.class.getDeclaredConstructor(Task.class);
            ReflectionUtils.makeAccessible(constructor);
            this.scheduledTask = constructor.newInstance(newTask);
            SCHEDULED_TASK_FUTURE_FIELD.set(this.scheduledTask, newExecutionSchedule);

        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new IllegalStateException("Failed to set 'future' in ScheduledTask", e);
        } catch (InvocationTargetException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}
