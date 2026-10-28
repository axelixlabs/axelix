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

import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.OneTimeTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import com.axelixlabs.axelix.common.utils.Lazy;

/**
 * {@link ScheduledTaskRegistrar} that wraps the runnable of every task declared by a {@code @Scheduled} annotation
 * into the {@link ScheduledAnnotationRunnable}. The tasks registered in any other way, e.g. via
 * {@code SchedulingConfigurer}, are left as is.
 *
 * @author Nikita Kirillov
 */
public class AxelixScheduledTaskRegistrar extends ScheduledTaskRegistrar {

    private final ThreadLocal<@Nullable String> processingContext = new ThreadLocal<>();

    @SuppressWarnings("NullAway.Init")
    private Lazy<ScheduledTaskExecutionHistory> history;

    void setHistory(Lazy<ScheduledTaskExecutionHistory> history) {
        this.history = history;
    }

    /**
     * Runs the processing of a {@code @Scheduled} annotation, during which the tasks are scheduled.
     *
     * @param suffix the suffix of the task id, see {@link AxelixScheduledAnnotationBeanPostProcessor}.
     */
    void process(String suffix, Runnable processing) {
        processingContext.set(suffix);
        try {
            processing.run();
        } finally {
            processingContext.remove();
        }
    }

    @Override
    public @Nullable ScheduledTask scheduleCronTask(CronTask task) {
        Runnable runnable = wrap(task.getRunnable());
        return super.scheduleCronTask(
                runnable == null ? task : new CronTask(runnable, (CronTrigger) task.getTrigger()));
    }

    @Override
    public @Nullable ScheduledTask scheduleFixedDelayTask(FixedDelayTask task) {
        Runnable runnable = wrap(task.getRunnable());
        return super.scheduleFixedDelayTask(
                runnable == null
                        ? task
                        : new FixedDelayTask(runnable, task.getIntervalDuration(), task.getInitialDelayDuration()));
    }

    @Override
    public @Nullable ScheduledTask scheduleFixedRateTask(FixedRateTask task) {
        Runnable runnable = wrap(task.getRunnable());
        return super.scheduleFixedRateTask(
                runnable == null
                        ? task
                        : new FixedRateTask(runnable, task.getIntervalDuration(), task.getInitialDelayDuration()));
    }

    @Override
    public @Nullable ScheduledTask scheduleOneTimeTask(OneTimeTask task) {
        Runnable runnable = wrap(task.getRunnable());
        return super.scheduleOneTimeTask(
                runnable == null ? task : new OneTimeTask(runnable, task.getInitialDelayDuration()));
    }

    /**
     * @return the wrapped runnable, or {@code null} if the task is not being scheduled for a {@code @Scheduled}
     *         annotation. That is also the case when Spring schedules the task again, after it has been registered
     *         before the scheduler was resolved.
     */
    private @Nullable Runnable wrap(Runnable runnable) {
        String suffix = processingContext.get();
        if (suffix == null) {
            return null;
        }
        return new ScheduledAnnotationRunnable(runnable, runnable + "#" + suffix, history.require());
    }
}
