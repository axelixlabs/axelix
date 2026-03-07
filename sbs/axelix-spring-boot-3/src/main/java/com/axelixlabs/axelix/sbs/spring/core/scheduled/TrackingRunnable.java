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

/**
 * A {@link Runnable} wrapper that tracks execution metadata: last status, last execution time,
 * and any exception thrown. Delegates {@link #toString()} to the wrapped runnable to preserve
 * task identity.
 *
 * @author Aleksei Ermakov
 */
class TrackingRunnable implements Runnable {

    private static final class ExecutionState {
        private final @Nullable String status;
        private final @Nullable Instant time;
        private final @Nullable Throwable exception;

        private ExecutionState(@Nullable String status, @Nullable Instant time, @Nullable Throwable exception) {
            this.status = status;
            this.time = time;
            this.exception = exception;
        }
    }

    private final Runnable delegate;

    private volatile ExecutionState state = new ExecutionState(null, null, null);

    TrackingRunnable(Runnable delegate) {
        this.delegate = delegate;
    }

    @Override
    public void run() {
        state = new ExecutionState("STARTED", Instant.now(), null);
        try {
            delegate.run();
            state = new ExecutionState("SUCCESS", Instant.now(), null);
        } catch (Throwable t) {
            state = new ExecutionState("ERROR", Instant.now(), t);
            throw t;
        }
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Nullable
    public String getLastStatus() {
        return state.status;
    }

    @Nullable
    public Instant getLastTime() {
        return state.time;
    }

    @Nullable
    public Throwable getLastException() {
        return state.exception;
    }
}
