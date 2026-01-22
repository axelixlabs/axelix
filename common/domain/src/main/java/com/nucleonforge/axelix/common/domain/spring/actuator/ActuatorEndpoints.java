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
package com.nucleonforge.axelix.common.domain.spring.actuator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.nucleonforge.axelix.common.domain.http.HttpMethod;

import static com.nucleonforge.axelix.common.domain.spring.actuator.ActuatorEndpoint.of;

/**
 * Represents all possible {@link ActuatorEndpoint actuator endpoints} that are being used in the system:
 * <ul>
 *     <li>Custom</li>
 *     <li>Spring Boot</li>
 *     <li>Spring Cloud Config</li>
 *     <li>Spring Cloud Gateway etc.</li>
 * </ul>
 *
 * Documentation: <br/>
 * <a href="https://docs.spring.io/spring-boot/reference/actuator/endpoints.html">Actuator Documentation</a><br/>
 * <a href="https://docs.spring.io/spring-boot/api/rest/actuator/env.html">All Spring Boot supported endpoints</a>
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
public class ActuatorEndpoints implements Iterable<ActuatorEndpoint> {

    private final Set<ActuatorEndpoint> endpoints;

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
    public static final ActuatorEndpoint GET_BEANS = of("/axelix-beans", HttpMethod.GET);

    // Caches
    public static final ActuatorEndpoint CLEAR_SINGLE_CACHE =
            of("/axelix-caches/{cacheManagerName}/{cacheName}", HttpMethod.DELETE);
    public static final ActuatorEndpoint CLEAR_ALL_CACHES = of("/axelix-caches", HttpMethod.DELETE);

    public static final ActuatorEndpoint GET_ALL_CACHES = of("/axelix-caches", HttpMethod.GET);
    public static final ActuatorEndpoint GET_SINGLE_CACHE =
            of("/axelix-caches/{cacheManagerName}/{cacheName}", HttpMethod.GET);
    public static final ActuatorEndpoint ENABLE_CACHE =
            of("/axelix-caches/{cacheManagerName}/{cacheName}/enable", HttpMethod.POST);
    public static final ActuatorEndpoint DISABLE_CACHE =
            of("/axelix-caches/{cacheManagerName}/{cacheName}/disable", HttpMethod.POST);
    public static final ActuatorEndpoint ENABLE_CACHE_MANAGER =
            of("/axelix-caches/{cacheManagerName}/enable", HttpMethod.POST);
    public static final ActuatorEndpoint DISABLE_CACHES_MANAGER =
            of("/axelix-caches/{cacheManagerName}/disable", HttpMethod.POST);

    // Conditions
    public static final ActuatorEndpoint CONDITIONS = of("/axelix-conditions", HttpMethod.GET);

    // @ConfigurationProperties beans
    public static final ActuatorEndpoint CONFIG_PROPS = of("/axelix-configprops", HttpMethod.GET);
    public static final ActuatorEndpoint CONFIG_PROPS_BY_PREFIX = of("/axelix-configprops/{prefix}", HttpMethod.GET);

    // Environment
    public static final ActuatorEndpoint GET_ALL_ENV_PROPERTIES = of("/axelix-env", HttpMethod.GET);
    public static final ActuatorEndpoint GET_SINGLE_ENV_PROPERTY = of("/env/{property.name}", HttpMethod.GET);

    // Flyway
    public static final ActuatorEndpoint FLYWAY = of("/flyway", HttpMethod.GET);

    // Health
    public static final ActuatorEndpoint HEALTH = of("/health", HttpMethod.GET);
    public static final ActuatorEndpoint HEALTH_COMPONENT = of("/health/{component}", HttpMethod.GET);
    public static final ActuatorEndpoint HEALTH_SUB_COMPONENT =
            of("/health/{component}/{sub-component}", HttpMethod.GET);

    // Heap Dump
    public static final ActuatorEndpoint GET_HEAP_DUMP = of("/heapdump", HttpMethod.GET);

    // Http Exchanges
    public static final ActuatorEndpoint HTTP_EXCHANGES = of("/httpexchanges", HttpMethod.GET);

    // General Info
    public static final ActuatorEndpoint INFO = of("/info", HttpMethod.GET);

    // Details
    public static final ActuatorEndpoint GET_DETAILS = of("/axelix-details", HttpMethod.GET);

    // Integration Graph (Spring Integrations project)
    public static final ActuatorEndpoint INTEGRATIONS_GRAPH = of("/integrationgraph", HttpMethod.GET);
    public static final ActuatorEndpoint REBUILD_INTEGRATIONS_GRAPH = of("/integrationgraph", HttpMethod.POST);

    // Liquibase
    public static final ActuatorEndpoint LIQUIBASE = of("/liquibase", HttpMethod.GET);

    // Log File
    public static final ActuatorEndpoint GET_LOG_FILE = of("/logfile", HttpMethod.GET);

    // Gc Log File
    public static final ActuatorEndpoint STATUS_GC_LOGGING = of("/axelix-gc/log/status", HttpMethod.GET);
    public static final ActuatorEndpoint GC_LOG_FILE = of("/axelix-gc/log/file", HttpMethod.GET);
    public static final ActuatorEndpoint GC_TRIGGER = of("/axelix-gc/trigger", HttpMethod.POST);
    public static final ActuatorEndpoint ENABLE_GC_LOGGING = of("/axelix-gc/log/enable", HttpMethod.POST);
    public static final ActuatorEndpoint DISABLE_GC_LOGGING = of("/axelix-gc/log/disable", HttpMethod.POST);

    // Loggers
    public static final ActuatorEndpoint GET_ALL_LOGGERS = of("/loggers", HttpMethod.GET);
    public static final ActuatorEndpoint GET_ONE_LOGGER = of("/loggers/{logger.name}", HttpMethod.GET);
    public static final ActuatorEndpoint GET_LOGGER_GROUP = of("/loggers/{group.name}", HttpMethod.GET);
    public static final ActuatorEndpoint SET_ONE_LOGGER = of("/loggers/{logger.name}", HttpMethod.POST);
    public static final ActuatorEndpoint SET_FOR_LOGGER_GROUP = of("/loggers/{group.name}", HttpMethod.POST);
    public static final ActuatorEndpoint CLEAR_FOR_LOGGER = of("/loggers/{logger.name}", HttpMethod.POST);

    // Mappings
    public static final ActuatorEndpoint MAPPINGS = of("/mappings", HttpMethod.GET);

    // Metadata
    public static final ActuatorEndpoint METADATA = of("/axelix-metadata", HttpMethod.GET);

    // Metric
    public static final ActuatorEndpoint GET_METRIC_GROUPS = of("/axelix-metrics", HttpMethod.GET);
    public static final ActuatorEndpoint GET_SINGLE_METRIC = of("/axelix-metrics/{metric.name}", HttpMethod.GET);

    // ProfileManagement
    public static final ActuatorEndpoint PROFILE_MANAGEMENT = of("/profile-management", HttpMethod.POST);

    // Prometheus
    public static final ActuatorEndpoint PROMETHEUS = of("/prometheus", HttpMethod.GET);

    // PropertyManagement
    public static final ActuatorEndpoint PROPERTY_MANAGEMENT = of("/property-management", HttpMethod.POST);

    // Quartz
    // TODO: there are more of them
    public static final ActuatorEndpoint QUARTZ = of("/quartz", HttpMethod.GET);
    public static final ActuatorEndpoint QUARTZ_JOBS = of("/quartz/jobs", HttpMethod.GET);
    public static final ActuatorEndpoint QUARTZ_SINGLE_JOB = of("/quartz/jobs/{job}", HttpMethod.GET);
    public static final ActuatorEndpoint QUARTZ_TRIGGERS = of("/quartz/triggers", HttpMethod.GET);

    // SBOM
    public static final ActuatorEndpoint ALL_SBOMS = of("/sbom", HttpMethod.GET);
    public static final ActuatorEndpoint SINGLE_SBOM = of("/sbom/{sbom-id}", HttpMethod.GET);

    // @Scheduled tasks
    public static final ActuatorEndpoint SCHEDULED_TASKS = of("/axelix-scheduled-tasks", HttpMethod.GET);
    public static final ActuatorEndpoint MODIFY_CRON_EXPRESSION_SCHEDULED_TASK =
            of("/axelix-scheduled-tasks/modify/cron-expression", HttpMethod.POST);
    public static final ActuatorEndpoint MODIFY_INTERVAL_SCHEDULED_TASK =
            of("/axelix-scheduled-tasks/modify/interval", HttpMethod.POST);
    public static final ActuatorEndpoint ENABLE_SCHEDULED_TASK = of("/axelix-scheduled-tasks/enable", HttpMethod.POST);
    public static final ActuatorEndpoint DISABLE_SCHEDULED_TASK =
            of("/axelix-scheduled-tasks/disable", HttpMethod.POST);
    public static final ActuatorEndpoint EXECUTE_SCHEDULED_TASK =
            of("/axelix-scheduled-tasks/execute", HttpMethod.POST);

    // Sessions
    public static final ActuatorEndpoint SESSION = of("/sessions", HttpMethod.GET);
    public static final ActuatorEndpoint SINGLE_SESSION = of("/sessions/{id}", HttpMethod.GET);
    public static final ActuatorEndpoint DELETE_SESSION = of("/sessions/{id}", HttpMethod.DELETE);

    // Shutdown
    public static final ActuatorEndpoint SHUTDOWN = of("/shutdown", HttpMethod.POST);

    // Startup
    public static final ActuatorEndpoint STARTUP = of("/startup", HttpMethod.POST);

    // Thread Dump
    public static final ActuatorEndpoint GET_THREAD_DUMP = of("/axelix-thread-dump", HttpMethod.GET);
    public static final ActuatorEndpoint THREAD_DUMP_ENABLE_CONTENTION_MONITORING =
            of("/axelix-thread-dump/enable", HttpMethod.POST);
    public static final ActuatorEndpoint THREAD_DUMP_DISABLE_CONTENTION_MONITORING =
            of("/axelix-thread-dump/disable", HttpMethod.POST);

    private static final ActuatorEndpoints INSTANCE = new ActuatorEndpoints();

    public static ActuatorEndpoints getInstance() {
        return INSTANCE;
    }

    // spotless:on

    private ActuatorEndpoints() {
        this.endpoints = new HashSet<>(List.of(
                GET_BEANS,
                GET_ALL_CACHES,
                GET_SINGLE_CACHE,
                CLEAR_ALL_CACHES,
                CLEAR_SINGLE_CACHE,
                ENABLE_CACHE,
                DISABLE_CACHE,
                ENABLE_CACHE_MANAGER,
                DISABLE_CACHES_MANAGER,
                CONDITIONS,
                CONFIG_PROPS,
                CONFIG_PROPS_BY_PREFIX,
                GET_ALL_ENV_PROPERTIES,
                GET_SINGLE_ENV_PROPERTY,
                FLYWAY,
                HEALTH,
                HEALTH_COMPONENT,
                HEALTH_SUB_COMPONENT,
                GET_HEAP_DUMP,
                HTTP_EXCHANGES,
                INFO,
                GET_DETAILS,
                INTEGRATIONS_GRAPH,
                REBUILD_INTEGRATIONS_GRAPH,
                LIQUIBASE,
                GET_LOG_FILE,
                GC_LOG_FILE,
                GET_ALL_LOGGERS,
                GET_ONE_LOGGER,
                GET_LOGGER_GROUP,
                SET_ONE_LOGGER,
                SET_FOR_LOGGER_GROUP,
                CLEAR_FOR_LOGGER,
                MAPPINGS,
                METADATA,
                GET_METRIC_GROUPS,
                GET_SINGLE_METRIC,
                PROFILE_MANAGEMENT,
                PROMETHEUS,
                PROPERTY_MANAGEMENT,
                QUARTZ,
                QUARTZ_JOBS,
                QUARTZ_SINGLE_JOB,
                QUARTZ_TRIGGERS,
                ALL_SBOMS,
                SINGLE_SBOM,
                SCHEDULED_TASKS,
                MODIFY_CRON_EXPRESSION_SCHEDULED_TASK,
                MODIFY_INTERVAL_SCHEDULED_TASK,
                ENABLE_SCHEDULED_TASK,
                DISABLE_SCHEDULED_TASK,
                EXECUTE_SCHEDULED_TASK,
                SESSION,
                SINGLE_SESSION,
                DELETE_SESSION,
                SHUTDOWN,
                STARTUP,
                GET_THREAD_DUMP,
                THREAD_DUMP_ENABLE_CONTENTION_MONITORING,
                THREAD_DUMP_DISABLE_CONTENTION_MONITORING));
    }

    @Override
    public Iterator<ActuatorEndpoint> iterator() {
        return endpoints.iterator();
    }
}
