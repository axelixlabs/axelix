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

import java.util.concurrent.ScheduledFuture;

import org.jspecify.annotations.Nullable;

import org.springframework.scheduling.config.Task;

/**
 * Holder for rescheduled task state.
 *
 * @since 03.03.2026
 * @author Aleksei Ermakov
 */
final class RescheduledState {

    private final Task task;

    private final @Nullable ScheduledFuture<?> future;

    /**
     * Creates a state holder for updated scheduled task internals.
     *
     * @param task the new scheduled task descriptor.
     * @param future the new schedule handle, can be {@code null}.
     */
    RescheduledState(Task task, @Nullable ScheduledFuture<?> future) {
        this.task = task;
        this.future = future;
    }

    /**
     * Returns the rescheduled task descriptor.
     *
     * @return task descriptor.
     */
    Task getTask() {
        return task;
    }

    /**
     * Returns the rescheduled future handle.
     *
     * @return future handle or {@code null}.
     */
    @Nullable
    ScheduledFuture<?> getFuture() {
        return future;
    }
}
