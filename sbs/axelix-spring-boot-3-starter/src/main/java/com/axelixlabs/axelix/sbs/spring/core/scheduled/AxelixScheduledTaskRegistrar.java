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

import io.micrometer.observation.ObservationRegistry;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import com.axelixlabs.axelix.common.utils.Lazy;

/**
 * {@link ScheduledTaskRegistrar} that wraps the runnable of every task declared by a {@code @Scheduled} annotation
 * into the {@link ScheduledAnnotationRunnable}. The tasks registered in any other way, e.g. via
 * {@code SchedulingConfigurer}, are left as is.
 *
 * <p>The one-time tasks of Spring Framework 6.1+ are left as is too.
 *
 * <p>The executions of the reactive tasks end only when their publishers complete, which only the observations of
 * Spring Framework 6.1+ know about. So the observation registry of this registrar always records them, whether the
 * application has its own registry or not.
 *
 * @author Nikita Kirillov
 */
public class AxelixScheduledTaskRegistrar extends ScheduledTaskRegistrar {

    private static final Logger log = LoggerFactory.getLogger(AxelixScheduledTaskRegistrar.class);

    private final ThreadLocal<@Nullable ScheduledAnnotationContext> processingContext = new ThreadLocal<>();

    @SuppressWarnings("NullAway.Init")
    private Lazy<ScheduledTaskExecutionHistory> history;

    private @Nullable ObservationRegistry observationRegistry;

    private boolean reactiveTaskScheduled;

    void setHistory(Lazy<ScheduledTaskExecutionHistory> history) {
        this.history = history;
    }

    /**
     * Overrides the method that exists since Spring Framework 6.1, so it deliberately has no {@code @Override}.
     */
    public void setObservationRegistry(@Nullable ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    /**
     * Overrides the method that exists since Spring Framework 6.1, so it deliberately has no {@code @Override}.
     *
     * @return the registry of the application, if any, or an own one, in both cases recording the executions of the
     *         reactive tasks. Absent before {@link #afterPropertiesSet()} and without the support of the reactive tasks.
     */
    public @Nullable ObservationRegistry getObservationRegistry() {
        return observationRegistry;
    }

    /**
     * Makes the observation registry record the executions of the reactive tasks. The registry of the application is
     * set before, and the tasks start running only after.
     */
    @Override
    public void afterPropertiesSet() {
        // unlike Spring Framework 6.0, Spring Framework 6.1+ that supports the reactive tasks depends on Micrometer
        if (AxelixScheduledAnnotationBeanPostProcessor.REACTIVE_SUPPORT_PRESENT) {
            ObservationRegistry applicationRegistry = observationRegistry;
            observationRegistry =
                    ScheduledTaskExecutionObservationHandler.recording(applicationRegistry, history.require());
            if (reactiveTaskScheduled
                    && observationRegistry == applicationRegistry
                    && !ScheduledTaskExecutionObservationHandler.observesScheduledTasks(applicationRegistry)) {
                log.warn("The observations of the scheduled tasks are disabled in the application, e.g. by the "
                        + "'management.observations.enable.tasks.scheduled.execution' property, so the executions of "
                        + "the reactive @Scheduled tasks are not recorded");
            }
        }
        super.afterPropertiesSet();
    }

    /**
     * Runs the processing of the given {@code @Scheduled} context, during which the tasks are scheduled.
     */
    void process(ScheduledAnnotationContext context, Runnable processing) {
        processingContext.set(context);
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

    /**
     * @return the wrapped runnable, or {@code null} if the task is not being scheduled for a {@code @Scheduled}
     *         annotation. That is also the case when Spring schedules the task again, after it has been registered
     *         before the scheduler was resolved.
     */
    private @Nullable Runnable wrap(Runnable runnable) {
        ScheduledAnnotationContext annotation = processingContext.get();
        if (annotation == null) {
            return null;
        }
        if (annotation.reactive()) {
            reactiveTaskScheduled = true;
        }
        return new ScheduledAnnotationRunnable(
                runnable,
                runnable + "#" + annotation.suffix(),
                annotation.qualifier(),
                annotation.reactive(),
                history.require());
    }

    /**
     * @param suffix the suffix of the task id, see {@link AxelixScheduledAnnotationBeanPostProcessor}.
     * @param qualifier the qualifier of the scheduler declared by the annotation, if any.
     * @param reactive whether the annotated method is the reactive one.
     */
    record ScheduledAnnotationContext(
            String suffix, @Nullable String qualifier, boolean reactive) {}
}
