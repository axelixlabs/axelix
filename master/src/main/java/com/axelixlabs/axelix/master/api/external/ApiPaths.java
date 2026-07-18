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
package com.axelixlabs.axelix.master.api.external;

import com.axelixlabs.axelix.master.domain.Instance;

/**
 * Constant class that holds the paths to the APIs that Axelix Master exposes
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
public final class ApiPaths {

    private ApiPaths() {}

    public static final class SettingsApi {

        public static final String SETTINGS = "/settings";
    }

    public static final class UsersApi {

        public static final String LOGIN = "/users/login";

        public static final String LOGOUT = "/users/logout";

        /**
         * Endpoint to retrieve the feed of managed users.
         */
        public static final String USERS_FEED = "/users/feed";

        /**
         * Endpoint to retrieve the feed of managed users.
         */
        public static final String GET_USER_BY_ID = "/users/feed/{userId}";
    }

    public static final class UsersManagementApi {

        /**
         * Endpoint to create a new managed user.
         */
        public static final String USERS_CREATE = "/users-management/create";

        /**
         * Endpoint to delete a managed user.
         */
        public static final String USERS_DELETE = "/users-management/delete";

        /**
         * Endpoint to update a managed user.
         */
        public static final String USERS_UPDATE = "/users-management/update";
    }

    public static final class OAuth2Api {

        public static final String CALLBACK = "/oauth2/callback";
    }

    public static final class InstancesApi {

        public static final String GRID = "/applications/grid";
    }

    public static final class BeansApi {

        /**
         * The Beans Feed used in the single instance
         */
        public static final String FEED = "/beans/feed/{instanceId}";
    }

    public static final class EnvironmentApi {

        /**
         * Environment feed for a single instance, providing all environment properties.
         */
        public static final String FEED = "/env/feed/{instanceId}";
    }

    public static final class StateExportApi {

        /**
         * Endpoint to export the state of the given application instance.
         */
        public static final String INSTANCE_ID = "/export-state/{instanceId}";
    }

    public static final class ConditionsApi {

        /**
         * Instance id for conditions Endpoint.
         */
        public static final String FEED = "/conditions/feed/{instanceId}";
    }

    public static final class ConfigPropsApi {

        /**
         * The Config-props Feed used in the single instance
         */
        public static final String FEED = "/configprops/feed/{instanceId}";
    }

    public static final class DashboardApi {

        /**
         * Base path for dashboard APIs.
         */
        public static final String MAIN = "/dashboard";

        /**
         * Endpoint to retrieve the aggregated Java/JVM features adoption across the ecosystem.
         */
        public static final String JAVA = "/dashboard/java";

        /**
         * Endpoint to retrieve the aggregated Spring Framework features adoption across the ecosystem.
         */
        public static final String SPRING_FRAMEWORK = "/dashboard/spring-framework";

        /**
         * Endpoint to retrieve the aggregated persistence problems (N + 1, in-memory pagination) across the ecosystem.
         */
        public static final String PERSISTENCE = "/dashboard/persistence";
    }

    public static final class LoggersApi {

        /**
         * Loggers endpoint with instance ID.
         */
        public static final String INSTANCE_ID = "/loggers/{instanceId}";

        /**
         * Endpoint to retrieve a specific logger by name from an instance.
         */
        public static final String LOGGER_NAME = "/loggers/{instanceId}/logger/{loggerName}";

        /**
         * Endpoint to change a specific logger by name across instances.
         */
        public static final String LOGGER_BULK_CHANGE = "/loggers/logger";

        /**
         * Endpoint to retrieve a specific logger group by name from an instance.
         */
        public static final String GROUP_NAME = "/loggers/{instanceId}/group/{groupName}";

        /**
         * Endpoint to reset the logging level of a logger by its name from an instance.
         */
        public static final String RESET_FOR_LOGGER = "/loggers/{instanceId}/logger/{loggerName}/reset";
    }

    public static final class GcLogFileApi {

        /**
         * GcLogfile endpoint with instance ID.
         */
        public static final String INSTANCE_ID = "/garbage-collector/logs/{instanceId}/file";

        /**
         * GC logging status endpoint with instance ID.
         */
        public static final String STATUS_GC_LOGGING = "/garbage-collector/logs/{instanceId}/status";

        /**
         * Manual GC trigger endpoint with instance ID.
         */
        public static final String TRIGGER_GC = "/garbage-collector/{instanceId}/trigger";

        /**
         * Enable GC logging endpoint with instance ID.
         */
        public static final String ENABLE_GC_LOGGING = "/garbage-collector/logs/{instanceId}/enable";

        /**
         * Disable GC logging endpoint with instance ID.
         */
        public static final String DISABLE_GC_LOGGING = "/garbage-collector/logs/{instanceId}/disable";
    }

    public static final class HeapDumpApi {

        /**
         * Heap-dump endpoint with instance ID.
         */
        public static final String INSTANCE_ID = "/heapdump/{instanceId}";
    }

    public static final class PropertyManagementApi {

        /**
         * Endpoint to update property of a given application instance.
         */
        public static final String INSTANCE_ID = "/property-management/{instanceId}";
    }

    public static final class CachesApi {

        /**
         * Caches endpoint with instance ID.
         */
        public static final String INSTANCE_ID = "/caches/{instanceId}";

        /**
         * Endpoint to retrieve a specific cache by name from an instance.
         */
        public static final String CACHE_NAME = "/caches/{instanceId}/cache/{cacheName}";

        /**
         * Endpoint to enable a specific cache in a cache manager.
         */
        public static final String ENABLE_CACHE = "/caches/{instanceId}/{cacheManagerName}/{cacheName}/enable";

        /**
         * Endpoint to disable a specific cache in a cache manager.
         */
        public static final String DISABLE_CACHE = "/caches/{instanceId}/{cacheManagerName}/{cacheName}/disable";

        /**
         * Endpoint to enable all caches in a cache manager.
         */
        public static final String ENABLE_CACHE_MANAGER = "/caches/{instanceId}/{cacheManagerName}/enable";

        /**
         * Endpoint to disable all caches in a cache manager.
         */
        public static final String DISABLE_CACHE_MANAGER = "/caches/{instanceId}/{cacheManagerName}/disable";
    }

    public static final class MetricsApi {

        /**
         * Retrieve metrics within a given {@link Instance}.
         */
        public static final String INSTANCE_ID = "/metrics/{instanceId}";

        /**
         * Retrieve a given metric within a given {@link Instance}.
         */
        public static final String METRIC_NAME = "/metrics/{instanceId}/{metric}";
    }

    public static final class ScheduledTasksApi {

        /**
         * ScheduledTasks endpoint with instance ID.
         */
        public static final String INSTANCE_ID = "/scheduled-tasks/{instanceId}";

        /**
         * Endpoint allows enabling a scheduled task.
         */
        public static final String ENABLE_TASK = "/scheduled-tasks/{instanceId}/enable";

        /**
         * Endpoint allows disabling a scheduled task.
         */
        public static final String DISABLE_TASK = "/scheduled-tasks/{instanceId}/disable";
        /**
         * Endpoint allows modification of the cron expression for a scheduled task.
         */
        public static final String MODIFY_CRON_EXPRESSION = "/scheduled-tasks/{instanceId}/modify/cron-expression";
        /**
         * Endpoint validates cron expression syntax.
         */
        public static final String VALIDATE_CRON_EXPRESSION = "/scheduled-tasks/validate-cron-expression";
        /**
         * Endpoint allows modification of the interval for a scheduled task.
         */
        public static final String MODIFY_INTERVAL = "/scheduled-tasks/{instanceId}/modify/interval";
        /**
         * Endpoint allows forcing a scheduled task to execute.
         */
        public static final String EXECUTE = "/scheduled-tasks/{instanceId}/execute";
    }

    public static final class DetailsApi {

        /**
         * Details endpoint with instance ID.
         */
        public static final String INSTANCE_ID = "/details/{instanceId}";
    }

    public static final class TransactionMonitoringApi {

        public static final String INSTANCE_ID = "/transaction-monitoring/{instanceId}";
    }

    public static final class ThreadDumpApi {

        /**
         * Thread dump endpoint with instance ID.
         */
        public static final String INSTANCE_ID = "/thread-dump/{instanceId}";

        /**
         * Endpoint allows enabling thread contention monitoring.
         */
        public static final String ENABLE_CONTENTION_MONITORING =
                "/thread-dump/{instanceId}/thread-contention-monitoring/enable";

        /**
         * Endpoint allows disabling thread contention monitoring.
         */
        public static final String DISABLE_CONTENTION_MONITORING =
                "/thread-dump/{instanceId}/thread-contention-monitoring/disable";
    }

    public static final class FeignClientApi {

        /**
         * Feign Client endpoint with instance ID.
         */
        public static final String INSTANCE_ID = "/feign/{instanceId}";
    }

    public static final class McpOAuth2Api {

        /**
         * OAuth2 Protected Resource Metadata endpoint.
         */
        public static final String PROTECTED_RESOURCE_METADATA = "/mcp-oauth2/.well-known/oauth-protected-resource";
    }

    public static final class McpToolApi {

        /**
         * The feed of MCP tools configured in the master service.
         */
        public static final String TOOLS_LIST = "/mcp/tools-feed";
    }
}
