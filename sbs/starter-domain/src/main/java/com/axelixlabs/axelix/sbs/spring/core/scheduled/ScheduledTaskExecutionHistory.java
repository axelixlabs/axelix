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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.axelixlabs.axelix.sbs.spring.core.SlidingWindow;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskExecution;

/**
 * Bounded in-memory history of the latest scheduled task executions, kept per task. The history is reported to master
 * as a whole every time, and master stores only the executions it does not know yet.
 *
 * @author Vyacheslav Yanin
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
public class ScheduledTaskExecutionHistory {

    /**
     * The maximum number of the latest executions kept per task and reported to master.
     */
    private static final int HISTORY_MAX_SIZE = 10;

    private final Map<String, SlidingWindow<ScheduledTaskExecution>> history = new ConcurrentHashMap<>();

    public void record(ScheduledTaskExecution execution) {
        history.computeIfAbsent(execution.getTaskId(), taskId -> new SlidingWindow<>(HISTORY_MAX_SIZE))
                .put(execution);
    }

    /**
     * Runs the given task and records its execution, whether it succeeds or fails.
     */
    public void recordExecution(String taskId, Runnable task) {
        Instant startedAt = Instant.now();
        long startNanos = System.nanoTime();
        Throwable error = null;
        try {
            task.run();
        } catch (Throwable e) {
            error = e;
            throw e;
        } finally {
            long durationMillis = (System.nanoTime() - startNanos) / 1_000_000;
            record(ScheduledTaskExecutions.of(taskId, startedAt, durationMillis, error));
        }
    }

    /**
     * @return the latest executions of every task, in the order of recording within a task.
     */
    public List<ScheduledTaskExecution> getExecutions() {
        return history.values().stream()
                .flatMap(window -> window.get().stream())
                .collect(Collectors.toUnmodifiableList());
    }
}
