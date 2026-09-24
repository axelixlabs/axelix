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

import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.axelixlabs.axelix.common.api.registration.insights.ScheduledTaskExecution;

/**
 * Bounded in-memory history of scheduled task executions, kept per task. The executions are handed over to the
 * strict-majority through the mark/commit protocol:
 *
 * <ul>
 *   <li>every {@linkplain #record(ScheduledTaskExecution) recorded} execution receives a monotonically increasing
 *   generation based on the insertion order;</li>
 *   <li>{@link #mark()} returns an immutable snapshot of the recorded executions together with the watermark equal to
 *   the generation of the last recorded execution. It is a pure read and never changes the history;</li>
 *   <li>{@link #commit(long)} drops the executions whose generation does not exceed the watermark once the snapshot
 *   has been successfully delivered;</li>
 * </ul>
 *
 * @author Vyacheslav Yanin
 */
public class ScheduledTaskExecutionHistory {

    private final Map<String, Deque<Entry>> history;
    private final ScheduledTaskHistoryConfigurationProperties properties;
    private final Object lock;
    private long insertionCounter;

    public ScheduledTaskExecutionHistory(ScheduledTaskHistoryConfigurationProperties properties) {
        this.properties = properties;
        this.insertionCounter = 0L;
        this.history = new ConcurrentHashMap<>();
        this.lock = new Object();
    }

    /**
     * Records a single execution of a scheduled task. The execution receives the next generation and is stored in the
     * queue of its task. When the queue grows beyond the configured limit, its oldest entries are evicted.
     *
     * @param execution the execution to record.
     */
    public void record(ScheduledTaskExecution execution) {
        synchronized (lock) {
            Deque<Entry> deque = history.computeIfAbsent(execution.getTaskId(), taskId -> new LinkedList<>());
            deque.addLast(new Entry(execution, ++insertionCounter));
            evictIfNeeded(deque);
        }
    }

    private void evictIfNeeded(Deque<Entry> deque) {
        while (deque.size() > properties.getHistoryMaxSize()) {
            deque.removeFirst();
        }
    }

    /**
     * Returns an immutable snapshot of the recorded executions without modifying the history. The executions are
     * ordered by generation. The watermark carried by the snapshot equals the generation of the last recorded
     * execution and is meant to be passed to {@link #commit(long)} once the snapshot has been delivered.
     *
     * @return the snapshot of the recorded executions and the current watermark.
     */
    public HistorySnapshot mark() {
        synchronized (lock) {
            List<ScheduledTaskExecution> executions = history.values().stream()
                    .flatMap(Deque::stream)
                    .sorted(Comparator.comparingLong(Entry::generation))
                    .map(Entry::execution)
                    .collect(Collectors.toUnmodifiableList());
            return new HistorySnapshot(executions, insertionCounter);
        }
    }

    /**
     * Drops the executions whose generation does not exceed the given watermark. Typically called once the snapshot
     * obtained from {@link #mark()} has been successfully delivered, so that the delivered executions are not sent
     * again. Executions recorded after the watermark are kept.
     *
     * @param watermark the generation up to which the executions are considered delivered.
     */
    public void commit(long watermark) {
        synchronized (lock) {
            history.values().forEach(deque -> deque.removeIf(entry -> entry.generation() <= watermark));
            history.values().removeIf(Deque::isEmpty);
        }
    }

    /**
     * An immutable snapshot of the {@link ScheduledTaskExecutionHistory} state, produced by {@link #mark()}.
     *
     * @author Vyacheslav Yanin
     */
    public static final class HistorySnapshot {

        private final List<ScheduledTaskExecution> executions;
        private final long watermark;

        private HistorySnapshot(List<ScheduledTaskExecution> executions, long watermark) {
            this.executions = executions;
            this.watermark = watermark;
        }

        public List<ScheduledTaskExecution> getExecutions() {
            return executions;
        }

        public long getWatermark() {
            return watermark;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            HistorySnapshot that = (HistorySnapshot) o;
            return watermark == that.watermark && Objects.equals(executions, that.executions);
        }

        @Override
        public int hashCode() {
            return Objects.hash(executions, watermark);
        }

        @Override
        public String toString() {
            return "HistorySnapshot{" + "executions=" + executions + ", watermark=" + watermark + '}';
        }
    }

    /**
     * An immutable entry of the {@link ScheduledTaskExecution} and recording generation.
     *
     * @author Vyacheslav Yanin
     */
    private static final class Entry {

        private final ScheduledTaskExecution execution;
        private final long generation;

        private Entry(ScheduledTaskExecution execution, long generation) {
            this.execution = execution;
            this.generation = generation;
        }

        public ScheduledTaskExecution execution() {
            return execution;
        }

        public long generation() {
            return generation;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return generation == entry.generation && Objects.equals(execution, entry.execution);
        }

        @Override
        public int hashCode() {
            return Objects.hash(execution, generation);
        }
    }
}
