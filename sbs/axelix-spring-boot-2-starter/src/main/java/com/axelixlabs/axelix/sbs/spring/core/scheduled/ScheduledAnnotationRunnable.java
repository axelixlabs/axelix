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

import java.lang.reflect.Method;

import org.springframework.scheduling.SchedulingAwareRunnable;
import org.springframework.scheduling.support.ScheduledMethodRunnable;

import com.axelixlabs.axelix.common.contract.metadata.ScheduledTaskLaunchType;

/**
 * The runnable of the task declared by a single {@code @Scheduled} annotation. Its string representation is the id of
 * the task, which tells apart the tasks of the repeatable {@code @Scheduled} annotations of the same method.
 *
 * <p>It is the {@link ScheduledMethodRunnable} of the annotated method, as without Axelix, for the libraries expecting
 * the runnable of a {@code @Scheduled} method to be the {@link ScheduledMethodRunnable}, e.g. ShedLock.
 *
 * @author Nikita Kirillov
 */
public final class ScheduledAnnotationRunnable extends ScheduledMethodRunnable implements SchedulingAwareRunnable {

    private final Runnable delegate;
    private final String id;
    private final ScheduledTaskExecutionHistory history;
    private final ScheduledTaskLaunchType launchType;

    /**
     * @param target the bean declaring the annotated method.
     * @param method the annotated method.
     */
    public ScheduledAnnotationRunnable(
            Runnable delegate, String id, Object target, Method method, ScheduledTaskExecutionHistory history) {
        this(delegate, id, target, method, history, ScheduledTaskLaunchType.SCHEDULED);
    }

    private ScheduledAnnotationRunnable(
            Runnable delegate,
            String id,
            Object target,
            Method method,
            ScheduledTaskExecutionHistory history,
            ScheduledTaskLaunchType launchType) {
        super(target, method);
        this.delegate = delegate;
        this.id = id;
        this.history = history;
        this.launchType = launchType;
    }

    /**
     * @return the runnable of the same task, whose executions are recorded as the manually launched ones.
     */
    public ScheduledAnnotationRunnable manual() {
        return new ScheduledAnnotationRunnable(
                delegate, id, getTarget(), getMethod(), history, ScheduledTaskLaunchType.MANUAL);
    }

    @Override
    public void run() {
        history.recordExecution(id, launchType, delegate);
    }

    @Override
    public boolean isLongLived() {
        return delegate instanceof SchedulingAwareRunnable && ((SchedulingAwareRunnable) delegate).isLongLived();
    }

    @Override
    public String toString() {
        return id;
    }
}
