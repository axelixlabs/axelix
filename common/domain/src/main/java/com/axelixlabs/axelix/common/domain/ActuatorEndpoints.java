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
package com.axelixlabs.axelix.common.domain;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.axelixlabs.axelix.common.domain.http.HttpMethod;

/**
 * Represents all possible {@link ActuatorEndpoint Axelix actuator endpoints} that are being used in the system.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
public class ActuatorEndpoints implements Iterable<ActuatorEndpoint> {

    private static final Set<ActuatorEndpoint> ENDPOINTS = new HashSet<>();

    private static final ActuatorEndpoints INSTANCE = new ActuatorEndpoints();

    /**
     * NOTE: Spring Actuator shows warnings for endpoint IDs containing dots or hyphens,
     * but they remain functionally valid. The WARNING_PATTERN [.-]+ triggers alerts
     * while VALID_PATTERN still permits these characters.
     * <p>
     * This appears to be a code style recommendation rather than a technical limitation,
     * and it seems to us that this will not affect anything.
     * Our endpoints with hyphens work correctly despite the warnings.
     */
    // spotless:off

    // Beans
    public static final ActuatorEndpoint GET_BEANS = endpoint("/axelix-beans", HttpMethod.GET);

    // Caches
    public static final ActuatorEndpoint CLEAR_SINGLE_CACHE =
            endpoint("/axelix-caches/{cacheManagerName}/{cacheName}/clear", HttpMethod.DELETE);
    public static final ActuatorEndpoint CLEAR_ALL_CACHES = endpoint("/axelix-caches/clear", HttpMethod.DELETE);
    public static final ActuatorEndpoint CLEAR_SINGLE_CACHE_MANAGER =
            endpoint("/axelix-caches/{cacheManagerName}/clear-all", HttpMethod.DELETE);
    public static final ActuatorEndpoint GET_ALL_CACHES = endpoint("/axelix-caches", HttpMethod.GET);
    public static final ActuatorEndpoint GET_SINGLE_CACHE =
            endpoint("/axelix-caches/{cacheManagerName}/{cacheName}", HttpMethod.GET);
    public static final ActuatorEndpoint ENABLE_CACHE =
            endpoint("/axelix-caches/{cacheManagerName}/{cacheName}/enable", HttpMethod.POST);
    public static final ActuatorEndpoint DISABLE_CACHE =
            endpoint("/axelix-caches/{cacheManagerName}/{cacheName}/disable", HttpMethod.POST);
    public static final ActuatorEndpoint ENABLE_CACHE_MANAGER =
            endpoint("/axelix-caches/{cacheManagerName}/enable", HttpMethod.POST);
    public static final ActuatorEndpoint DISABLE_CACHES_MANAGER =
            endpoint("/axelix-caches/{cacheManagerName}/disable", HttpMethod.POST);

    // Conditions
    public static final ActuatorEndpoint GET_CONDITIONS = endpoint("/axelix-conditions", HttpMethod.GET);

    // @ConfigurationProperties beans
    public static final ActuatorEndpoint GET_CONFIG_PROPS = endpoint("/axelix-configprops", HttpMethod.GET);

    // Environment
    public static final ActuatorEndpoint GET_ALL_ENV_PROPERTIES = endpoint("/axelix-env", HttpMethod.GET);

    // Heap Dump
    public static final ActuatorEndpoint GET_HEAP_DUMP = endpoint("/axelix-heap-dump", HttpMethod.GET);

    // Details
    public static final ActuatorEndpoint GET_DETAILS = endpoint("/axelix-details", HttpMethod.GET);

    // Gc Log File
    public static final ActuatorEndpoint GET_STATUS_GC_LOGGING = endpoint("/axelix-gc/log/status", HttpMethod.GET);
    public static final ActuatorEndpoint GET_GC_LOG_FILE = endpoint("/axelix-gc/log/file", HttpMethod.GET);
    public static final ActuatorEndpoint GC_TRIGGER = endpoint("/axelix-gc/trigger", HttpMethod.POST);
    public static final ActuatorEndpoint ENABLE_GC_LOGGING = endpoint("/axelix-gc/log/enable", HttpMethod.POST);
    public static final ActuatorEndpoint DISABLE_GC_LOGGING = endpoint("/axelix-gc/log/disable", HttpMethod.POST);

    // Loggers
    public static final ActuatorEndpoint GET_ALL_LOGGERS = endpoint("/axelix-loggers", HttpMethod.GET);
    public static final ActuatorEndpoint GET_ONE_LOGGER = endpoint("/axelix-loggers/logger/{name}", HttpMethod.GET);
    public static final ActuatorEndpoint GET_LOGGER_GROUP = endpoint("/axelix-loggers/group/{name}", HttpMethod.GET);
    public static final ActuatorEndpoint SET_ONE_LOGGER =
            endpoint("/axelix-loggers/logger/{name}/change-level", HttpMethod.POST);
    public static final ActuatorEndpoint SET_FOR_LOGGER_GROUP =
            endpoint("/axelix-loggers/group/{name}/change-level", HttpMethod.POST);
    public static final ActuatorEndpoint RESET_FOR_LOGGER =
            endpoint("/axelix-loggers/logger/{name}/reset", HttpMethod.POST);

    // Metadata
    public static final ActuatorEndpoint METADATA = endpoint("/axelix-metadata", HttpMethod.GET);

    // Metric
    public static final ActuatorEndpoint GET_METRIC_GROUPS = endpoint("/axelix-metrics", HttpMethod.GET);
    public static final ActuatorEndpoint GET_SINGLE_METRIC = endpoint("/axelix-metrics/{metric.name}", HttpMethod.GET);

    // @Scheduled tasks
    public static final ActuatorEndpoint GET_SCHEDULED_TASKS = endpoint("/axelix-scheduled-tasks", HttpMethod.GET);
    public static final ActuatorEndpoint MODIFY_CRON_EXPRESSION_SCHEDULED_TASK =
            endpoint("/axelix-scheduled-tasks/modify/cron-expression", HttpMethod.POST);
    public static final ActuatorEndpoint MODIFY_INTERVAL_SCHEDULED_TASK =
            endpoint("/axelix-scheduled-tasks/modify/interval", HttpMethod.POST);
    public static final ActuatorEndpoint ENABLE_SCHEDULED_TASK =
            endpoint("/axelix-scheduled-tasks/enable", HttpMethod.POST);
    public static final ActuatorEndpoint DISABLE_SCHEDULED_TASK =
            endpoint("/axelix-scheduled-tasks/disable", HttpMethod.POST);
    public static final ActuatorEndpoint EXECUTE_SCHEDULED_TASK =
            endpoint("/axelix-scheduled-tasks/execute", HttpMethod.POST);

    // Thread Dump
    public static final ActuatorEndpoint GET_THREAD_DUMP = endpoint("/axelix-thread-dump", HttpMethod.GET);
    public static final ActuatorEndpoint THREAD_DUMP_ENABLE_CONTENTION_MONITORING =
            endpoint("/axelix-thread-dump/enable", HttpMethod.POST);
    public static final ActuatorEndpoint THREAD_DUMP_DISABLE_CONTENTION_MONITORING =
            endpoint("/axelix-thread-dump/disable", HttpMethod.POST);

    // Feign Client
    public static final ActuatorEndpoint GET_FEIGN_CLIENT = endpoint("/axelix-feign", HttpMethod.GET);

    public static ActuatorEndpoint endpoint(String path, HttpMethod method) {
        var endpoint = ActuatorEndpoint.of(path, method);
        ENDPOINTS.add(endpoint);
        return endpoint;
    }

    public static ActuatorEndpoints getInstance() {
        return INSTANCE;
    }

    private ActuatorEndpoints() {}

    @Override
    public Iterator<ActuatorEndpoint> iterator() {
        return ENDPOINTS.iterator();
    }
}
