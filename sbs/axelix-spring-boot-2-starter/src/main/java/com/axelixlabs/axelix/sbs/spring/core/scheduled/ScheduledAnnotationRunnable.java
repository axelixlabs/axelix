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

import org.springframework.scheduling.SchedulingAwareRunnable;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskLaunchType;

/**
 * The runnable of the task declared by a single {@code @Scheduled} annotation. Its string representation is the id of
 * the task, which tells apart the tasks of the repeatable {@code @Scheduled} annotations of the same method.
 *
 * @author Nikita Kirillov
 */
public final class ScheduledAnnotationRunnable implements SchedulingAwareRunnable {

    private final Runnable delegate;
    private final String id;
    private final ScheduledTaskExecutionHistory history;

    public ScheduledAnnotationRunnable(Runnable delegate, String id, ScheduledTaskExecutionHistory history) {
        this.delegate = delegate;
        this.id = id;
        this.history = history;
    }

    @Override
    public void run() {
        // TODO: the manual launches are recorded as the scheduled ones too, until they are told apart as in SB3
        history.recordExecution(id, ScheduledTaskLaunchType.SCHEDULED, delegate);
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
