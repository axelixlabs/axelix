/*
 * Copyright 2025-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.sbs.spring.scheduled;

import java.lang.reflect.Field;
import java.util.concurrent.ScheduledFuture;

import org.jspecify.annotations.Nullable;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.Task;
import org.springframework.scheduling.config.TriggerTask;

/**
 * Represents a managed scheduled task with additional control capabilities.
 *
 * <p>Wraps a standard Spring {@link ScheduledTask} and provides functionality
 * to enable/disable task execution at runtime, as well as track the task's
 * current state.
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
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
    private final ScheduledTask scheduledTask;

    /**
     * The task scheduler used for rescheduling operations.
     */
    private final TaskScheduler taskScheduler;

    /**
     * The runnable task to be executed.
     */
    private final Runnable runnable;

    /**
     * Optional trigger for custom scheduled tasks, {@code null} for fixed-rate and fixed-delay tasks.
     */
    @Nullable
    private final Trigger trigger;

    static {
        try {
            SCHEDULED_TASK_FUTURE_FIELD = ScheduledTask.class.getDeclaredField("future");
            SCHEDULED_TASK_FUTURE_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e.getMessage());
        }
    }

    public ManagedScheduledTask(String id, ScheduledTask scheduledTask, TaskScheduler taskScheduler) {
        this.id = id;
        this.scheduledTask = scheduledTask;
        this.taskScheduler = taskScheduler;

        Task t = scheduledTask.getTask();
        this.runnable = t.getRunnable();

        if (t instanceof TriggerTask triggerTask) {
            this.trigger = triggerTask.getTrigger();
        } else {
            this.trigger = null;
        }
    }

    public String getId() {
        return id;
    }

    public ScheduledTask getScheduledTask() {
        return scheduledTask;
    }

    public TaskScheduler getTaskScheduler() {
        return taskScheduler;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public @Nullable Trigger getTrigger() {
        return trigger;
    }

    public ScheduledFuture<?> getFuture() {
        try {
            return (ScheduledFuture<?>) SCHEDULED_TASK_FUTURE_FIELD.get(scheduledTask);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to get 'future' from ScheduledTask", e);
        }
    }

    public void setFuture(ScheduledFuture<?> future) {
        try {
            SCHEDULED_TASK_FUTURE_FIELD.set(scheduledTask, future);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to set 'future' in ScheduledTask", e);
        }
    }
}
