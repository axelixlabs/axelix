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
package com.axelixlabs.axelix.master.service.dashboard;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

/**
 * {@link DashboardCache} backed by a {@link ConcurrentHashMap}.
 *
 * @author Vyacheslav Yanin
 */
public class InMemoryMapDashboardCache implements DashboardCache {

    private final ConcurrentMap<DashboardKey, Object> cache = new ConcurrentHashMap<>();

    @Override
    public <T> T get(DashboardKey key, Class<T> type, Supplier<T> loader) {
        Object value = cache.computeIfAbsent(
                key, ignored -> Objects.requireNonNull(loader.get(), "loader must not return null"));

        return type.cast(value);
    }

    @Override
    public <T> void put(DashboardKey key, T value) {
        cache.put(key, value);
    }

    @Override
    public void evict(DashboardKey key) {
        cache.remove(key);
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
