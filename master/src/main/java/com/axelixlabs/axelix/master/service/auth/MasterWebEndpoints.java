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
package com.axelixlabs.axelix.master.service.auth;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.master.api.external.ApiPaths;

/**
 * The catalog of every {@link MasterWebEndpoint Axelix Master web API endpoint} exposed to the UI. This is the
 * master-side counterpart of {@link com.axelixlabs.axelix.common.domain.ActuatorEndpoints} and the single source
 * of truth for the {@code (path, method) -> (operation code, required authority)} mapping.
 * <p>
 * Only a subset of endpoints currently carries a required {@link Authority}; the remaining endpoints (mostly reads)
 * declare a {@code null} authority, which preserves today's authorization behaviour. Every endpoint nevertheless
 * carries a stable {@code operationCode} so that operations never have to be deduced from the raw request.
 *
 * @author Mikhail Polivakha
 */
public final class MasterWebEndpoints {

    private static final Set<MasterWebEndpoint> ENDPOINTS = new LinkedHashSet<>();

    private MasterWebEndpoints() {}

    // spotless:off

    // Beans
    public static final MasterWebEndpoint BEANS_READ =
            register("beans:read", HttpMethod.GET, ApiPaths.BeansApi.FEED, null);

    // Environment
    public static final MasterWebEndpoint ENVIRONMENT_READ =
            register("environment:read", HttpMethod.GET, ApiPaths.EnvironmentApi.FEED, null);

    // @ConfigurationProperties
    public static final MasterWebEndpoint CONFIG_PROPS_READ =
            register("config-props:read", HttpMethod.GET, ApiPaths.ConfigPropsApi.FEED, null);

    // Conditions
    public static final MasterWebEndpoint CONDITIONS_READ =
            register("conditions:read", HttpMethod.GET, ApiPaths.ConditionsApi.FEED, null);

    // Details
    public static final MasterWebEndpoint DETAILS_READ =
            register("details:read", HttpMethod.GET, ApiPaths.DetailsApi.INSTANCE_ID, null);

    // Instances
    public static final MasterWebEndpoint INSTANCES_READ =
            register("instances:read", HttpMethod.GET, ApiPaths.InstancesApi.GRID, null);

    // Metrics
    public static final MasterWebEndpoint METRICS_READ =
            register("metrics:read", HttpMethod.GET, ApiPaths.MetricsApi.INSTANCE_ID, null);
    public static final MasterWebEndpoint METRIC_READ_ONE =
            register("metrics:read-one", HttpMethod.GET, ApiPaths.MetricsApi.METRIC_NAME, null);

    // Loggers
    public static final MasterWebEndpoint LOGGERS_READ =
            register("loggers:read", HttpMethod.GET, ApiPaths.LoggersApi.INSTANCE_ID, null);
    public static final MasterWebEndpoint LOGGER_READ_ONE =
            register("logger:read-one", HttpMethod.GET, ApiPaths.LoggersApi.LOGGER_NAME, null);
    public static final MasterWebEndpoint LOGGER_GROUP_READ =
            register("logger-group:read", HttpMethod.GET, ApiPaths.LoggersApi.GROUP_NAME, null);
    public static final MasterWebEndpoint LOGGERS_BULK_CHANGE =
            register("loggers:change-level", HttpMethod.POST, ApiPaths.LoggersApi.LOGGER_BULK_CHANGE, null);
    public static final MasterWebEndpoint LOGGER_GROUP_CHANGE =
            register("logger-group:change-level", HttpMethod.POST, ApiPaths.LoggersApi.GROUP_NAME, null);
    public static final MasterWebEndpoint LOGGER_RESET =
            register("logger:reset", HttpMethod.POST, ApiPaths.LoggersApi.RESET_FOR_LOGGER, null);

    // Caches
    public static final MasterWebEndpoint CACHES_READ =
            register("caches:read", HttpMethod.GET, ApiPaths.CachesApi.INSTANCE_ID, null);
    public static final MasterWebEndpoint CACHE_READ_ONE =
            register("cache:read-one", HttpMethod.GET, ApiPaths.CachesApi.CACHE_NAME, null);
    public static final MasterWebEndpoint CACHE_ENABLE =
            register("caches:enable", HttpMethod.POST, ApiPaths.CachesApi.ENABLE_CACHE, DefaultAuthority.CACHES_TOGGLE);
    public static final MasterWebEndpoint CACHE_DISABLE = register(
            "caches:disable", HttpMethod.POST, ApiPaths.CachesApi.DISABLE_CACHE, DefaultAuthority.CACHES_TOGGLE);
    public static final MasterWebEndpoint CACHE_MANAGER_ENABLE = register(
            "cache-manager:enable",
            HttpMethod.POST,
            ApiPaths.CachesApi.ENABLE_CACHE_MANAGER,
            DefaultAuthority.CACHES_TOGGLE);
    public static final MasterWebEndpoint CACHE_MANAGER_DISABLE = register(
            "cache-manager:disable",
            HttpMethod.POST,
            ApiPaths.CachesApi.DISABLE_CACHE_MANAGER,
            DefaultAuthority.CACHES_TOGGLE);
    public static final MasterWebEndpoint CACHE_CLEAR_ONE = register(
            "caches:clear-one", HttpMethod.DELETE, ApiPaths.CachesApi.CACHE_NAME, DefaultAuthority.CACHES_CLEAR);
    public static final MasterWebEndpoint CACHES_CLEAR_ALL = register(
            "caches:clear-all", HttpMethod.DELETE, ApiPaths.CachesApi.INSTANCE_ID, DefaultAuthority.CACHES_CLEAR);

    // Scheduled tasks
    public static final MasterWebEndpoint SCHEDULED_TASKS_READ =
            register("scheduled-tasks:read", HttpMethod.GET, ApiPaths.ScheduledTasksApi.INSTANCE_ID, null);
    public static final MasterWebEndpoint SCHEDULED_TASK_ENABLE = register(
            "scheduled-task:enable",
            HttpMethod.POST,
            ApiPaths.ScheduledTasksApi.ENABLE_TASK,
            DefaultAuthority.SCHEDULED_TASKS_MODIFY);
    public static final MasterWebEndpoint SCHEDULED_TASK_DISABLE = register(
            "scheduled-task:disable",
            HttpMethod.POST,
            ApiPaths.ScheduledTasksApi.DISABLE_TASK,
            DefaultAuthority.SCHEDULED_TASKS_MODIFY);
    public static final MasterWebEndpoint SCHEDULED_TASK_EXECUTE = register(
            "scheduled-task:execute",
            HttpMethod.POST,
            ApiPaths.ScheduledTasksApi.EXECUTE,
            DefaultAuthority.SCHEDULED_TASKS_MODIFY);
    public static final MasterWebEndpoint SCHEDULED_TASK_MODIFY_CRON = register(
            "scheduled-task:modify-cron-expression",
            HttpMethod.POST,
            ApiPaths.ScheduledTasksApi.MODIFY_CRON_EXPRESSION,
            DefaultAuthority.SCHEDULED_TASKS_MODIFY);
    public static final MasterWebEndpoint SCHEDULED_TASK_MODIFY_INTERVAL = register(
            "scheduled-task:modify-interval",
            HttpMethod.POST,
            ApiPaths.ScheduledTasksApi.MODIFY_INTERVAL,
            DefaultAuthority.SCHEDULED_TASKS_MODIFY);
    public static final MasterWebEndpoint SCHEDULED_TASK_VALIDATE_CRON = register(
            "scheduled-task:validate-cron-expression",
            HttpMethod.POST,
            ApiPaths.ScheduledTasksApi.VALIDATE_CRON_EXPRESSION,
            null);

    // Garbage collector
    public static final MasterWebEndpoint GC_LOG_READ_FILE =
            register("gc-log:read-file", HttpMethod.GET, ApiPaths.GcLogFileApi.INSTANCE_ID, null);
    public static final MasterWebEndpoint GC_LOG_READ_STATUS =
            register("gc-log:read-status", HttpMethod.GET, ApiPaths.GcLogFileApi.STATUS_GC_LOGGING, null);
    public static final MasterWebEndpoint GC_TRIGGER = register(
            "gc:trigger", HttpMethod.POST, ApiPaths.GcLogFileApi.TRIGGER_GC, DefaultAuthority.GARBAGE_COLLECTOR);
    public static final MasterWebEndpoint GC_LOG_ENABLE = register(
            "gc-log:enable",
            HttpMethod.POST,
            ApiPaths.GcLogFileApi.ENABLE_GC_LOGGING,
            DefaultAuthority.GARBAGE_COLLECTOR);
    public static final MasterWebEndpoint GC_LOG_DISABLE = register(
            "gc-log:disable",
            HttpMethod.POST,
            ApiPaths.GcLogFileApi.DISABLE_GC_LOGGING,
            DefaultAuthority.GARBAGE_COLLECTOR);

    // Thread dump
    public static final MasterWebEndpoint THREAD_DUMP_READ =
            register("thread-dump:read", HttpMethod.GET, ApiPaths.ThreadDumpApi.INSTANCE_ID, null);
    public static final MasterWebEndpoint THREAD_DUMP_ENABLE_CONTENTION = register(
            "thread-dump:enable-contention-monitoring",
            HttpMethod.POST,
            ApiPaths.ThreadDumpApi.ENABLE_CONTENTION_MONITORING,
            null);
    public static final MasterWebEndpoint THREAD_DUMP_DISABLE_CONTENTION = register(
            "thread-dump:disable-contention-monitoring",
            HttpMethod.POST,
            ApiPaths.ThreadDumpApi.DISABLE_CONTENTION_MONITORING,
            null);

    // Heap dump
    public static final MasterWebEndpoint HEAP_DUMP_READ =
            register("heap-dump:read", HttpMethod.GET, ApiPaths.HeapDumpApi.INSTANCE_ID, null);

    // Feign clients
    public static final MasterWebEndpoint FEIGN_READ =
            register("feign:read", HttpMethod.GET, ApiPaths.FeignClientApi.INSTANCE_ID, null);

    // Transaction monitoring
    public static final MasterWebEndpoint TRANSACTION_MONITORING_READ = register(
            "transaction-monitoring:read", HttpMethod.GET, ApiPaths.TransactionMonitoringApi.INSTANCE_ID, null);

    // State export
    public static final MasterWebEndpoint STATE_EXPORT =
            register("state:export", HttpMethod.POST, ApiPaths.StateExportApi.INSTANCE_ID, null);

    // Dashboard
    public static final MasterWebEndpoint DASHBOARD_READ =
            register("dashboard:read", HttpMethod.GET, ApiPaths.DashboardApi.MAIN, null);
    public static final MasterWebEndpoint DASHBOARD_READ_JAVA =
            register("dashboard:read-java", HttpMethod.GET, ApiPaths.DashboardApi.JAVA, null);
    public static final MasterWebEndpoint DASHBOARD_READ_SPRING_FRAMEWORK =
            register("dashboard:read-spring-framework", HttpMethod.GET, ApiPaths.DashboardApi.SPRING_FRAMEWORK, null);
    public static final MasterWebEndpoint DASHBOARD_READ_PERSISTENCE =
            register("dashboard:read-persistence", HttpMethod.GET, ApiPaths.DashboardApi.PERSISTENCE, null);

    // MCP tools feed
    public static final MasterWebEndpoint MCP_TOOLS_READ =
            register("mcp-tools:read", HttpMethod.GET, ApiPaths.McpToolApi.TOOLS_LIST, null);

    // Users
    public static final MasterWebEndpoint USERS_READ =
            register("users:read", HttpMethod.GET, ApiPaths.UsersApi.USERS_FEED, DefaultAuthority.USERS_VIEW);
    public static final MasterWebEndpoint USER_READ_ONE =
            register("users:read-one", HttpMethod.GET, ApiPaths.UsersApi.GET_USER_BY_ID, null);
    public static final MasterWebEndpoint AUTH_LOGIN =
            register("auth:login", HttpMethod.POST, ApiPaths.UsersApi.LOGIN, null);
    public static final MasterWebEndpoint AUTH_LOGOUT =
            register("auth:logout", HttpMethod.POST, ApiPaths.UsersApi.LOGOUT, null);

    // User management
    public static final MasterWebEndpoint USER_CREATE = register(
            "users:create",
            HttpMethod.POST,
            ApiPaths.UsersManagementApi.USERS_CREATE,
            DefaultAuthority.USERS_MANAGEMENT);
    public static final MasterWebEndpoint USER_DELETE = register(
            "users:delete",
            HttpMethod.DELETE,
            ApiPaths.UsersManagementApi.USERS_DELETE,
            DefaultAuthority.USERS_MANAGEMENT);
    public static final MasterWebEndpoint USER_UPDATE = register(
            "users:update",
            HttpMethod.PUT,
            ApiPaths.UsersManagementApi.USERS_UPDATE,
            DefaultAuthority.USERS_MANAGEMENT);
    public static final MasterWebEndpoint USER_CHANGE_STATUS = register(
            "users:change-status",
            HttpMethod.PUT,
            ApiPaths.UsersManagementApi.USERS_STATUS,
            DefaultAuthority.USERS_MANAGEMENT);

    // spotless:on

    private static MasterWebEndpoint register(
            String operationCode, HttpMethod method, String path, @Nullable Authority authority) {
        MasterWebEndpoint endpoint = MasterWebEndpoint.of(operationCode, method, path, authority);
        ENDPOINTS.add(endpoint);
        return endpoint;
    }

    /**
     * @return an immutable view of every registered master web endpoint.
     */
    public static Set<MasterWebEndpoint> oss() {
        return Collections.unmodifiableSet(ENDPOINTS);
    }
}
