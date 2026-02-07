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
 * Enumeration of authorities required for accessing specific front-end endpoints.
 *
 * <p>Each authority corresponds to a single Spring Boot Actuator endpoint or a custom extension.</p>
 *
 * @see Authority
 * @since 28.07.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
// TODO: Move this class to Master
public enum ExternalAuthority implements Authority {

    /**
     * Grants access to actuator cache control operations.
     * <p>Allows clearing specific cache entries, entire caches, or all caches for a cache manager.</p>
     * <p>Implemented by custom actuator endpoint: {@code CacheDispatcherEndpoint}.</p>
     */
    CACHE_DISPATCHER,

    /**
     * Grants access to runtime profile management operations.
     * <p>Allows replacing the list of active Spring profiles at runtime.</p>
     * <p>Implemented by custom actuator endpoint: {@code ProfileManagementEndpoint}.</p>
     */
    PROFILE_MANAGEMENT,

    /**
     * Grants access to runtime property mutation operations.
     * <p>Allows changing configuration properties at runtime.</p>
     * <p>Implemented by custom actuator endpoint: {@code PropertyManagementEndpoint}.</p>
     */
    PROPERTY_MANAGEMENT,

    /**
     * Grants authority to view all registered Spring beans.
     */
    BEANS,

    /**
     * Grants authority to access application caches.
     */
    CACHES,

    /**
     * Grants authority to view application health.
     */
    HEALTH,

    /**
     * Grants authority to view application info metadata.
     */
    INFO,

    /**
     * Grants authority to view Configuration Properties.
     */
    CONFIGPROPS,

    /**
     * Grants authority custom details endpoint.
     */
    DETAILS,

    /**
     * Grants authority to view auto-configuration conditions.
     */
    CONDITIONS,

    /**
     * Grants authority to access environment properties.
     */
    ENV,

    /**
     * Grants authority to download a JVM heap dump.
     */
    HEAP_DUMP,

    /**
     * Grants authority to view all running JVM threads.
     */
    THREAD_DUMP,

    /**
     * Grants authority to access application metrics.
     */
    METRICS,

    /**
     * Grants authority to access application loggers.
     */
    LOGGERS,

    /**
     * Grants authority to view scheduled-tasks endpoint.
     */
    SCHEDULED_TASKS,

    /**
     * Grants authority to view controller and endpoint mappings.
     */
    MAPPINGS;

    @Override
    public String getName() {
        return name();
    }
}
