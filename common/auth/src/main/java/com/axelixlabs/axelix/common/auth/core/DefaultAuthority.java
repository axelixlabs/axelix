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
package com.axelixlabs.axelix.common.auth.core;

/**
 * Enumeration of default authorities supported by Axelix.
 *
 * @see Authority
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public enum DefaultAuthority implements Authority {

    /**
     * Grants the right to view values in Environment.
     */
    ENV_VALUES_READ,

    /**
     * Grants the right to view values in Configuration Properties.
     */
    CONFIG_PROPS_VALUES_READ,

    /**
     * Grants the right to modify scheduled tasks.
     * <p>Allows modifying individual tasks, forcing their execution, and enabling/disabling them at runtime.</p>
     */
    SCHEDULED_TASKS_MODIFY,

    /**
     * Grants the right to clear caches.
     * <p>Allows clearing individual caches, cache managers, and all app cache at runtime.</p>
     */
    CACHES_CLEAR,

    /**
     * Grants the right to enable/disable caches.
     * <p>Allows enabling/disabling individual caches and cache managers at runtime.</p>
     */
    CACHES_TOGGLE,

    /**
     * Grants permission to perform actions for garbage collector log monitoring.
     * <p>Allows triggering, enabling, and disabling garbage collector log monitoring at runtime.</p>
     */
    GARBAGE_COLLECTOR;

    @Override
    public String getName() {
        return name();
    }
}
