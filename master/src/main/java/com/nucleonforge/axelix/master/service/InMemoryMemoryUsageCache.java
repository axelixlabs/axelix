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
    public void clear(InstanceId instanceId) {
        this.cache.remove(instanceId);
    }

    @Override
    public void clear() {
        cache.clear();
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
