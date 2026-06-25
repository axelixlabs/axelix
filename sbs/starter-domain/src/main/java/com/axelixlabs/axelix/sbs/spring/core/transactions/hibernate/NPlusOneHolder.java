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
package com.axelixlabs.axelix.sbs.spring.core.transactions.hibernate;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * Thread-local storage for N+1 and Batch+1 query detection within a single transaction.
 *
 * @author Nikita Kirillov
 */
public final class NPlusOneHolder {

    // assume no REQUIRES_NEW transactions by default
    private final ThreadLocal<Deque<NPlusOneContext>> contextStack = ThreadLocal.withInitial(() -> new ArrayDeque<>(1));

    /**
     * Pushes a new context onto the stack.
     * Must be called at the start of each tracked transaction.
     */
    public void startContext() {
        contextStack.get().push(new NPlusOneContext());
    }

    /**
     * Pops and returns the current context from the stack.
     * Must be called in the {@code finally} block of each tracked transaction.
     *
     * @return the completed context, or {@code null} if the stack is empty
     */
    @Nullable
    public NPlusOneContext popContext() {
        Deque<NPlusOneContext> stack = contextStack.get();
        NPlusOneContext currentContext = stack.poll();
        if (stack.isEmpty()) {
            contextStack.remove();
        }
        return currentContext;
    }

    /**
     * Clears all contexts from the stack.
     * Called by {@code NPlusOneCleanupFilter} after each HTTP request
     * to prevent ThreadLocal leaks when OSIV is enabled.
     */
    public void clearAll() {
        contextStack.remove();
    }

    /**
     * Records a lazy collection load triggered by {@code EventType.INIT_COLLECTION}.
     *
     * @param role    collection role (e.g. {@code com.example.Order.items})
     * @param ownerId ID of the entity that owns the collection
     * @param isBatch {@code true} if batch loaded, otherwise {@code false}
     */
    public void recordCollectionLoad(String role, Object ownerId, boolean isBatch) {
        NPlusOneContext currentContext = currentContext();
        if (currentContext != null) {
            currentContext.collectionLoadsSnapshot.record(role, ownerId, currentContext.sqlCounter, isBatch);
        }
    }

    /**
     * Records an owning-side entity load triggered by {@code EventType.LOAD}.
     *
     * @param entityClassName fully qualified entity class name
     * @param id              loaded entity ID
     * @param isBatch         {@code true} if batch loaded, otherwise {@code false}
     */
    public void recordEntityLoad(String entityClassName, Object id, boolean isBatch) {
        NPlusOneContext currentContext = currentContext();
        if (currentContext != null) {
            currentContext.entityLoadsSnapshot.record(entityClassName, id, currentContext.sqlCounter, isBatch);
        }
    }

    /**
     * Increments the SQL statement counter for the current context.
     * Called from {@code ProxyingPreparedStatement} before each SQL execution.
     */
    public @Nullable Integer incrementAndGetSqlCount() {
        NPlusOneContext currentContext = currentContext();
        return currentContext != null ? ++currentContext.sqlCounter : null;
    }

    @Nullable
    private NPlusOneContext currentContext() {
        return contextStack.get().peek();
    }

    /**
     * Holds all N+1 and Batch+1 tracking data for a single transaction boundary.
     */
    public static class NPlusOneContext {

        private final LoadSnapshot collectionLoadsSnapshot = new LoadSnapshot();
        private final LoadSnapshot entityLoadsSnapshot = new LoadSnapshot();

        /**
         * Counts SQL statements executed within this transaction.
         * Used to assign snapshots for distinguishing bulk loads from secondary selects.
         */
        private int sqlCounter = 0;

        public LoadSnapshot getCollectionLoadsSnapshot() {
            return collectionLoadsSnapshot;
        }

        public LoadSnapshot getEntityLoadsSnapshot() {
            return entityLoadsSnapshot;
        }
    }

    public static class LoadSnapshot {
        // Key: role or entityClassName
        // Value: map of id → sqlIndex
        private final Map<String, Map<Object, Integer>> standardLoads = new HashMap<>();
        private final Map<String, Map<Object, Integer>> batchLoads = new HashMap<>();

        public void record(String targetName, Object id, int sqlCounter, boolean isBatch) {
            Map<String, Map<Object, Integer>> targetMap = isBatch ? batchLoads : standardLoads;
            targetMap.computeIfAbsent(targetName, k -> new HashMap<>()).put(id, sqlCounter);
        }

        public Map<String, Map<Object, Integer>> getStandardLoads() {
            return standardLoads;
        }

        public Map<String, Map<Object, Integer>> getBatchLoads() {
            return batchLoads;
        }
    }
}
