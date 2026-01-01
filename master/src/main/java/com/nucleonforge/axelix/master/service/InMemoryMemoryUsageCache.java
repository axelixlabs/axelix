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

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jspecify.annotations.NullMarked;

import org.springframework.stereotype.Service;

import com.nucleonforge.axelix.master.model.instance.InstanceId;

/**
 * An in-memory representation of {@link MemoryUsageCache}.
 *
 * @author Mikhail Polivakha
 */
@NullMarked
@Service
public class InMemoryMemoryUsageCache implements MemoryUsageCache {

    // InstanceId is an immutable record so we're safe here.
    private final ConcurrentMap<InstanceId, Double> cache;

    public InMemoryMemoryUsageCache() {
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public double getHeapSize(InstanceId instanceId) {
        return Optional.ofNullable(cache.get(instanceId)).orElse(-1d);
    }

    @Override
    public void putHeapSize(InstanceId instanceId, double rss) {
        cache.put(instanceId, rss);
    }

    @Override
    public double getAverageHeapSize() {
        return cache.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(-1d);
    }

    @Override
    public double getTotalHeapSize() {
        return cache.values().stream().reduce(0d, Double::sum);
    }
}
