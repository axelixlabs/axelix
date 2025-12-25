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
package com.nucleonforge.axile.common.auth.core;

/**
 * Enumeration of authorities required for accessing specific Actuator endpoints.
 *
 * <p>Each authority corresponds to a single Spring Boot Actuator endpoint or a custom extension.</p>
 *
 * @see Authority
 * @since 28.07.2025
 * @author Nikita Kirillov
 */
public enum DefaultAuthority implements Authority {

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
     * Grants authority to view auto-configuration conditions.
     */
    CONDITIONS,

    /**
     * Grants authority to view Configuration Properties.
     */
    CONFIGPROPS,

    /**
     * Grants authority custom details endpoint.
     */
    DETAILS,

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
     * Grants authority to view controller and endpoint mappings.
     */
    MAPPINGS,

    /**
     * Grants authority to access application loggers.
     */
    LOGGERS,

    /**
     * Grants authority to view scheduledtasks endpoint.
     */
    SCHEDULED_TASKS,

    /**
     * Grants authority to view custom scheduled-tasks-management endpoint.
     */
    SCHEDULED_TASKS_MANAGEMENT;

    @Override
    public String getName() {
        return name();
    }
}
