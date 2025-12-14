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
package com.nucleonforge.axile.master.service;

import org.jspecify.annotations.NullMarked;

import com.nucleonforge.axile.master.model.instance.Instance;
import com.nucleonforge.axile.master.model.instance.InstanceId;

/**
 * Stores our best estimate of how much memory the given {@link Instance} consumes.
 *
 * @author Mikhail Polivakha
 */
@NullMarked
public interface MemoryUsageCache {

    /**
     * Get the RSS usage for the {@link Instance} with the given {@link InstanceId}.
     *
     * @param instanceId the id of the {@link Instance} for which RSS usage is recorded.
     * @return the estimated amount of RSS in bytes that is occupied by instance,
     *         identified by passed {@link InstanceId}, or -1 if the RSS usage for the
     *         given {@link InstanceId} is not recorded.
     */
    double getRss(InstanceId instanceId);

    /**
     * Record the RSS usage for the {@link Instance} with the given {@link InstanceId}.
     *
     * @param instanceId the id of the {@link Instance} for which RSS usage is recorded.
     * @param rss the estimated amount of RSS in bytes that have been used
     *           by an {@link Instance} identified by passed {@link InstanceId}.
     */
    void putRss(InstanceId instanceId, double rss);

    /**
     * @return the estimate of an average RSS in bytes among all the recorded services,
     *         or -1 if this {@link MemoryUsageCache} does not have any RSS usages recorded yet.
     */
    double getAverageRss();
}
