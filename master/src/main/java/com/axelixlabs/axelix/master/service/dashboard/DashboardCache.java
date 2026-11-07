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

import java.util.function.Supplier;

/**
 * A cache of dashboard-related snapshots, shared by every dashboard.
 *
 * @author Vyacheslav Yanin
 */
public interface DashboardCache {

    /**
     * Returns the value cached under the given key, computing and storing it first when the key is absent.
     *
     * @param type the expected type of the cached value
     * @param loader the loader used to compute the value when the key is absent
     * @return the cached value or the value produced by the loader
     */
    <T> T get(DashboardKey key, Class<T> type, Supplier<T> loader);

    <T> void put(DashboardKey key, T value);

    void evict(DashboardKey key);

    void clear();
}
