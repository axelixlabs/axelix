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
package com.nucleonforge.axelix.master.service;

import org.jspecify.annotations.NullMarked;

import com.nucleonforge.axelix.master.model.instance.Instance;
import com.nucleonforge.axelix.master.model.instance.InstanceId;

/**
 * Stores our best estimate of how much memory the given {@link Instance} consumes.
 *
 * @author Mikhail Polivakha
 */
@NullMarked
public interface MemoryUsageCache {

    /**
     * Get the heap usage for the {@link Instance} with the given {@link InstanceId}.
     *
     * @param instanceId the id of the {@link Instance} for which heap usage is recorded.
     * @return the estimated amount of heap in bytes that is occupied by instance,
     *         identified by passed {@link InstanceId}, or -1 if the heap usage for the
     *         given {@link InstanceId} is not recorded.
     */
    double getHeapSize(InstanceId instanceId);

    /**
     * Record the heap usage for the {@link Instance} with the given {@link InstanceId}.
     *
     * @param instanceId the id of the {@link Instance} for which heap usage is recorded.
     * @param heap the estimated amount of heap in bytes that have been used
     *           by an {@link Instance} identified by passed {@link InstanceId}.
     */
    void putHeapSize(InstanceId instanceId, double heap);

    /**
     * Clear recording for of heap usage for the {@link Instance} with the given {@link InstanceId}.
     *
     * @param instanceId the id of the {@link Instance} for which heap usage is recorded.
     */
    void clear(InstanceId instanceId);

    /**
     * @return the estimate of an average heap in bytes among all the recorded services,
     *         or -1 if this {@link MemoryUsageCache} does not have any heap usages recorded yet.
     */
    double getAverageHeapSize();

    /**
     * @return get estimate of total heap usage by all instances or -1 if this
     *         {@link MemoryUsageCache} does not have any heap usages recorded yet.
     */
    double getTotalHeapSize();

    /**
     * Clears all cached heap usage data. The cache will be empty after this call returns.
     */
    void clear();
}
