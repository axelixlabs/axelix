/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
     * @return the estimate of an average heap in bytes among all the recorded services,
     *         or -1 if this {@link MemoryUsageCache} does not have any heap usages recorded yet.
     */
    double getAverageHeapSize();

    /**
     * @return get estimate of total heap usage by all instances or -1 if this
     *         {@link MemoryUsageCache} does not have any heap usages recorded yet.
     */
    double getTotalHeapSize();
}
