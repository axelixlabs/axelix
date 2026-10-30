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

import org.springframework.scheduling.SchedulingAwareRunnable;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskLaunchType;

/**
 * The runnable of the task declared by a single {@code @Scheduled} annotation. Its string representation is the id of
 * the task, which tells apart the tasks of the repeatable {@code @Scheduled} annotations of the same method.
 *
 * <p>The execution of a regular task is recorded right here. The execution of a reactive task ends only when its
 * publisher completes, so it is recorded by the {@link ScheduledTaskExecutionObservationHandler}.
 *
 * @author Nikita Kirillov
 */
public final class ScheduledAnnotationRunnable implements SchedulingAwareRunnable {

    private static final ThreadLocal<@Nullable ScheduledAnnotationRunnable> CURRENT_REACTIVE_TASK = new ThreadLocal<>();

    private final Runnable delegate;
    private final String id;
    private final boolean reactive;
    private final ScheduledTaskExecutionHistory history;
    private final ScheduledTaskLaunchType launchType;

    public ScheduledAnnotationRunnable(
            Runnable delegate, String id, boolean reactive, ScheduledTaskExecutionHistory history) {
        this(delegate, id, reactive, history, ScheduledTaskLaunchType.SCHEDULED);
    }

    private ScheduledAnnotationRunnable(
            Runnable delegate,
            String id,
            boolean reactive,
            ScheduledTaskExecutionHistory history,
            ScheduledTaskLaunchType launchType) {
        this.delegate = delegate;
        this.id = id;
        this.reactive = reactive;
        this.history = history;
        this.launchType = launchType;
    }

    /**
     * @return the runnable of the same task, whose executions are recorded as the manually launched ones.
     */
    public ScheduledAnnotationRunnable manual() {
        return new ScheduledAnnotationRunnable(delegate, id, reactive, history, ScheduledTaskLaunchType.MANUAL);
    }

    @Override
    public void run() {
        if (reactive) {
            runReactive();
        } else {
            history.recordExecution(id, launchType, delegate);
        }
    }

    private void runReactive() {
        CURRENT_REACTIVE_TASK.set(this);
        try {
            delegate.run();
        } finally {
            CURRENT_REACTIVE_TASK.remove();
        }
    }

    /**
     * @return the reactive task that the current thread is subscribing to, if any.
     */
    static @Nullable ScheduledAnnotationRunnable currentReactiveTask() {
        return CURRENT_REACTIVE_TASK.get();
    }

    ScheduledTaskLaunchType getLaunchType() {
        return launchType;
    }

    @Override
    public boolean isLongLived() {
        return delegate instanceof SchedulingAwareRunnable schedulingAwareRunnable
                && schedulingAwareRunnable.isLongLived();
    }

    @Override
    public @Nullable String getQualifier() {
        return delegate instanceof SchedulingAwareRunnable schedulingAwareRunnable
                ? schedulingAwareRunnable.getQualifier()
                : null;
    }

    @Override
    public String toString() {
        return id;
    }
}
