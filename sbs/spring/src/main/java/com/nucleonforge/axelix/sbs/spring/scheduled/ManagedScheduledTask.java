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

import java.lang.reflect.Field;
import java.util.concurrent.ScheduledFuture;

import org.jspecify.annotations.Nullable;

import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.CronTrigger;

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
    private final ScheduledTask scheduledTask;

    /**
     * The original CronTrigger, or {@code null} if absent.
     */
    @Nullable
    private CronTrigger cronTrigger;

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

        if (scheduledTask.getTask() instanceof CronTask cronTask) {
            this.cronTrigger = (CronTrigger) cronTask.getTrigger();
        } else {
            this.cronTrigger = null;
        }
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

    public void replaceScheduledFuture(ScheduledFuture<?> actual) {
        try {
            this.scheduledTask.cancel();
            SCHEDULED_TASK_FUTURE_FIELD.set(this.scheduledTask, actual);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to set 'future' in ScheduledTask", e);
        }
    }

    public @Nullable CronTrigger getCronTrigger() {
        return cronTrigger;
    }

    public void setCronTrigger(CronTrigger cronTrigger) {
        this.cronTrigger = cronTrigger;
    }
}
