/*
 * Copyright 2025-present, Nucleon Forge Software.
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
package com.nucleonforge.axelix.sbs.spring.scheduled;

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
