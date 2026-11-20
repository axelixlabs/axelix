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
package com.axelixlabs.axelix.sbs.spring.core.cache;

import java.util.concurrent.atomic.AtomicBoolean;

import org.jspecify.annotations.NonNull;

import com.axelixlabs.axelix.sbs.spring.core.SlidingWindow;

/**
 * Management state of a named cache within a cache manager.
 * Preserves the enabled status and lookup history when the underlying cache instance changes.
 *
 * @author Nikita Kirillov
 */
public final class CacheManagementState {

    private final AtomicBoolean enabled;
    private final SlidingWindow<@NonNull CacheLookup> cacheLookupHistory;

    public CacheManagementState() {
        this.enabled = new AtomicBoolean(true);
        // TODO: We need to find a way to allow for configuring those values
        this.cacheLookupHistory = new SlidingWindow<>(200);
    }

    public AtomicBoolean getEnabled() {
        return enabled;
    }

    public SlidingWindow<@NonNull CacheLookup> getCacheLookupHistory() {
        return cacheLookupHistory;
    }
}
