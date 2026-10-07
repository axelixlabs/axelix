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

import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskExecution;
import com.axelixlabs.axelix.sbs.spring.core.log.Logger;

/**
 * Bounded in-memory history of scheduled task executions, kept per task. The executions are consumed by the
 * mark/commit/rollback protocol for the purposes of minimizing the chance of losing the execution:
 *
 * <ul>
 *   <li>every {@linkplain #record(ScheduledTaskExecution) recorded} execution receives a monotonically increasing
 *   generation based on the insertion order;</li>
 *   <li>{@link #mark()} returns the recorded executions ordered by generation and remembers the watermark of that
 *   snapshot. It never drops anything;</li>
 *   <li>{@link #commit()} drops the executions whose generation does not exceed the remembered watermark once the
 *   snapshot has been successfully delivered;</li>
 *   <li>{@link #rollback()} releases the remembered watermark without dropping anything, for the case when the
 *   delivery has failed and the executions have to stay in the history for the next attempt.</li>
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
    // the watermark of the snapshot that has been marked but neither committed nor rolled back yet, null if there is
    // none
    private final AtomicReference<@Nullable Long> markedGeneration;
    private final Logger logger;

    public ScheduledTaskExecutionHistory(ScheduledTaskHistoryConfigurationProperties properties, Logger logger) {
        this.properties = properties;
        this.insertionCounter = new AtomicLong(0L);
        this.history = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
        this.markedGeneration = new AtomicReference<>();
        this.logger = logger;
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
     * Returns the recorded executions ordered by generation and remembers the watermark of this snapshot, so that
     * {@link #commit()} is able to drop them once the caller has delivered them. Nothing is removed from the history here.
     *
     * <p>Only one watermark is remembered at a time, so a mark made while the previous one is still pending cannot
     * advance it: the pending watermark is kept as is and the situation is logged. Overwriting it would let a later
     * {@link #commit()} drop the executions that no delivered snapshot has ever carried, which loses data instead of
     * merely duplicating a delivery.
     *
     * <p>The caller is expected to close every {@code mark()} with either {@link #commit()} or {@link #rollback()},
     * depending on the delivery outcome.
     *
     * @return the immutable list of the recorded executions ordered by generation.
     */
    public List<ScheduledTaskExecution> mark() {
        Lock writeLock = null;
        try {
            writeLock = lock.writeLock();
            writeLock.lock();

            List<ScheduledTaskExecution> executions = snapshotOrderedByGeneration();

            if (markedGeneration.compareAndSet(null, insertionCounter.get())) {
                return executions;
            }

            logger.info("The scheduled task executions are marked while the previous mark has neither been committed "
                    + "nor rolled back yet. The previous watermark is kept, therefore the executions recorded in "
                    + "between are delivered once again on the next attempt.");

            return executions;
        } finally {
            if (writeLock != null) {
                writeLock.unlock();
            }
        }
    }

    /**
     * Drops the executions whose generation does not exceed the watermark remembered by the last {@link #mark()}, and
     * releases that watermark.
     */
    public void commit() {
        Long watermark = markedGeneration.getAndSet(null);
        if (watermark == null) {
            return;
        }
        commit(watermark);
    }

    /**
     * Drops the executions whose generation does not exceed the given generation. Called by {@link #commit()} once the
     * snapshot obtained from {@link #mark()} has been successfully delivered, so that the delivered executions are not
     * sent again. Executions recorded after the watermark are kept.
     *
     * @param generation the generation up to which the executions are considered delivered.
     */
    private void commit(long generation) {
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

    /**
     * Releases the watermark remembered by the last {@link #mark()} without dropping anything, for the case when the
     * delivery has failed.
     */
    public void rollback() {
        markedGeneration.set(null);
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

    private List<ScheduledTaskExecution> snapshotOrderedByGeneration() {
        // here, we're risking a bit since we're getting a shallow copy of ScheduledTaskExecution
        // We cannot make ScheduledTaskExecution immutable since it is auto-generated by openapi generator,
        // but we can assume that by its nature ScheduledTaskExecution is supposed to be immutable.
        return history.values().stream()
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(Entry::generation))
                .map(Entry::execution)
                .collect(Collectors.toUnmodifiableList());
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
