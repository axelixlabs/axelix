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

    public static final class UsersApi {

        public static final String MAIN = "/users";

        public static final String LOGIN = "/login";

        public static final String LOGOUT = "/logout";
    }

    public static final class InstancesApi {

        public static final String MAIN = "/applications";

        public static final String GRID = "/grid";
    }

    public static final class BeansApi {

        /**
         * Base path for all beans APIs.
         */
        public static final String MAIN = "/beans";

        /**
         * The Beans Feed used in the single instance
         */
        public static final String FEED = "/feed/{instanceId}";
    }

    public static final class EnvironmentApi {

        /**
         * Base path for all environment APIs.
         */
        public static final String MAIN = "/env";

        /**
         * Environment feed for a single instance, providing all environment properties.
         */
        public static final String FEED = "/feed/{instanceId}";
    }

    public static final class StateExportApi {

        /**
         * Base path for state export API.
         */
        public static final String MAIN = "/export-state";

        /**
         * Endpoint to export the state of the given application instance.
         */
        public static final String INSTANCE_ID = "/{instanceId}";
    }

    public static final class SoftwareApi {

        /**
         * Base path for all software APIs.
         */
        public static final String MAIN = "/software";

        /**
         * Core software components used in the entire deployment.
         */
        public static final String CORE_SUMMARY = "/core/summary";

        /**
         * Core software components used in the application.
         */
        public static final String CORE = "/core/{instanceId}";
    }

    public static final class InfoApi {

        /**
         * Base path for info APIs.
         */
        public static final String MAIN = "/info";
        /**
         * Info endpoint with instance ID
         */
        public static final String INSTANCE_ID = "/{instanceId}";
    }

    public static final class ConditionsApi {

        /**
         * Base path for conditions APIs.
         */
        public static final String MAIN = "/conditions";

        /**
         * Instance id for conditions Endpoint.
         */
        public static final String FEED = "/feed/{instanceId}";
    }

    public static final class ConfigPropsApi {

        /**
         * Base path for configprops APIs.
         */
        public static final String MAIN = "/configprops";
        /**
         * The Configprops Feed used in the single instance
         */
        public static final String FEED = "/feed/{instanceId}";
    }

    public static final class DashboardApi {

        /**
         * Base path for dashboard APIs.
         */
        public static final String MAIN = "/dashboard";
    }

    public static final class LoggersApi {

        /**
         * Base path for loggers APIs.
         */
        public static final String MAIN = "/loggers";
        /**
         * Loggers endpoint with instance ID.
         */
        public static final String INSTANCE_ID = "/{instanceId}";
        /**
         * Endpoint to retrieve a specific logger by name from an instance.
         */
        public static final String LOGGER_NAME = "/{instanceId}/logger/{loggerName}";
        /**
         * Endpoint to retrieve a specific logger group by name from an instance.
         */
        public static final String GROUP_NAME = "/{instanceId}/group/{groupName}";
        /**
         * Endpoint to reset the logging level of a logger by its name from an instance.
         */
        public static final String RESET_FOR_LOGGER = "/{instanceId}/logger/{loggerName}/reset";
    }

    public static final class GcLogFileApi {

        /**
         * Base path for GcLogFileApi.
         */
        public static final String MAIN = "/garbage-collector";

        /**
         * GcLogfile endpoint with instance ID.
         */
        public static final String INSTANCE_ID = "/logs/{instanceId}/file";

        /**
         * GC logging status endpoint with instance ID.
         */
        public static final String STATUS_GC_LOGGING = "/logs/{instanceId}/status";

        /**
         * Manual GC trigger endpoint with instance ID.
         */
        public static final String TRIGGER_GC = "/{instanceId}/trigger";

        /**
         * Enable GC logging endpoint with instance ID.
         */
        public static final String ENABLE_GC_LOGGING = "/logs/{instanceId}/enable";

        /**
         * Disable GC logging endpoint with instance ID.
         */
        public static final String DISABLE_GC_LOGGING = "/logs/{instanceId}/disable";
    }

    public static final class HeapDumpApi {

        /**
         * Base path for heap-dump API.
         */
        public static final String MAIN = "/heapdump";

        /**
         * Heap-dump endpoint with instance ID.
         */
        public static final String INSTANCE_ID = "/{instanceId}";
    }

    public static final class ProfileManagementApi {

        /**
         * Base path for profile management APIs.
         */
        public static final String MAIN = "/profile-management";

        /**
         * Endpoint to replace the active Spring profiles of a given application instance.
         */
        public static final String INSTANCE_ID = "/{instanceId}";
    }

    public static final class PropertyManagementApi {

        /**
         * Base path for property management APIs.
         */
        public static final String MAIN = "/property-management";

        /**
         * Endpoint to update property of a given application instance.
         */
        public static final String INSTANCE_ID = "/{instanceId}";
    }

    public static final class CachesApi {

        /**
         * Base path for caches APIs.
         */
        public static final String MAIN = "/caches";
        /**
         * Caches endpoint with instance ID.
         */
        public static final String INSTANCE_ID = "/{instanceId}";
        /**
         * Endpoint to retrieve a specific cache by name from an instance.
         */
        public static final String CACHE_NAME = "/{instanceId}/cache/{cacheName}";

        /**
         * Endpoint to enable a specific cache in a cache manager.
         */
        public static final String ENABLE_CACHE = "/{instanceId}/{cacheManagerName}/{cacheName}/enable";

        /**
         * Endpoint to disable a specific cache in a cache manager.
         */
        public static final String DISABLE_CACHE = "/{instanceId}/{cacheManagerName}/{cacheName}/disable";

        /**
         * Endpoint to enable all caches in a cache manager.
         */
        public static final String ENABLE_CACHE_MANAGER = "/{instanceId}/{cacheManagerName}/enable";

        /**
         * Endpoint to disable all caches in a cache manager.
         */
        public static final String DISABLE_CACHE_MANAGER = "/{instanceId}/{cacheManagerName}/disable";
    }

    public static final class MetricsApi {

        /**
         * Base path for metrics APIs.
         */
        public static final String MAIN = "/metrics";

        /**
         * Retrieve metrics within a given {@link Instance}.
         */
        public static final String INSTANCE_ID = "/{instanceId}";

        /**
         * Retrieve a given metric within a given {@link Instance}.
         */
        public static final String METRIC_NAME = "/{instanceId}/{metric}";
    }

    public static final class ScheduledTasksApi {

        /**
         * Base path for {@link com.axelixlabs.axelix.master.api.external.endpoint.ScheduledTasksApi} APIs.
         *
         */
        public static final String MAIN = "/scheduled-tasks";
        /**
         * ScheduledTasks endpoint with instance ID.
         */
        public static final String INSTANCE_ID = "/{instanceId}";
        /**
         * Endpoint allows enabling a scheduled task.
         */
        public static final String ENABLE_TASK = "/{instanceId}/enable";

        /**
         * Endpoint allows disabling a scheduled task.
         */
        public static final String DISABLE_TASK = "/{instanceId}/disable";
        /**
         * Endpoint allows modification of the cron expression for a scheduled task.
         */
        public static final String MODIFY_CRON_EXPRESSION = "/{instanceId}/modify/cron-expression";
        /**
         * Endpoint validates cron expression syntax.
         */
        public static final String VALIDATE_CRON_EXPRESSION = "/validate-cron-expression";
        /**
         * Endpoint allows modification of the interval for a scheduled task.
         */
        public static final String MODIFY_INTERVAL = "/{instanceId}/modify/interval";
        /**
         * Endpoint allows forcing a scheduled task to execute.
         */
        public static final String EXECUTE = "/{instanceId}/execute";
    }

    public static final class DetailsApi {

        /**
         * Base path for details APIs.
         */
        public static final String MAIN = "/details";
        /**
         * Details endpoint with instance ID.
         */
        public static final String INSTANCE_ID = "/{instanceId}";
    }

    public static final class TransactionMonitoringApi {

        public static final String MAIN = "/transaction-monitoring";

        public static final String INSTANCE_ID = "/{instanceId}";
    }

    public static final class ThreadDumpApi {

        /**
         * Base path for thread dump APIs.
         */
        public static final String MAIN = "/thread-dump";
        /**
         * Thread dump endpoint with instance ID.
         */
        public static final String INSTANCE_ID = "/{instanceId}";

        /**
         * Endpoint allows enabling thread contention monitoring.
         */
        public static final String ENABLE_CONTENTION_MONITORING = "/{instanceId}/thread-contention-monitoring/enable";

        /**
         * Endpoint allows disabling thread contention monitoring.
         */
        public static final String DISABLE_CONTENTION_MONITORING = "/{instanceId}/thread-contention-monitoring/disable";
    }

    public static final class FeignClientApi {

        /**
         * Base path for feign client APIs.
         */
        public static final String MAIN = "/feign";
        /**
         * Feign Client endpoint with instance ID.
         */
        public static final String INSTANCE_ID = "/{instanceId}";
    }
}
