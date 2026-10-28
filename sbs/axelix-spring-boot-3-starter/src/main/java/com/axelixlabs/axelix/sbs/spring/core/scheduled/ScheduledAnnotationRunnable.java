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

import java.time.Instant;

import org.jspecify.annotations.Nullable;

import org.springframework.scheduling.SchedulingAwareRunnable;

/**
 * The runnable of the task declared by a single {@code @Scheduled} annotation. Its string representation is the id of
 * the task, which tells apart the tasks of the repeatable {@code @Scheduled} annotations of the same method.
 *
 * @author Nikita Kirillov
 */
public final class ScheduledAnnotationRunnable implements SchedulingAwareRunnable {

    private final Runnable delegate;
    private final String id;
    private final @Nullable String qualifier;
    private final ScheduledTaskExecutionHistory history;

    public ScheduledAnnotationRunnable(
            Runnable delegate, String id, @Nullable String qualifier, ScheduledTaskExecutionHistory history) {
        this.delegate = delegate;
        this.id = id;
        this.qualifier = qualifier;
        this.history = history;
    }

    @Override
    public void run() {
        // TODO: the executions of the reactive tasks are recorded wrongly, since run() only subscribes to the
        //  publisher and returns, so the duration and the error of the execution are lost. To be fixed separately.
        long startNanos = System.nanoTime();
        Throwable error = null;
        try {
            delegate.run();
        } catch (Throwable e) {
            error = e;
            throw e;
        } finally {
            long durationMillis = (System.nanoTime() - startNanos) / 1_000_000;
            history.record(ScheduledTaskExecutions.of(
                    id, Instant.ofEpochMilli(startNanos / 1_000_000), durationMillis, error));
        }
    }

    @Override
    public boolean isLongLived() {
        return delegate instanceof SchedulingAwareRunnable schedulingAwareRunnable
                && schedulingAwareRunnable.isLongLived();
    }

    /**
     * The qualifier of the scheduler that the task is routed to. Overrides the method that exists since Spring
     * Framework 6.1, so it deliberately has no {@code @Override}.
     */
    public @Nullable String getQualifier() {
        return qualifier;
    }

    @Override
    public String toString() {
        return id;
    }
}
