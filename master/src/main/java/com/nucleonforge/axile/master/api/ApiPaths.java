package com.nucleonforge.axile.master.api;

/**
 * Constant class that holds the paths to the APIs that Axile Master exposes
 *
 * @author Mikhail Polivakha
 */
public final class ApiPaths {

    private ApiPaths() {}

    public static final class UsersApi {

        public static final String MAIN = "/users";

        public static final String LOGIN = "/login";
    }

    public static final class InstancesApi {

        public static final String MAIN = "/applications";

        public static final String GRID = "/grid";

        public static final String SINGLE = "/single/{name}";
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

        /**
         * Endpoint for fetching a specific property of an instance.
         */
        public static final String PROPERTY = "/{instanceId}/property/{propertyName}";
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

    public static final class ConfigpropsApi {

        /**
         * Base path for configprops APIs.
         */
        public static final String MAIN = "/configprops";
        /**
         * The Configprops Feed used in the single instance
         */
        public static final String FEED = "/feed/{instanceId}";
        /**
         * Endpoint to retrieve a specific Configprops beans of an instance.
         */
        public static final String BEAN_BY_PREFIX = "/{instanceId}/beans/{prefix}";
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
         * Endpoint to clear the logging level of a logger by its name from an instance.
         */
        public static final String CLEAR_FOR_LOGGER = "/{instanceId}/logger/{loggerName}/clear";
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
    }

    public static final class ScheduledTasksApi {

        /**
         * Base path for {@link com.nucleonforge.axile.master.api.ScheduledTasksApi} APIs.
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
    }
}
