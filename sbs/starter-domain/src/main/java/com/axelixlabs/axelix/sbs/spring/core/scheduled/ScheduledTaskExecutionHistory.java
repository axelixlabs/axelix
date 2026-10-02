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

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskExecution;

/**
 * Bounded in-memory history of scheduled task executions, kept per task. The executions are consumed by the
 * mark/commit protocol for the purposes of minimizing the chance of losing the execution:
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
 * @author Mikhail Polivakha
 */
public class ScheduledTaskExecutionHistory {

    private final Map<String, Deque<Entry>> history;
    private final ScheduledTaskHistoryConfigurationProperties properties;
    // lock to synchronize the record and the snapshotting of the history
    private final ReadWriteLock lock;
    private final AtomicLong insertionCounter;

    public ScheduledTaskExecutionHistory(ScheduledTaskHistoryConfigurationProperties properties) {
        this.properties = properties;
        this.insertionCounter = new AtomicLong(0L);
        this.history = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
    }

    /**
     * Records a single execution of a scheduled task. The execution receives the next generation and is stored in the
     * queue of its task. There're a couple of assumptions regarding this method:
     * <ol>
     *    <li>Here is that the amount of contention on this method from the {@code @Scheduled} threads will be low.</li>
     *    <li>This contention will mainly exist between various tasks, rather than within the task.</li>
     * </ol>
     * <p>
     * Still, we must acknowledge that contention MAY happen within the single task (identified by taskId)
     * when the both conditions are true:
     * <p>
     * <ol>
     *    <li>The thread pool that backs the scheduling is configured to have size greater than 1, so the
     *    contention is possible in principle.</li>
     *    <li>The task itself is fixedRate, so it does not wait for termination of its execution.</li>
     * </ol>
     * <p>
     * So ideally we do not want concurrent invocations of this method of the different {@code Scheduled}
     * tasks to block each other, and they do not have to. Also, there is a room for contention within
     * taskId (as explained above), so we must account for that. And finally, the event of marking is going
     * to be rare,
     * <p>
     * With that in mind, read the implementation below.
     *
     * @param execution the execution to record.
     */
    public void record(ScheduledTaskExecution execution) {
        Lock snapshotLock = null;
        try {
            // we get read lock and not write (counterintuitively) so that this lock instance is
            // locked by corresponding write lock, but sibling read locks do not block each other.
            snapshotLock = lock.readLock();
            snapshotLock.lock();

            putInternal(execution);
        } finally {
            if (snapshotLock != null) {
                snapshotLock.unlock();
            }
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
        Lock writeLock = null;
        try {
            writeLock = lock.writeLock();
            writeLock.lock();

            Map<String, List<ScheduledTaskExecution>> copy = new HashMap<>(history.size());

            history.forEach((s, entries) -> {
                // here, we're risking a bit since we're getting a shallow copy of ScheduledTaskExecution
                // We cannot make ScheduledTaskExecution immutable since it is auto-generated by openapi generator,
                // but we can assume that by its nature ScheduledTaskExecution is supposed to be immutable.
                copy.put(s, entries.stream().map(Entry::execution).collect(Collectors.toUnmodifiableList()));
            });

            return new HistorySnapshot(copy, insertionCounter.get());
        } finally {
            if (writeLock != null) {
                writeLock.unlock();
            }
        }
    }

    /**
     * Drops the executions whose generation does not exceed the given generation. Typically called once the snapshot
     * obtained from {@link #mark()} has been successfully delivered, so that the delivered executions are not sent
     * again. Executions recorded after the watermark are kept.
     *
     * @param generation the generation up to which the executions are considered delivered.
     */
    public void commit(long generation) {
        Lock writeLock = null;
        try {
            writeLock = lock.writeLock();
            writeLock.lock();

            history.values().forEach(deque -> deque.removeIf(entry -> entry.generation() <= generation));
            history.values().removeIf(Deque::isEmpty);
        } finally {
            if (writeLock != null) {
                writeLock.unlock();
            }
        }
    }

    private void putInternal(ScheduledTaskExecution execution) {
        Deque<Entry> deque = history.computeIfAbsent(execution.getTaskId(), taskId -> new LinkedList<>());
        deque.addLast(new Entry(execution, insertionCounter.incrementAndGet()));

        // synchronizing on the deque to prevent the race between concurrent
        // executions of the same task, which is possible, but quite rare.
        synchronized (deque) {
            while (deque.size() > properties.getHistoryMaxSize()) {
                deque.removeFirst();
            }
        }
    }

    /**
     * An immutable snapshot of the {@link ScheduledTaskExecutionHistory} state, produced by {@link #mark()}.
     *
     * @author Vyacheslav Yanin
     */
    public static final class HistorySnapshot {

        private final Map<String, List<ScheduledTaskExecution>> executions;
        private final long generation;

        private HistorySnapshot(Map<String, List<ScheduledTaskExecution>> executions, long generation) {
            this.executions = executions;
            this.generation = generation;
        }

        public Map<String, List<ScheduledTaskExecution>> getExecutions() {
            return executions;
        }

        public long getGeneration() {
            return generation;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            HistorySnapshot that = (HistorySnapshot) o;
            return generation == that.generation && Objects.equals(executions, that.executions);
        }

        @Override
        public int hashCode() {
            return Objects.hash(executions, generation);
        }

        @Override
        public String toString() {
            return "HistorySnapshot{" + "executions=" + executions + ", generation=" + generation + '}';
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
    }
}
