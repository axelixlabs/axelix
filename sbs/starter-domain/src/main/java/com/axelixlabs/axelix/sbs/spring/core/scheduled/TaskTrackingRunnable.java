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

import java.time.Duration;
import java.time.Instant;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskExecution;

/**
 * Wrapper around a scheduled task that reports every invocation to the {@link ScheduledTaskExecutionHistory}.
 *
 * <p>Each invocation is measured and reported as a {@link ScheduledTaskExecution}:
 *
 * <ul>
 *   <li>{@code startedAt} is captured before the delegate is invoked, and {@code durationMillis} once it returns or
 *   throws;</li>
 *   <li>{@code success} tells whether the delegate completed without throwing: it stays {@code true} while nothing
 *   escapes the delegate, and becomes {@code false} as soon as something does;</li>
 *   <li>{@code errorType} and {@code errorMessage} carry the type and the message of the thrown exception, and are
 *   left absent when the delegate succeeded.</li>
 * </ul>
 *
 * <p>The execution is reported regardless of the outcome, and the original {@link Throwable} is rethrown afterwards.
 * Tracking is not supposed to be observable by the scheduler, so a failing task keeps failing exactly the way it would
 * have failed without this wrapper.
 *
 * @author Vyacheslav Yanin
 */
public class TaskTrackingRunnable implements Runnable {

    private final Runnable trackingTask;
    private final ScheduledTaskExecutionHistory taskExecutionHistory;

    /**
     * @param trackingTask the delegate to run and measure.
     * @param taskExecutionHistory the history to report the executions to.
     */
    public TaskTrackingRunnable(Runnable trackingTask, ScheduledTaskExecutionHistory taskExecutionHistory) {
        this.trackingTask = trackingTask;
        this.taskExecutionHistory = taskExecutionHistory;
    }

    @Override
    public void run() {
        String errorMessage = null;
        String errorType = null;
        boolean success = true;
        Instant start = Instant.now();

        try {
            trackingTask.run();
        } catch (Throwable e) {
            errorMessage = e.getMessage();
            errorType = e.getClass().getSimpleName();
            success = false;
            throw e;
        } finally {
            long durationMillis = Duration.between(start, Instant.now()).toMillis();

            var taskExecution = new ScheduledTaskExecution()
                    .taskId(trackingTask.toString())
                    .startedAt(start.toString())
                    .durationMillis(durationMillis)
                    .success(success)
                    .errorMessage(errorMessage)
                    .errorType(errorType);

            taskExecutionHistory.record(taskExecution);
        }
    }
}
