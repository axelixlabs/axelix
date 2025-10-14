package com.nucleonforge.axile.common.domain.spring.actuator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.nucleonforge.axile.common.domain.http.HttpMethod;

import static com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint.of;

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
 */
public class ActuatorEndpoints implements Iterable<ActuatorEndpoint> {

    private final Set<ActuatorEndpoint> endpoints;

    // spotless:off

    // Beans
    public static final ActuatorEndpoint BEANS = of("/beans", HttpMethod.GET);

    // Caches
    public static final ActuatorEndpoint GET_ALL_CACHES = of("/caches", HttpMethod.GET);
    public static final ActuatorEndpoint GET_SINGLE_CACHE = of("/caches/{name}", HttpMethod.GET);
    public static final ActuatorEndpoint CLEAR_ALL_CACHES = of("/caches", HttpMethod.DELETE);
    public static final ActuatorEndpoint CLEAR_SINGLE_CACHES = of("/caches/{name}", HttpMethod.DELETE);

    // Conditions
    public static final ActuatorEndpoint CONDITIONS = of("/conditions", HttpMethod.GET);

    // @ConfigurationProperties beans
    public static final ActuatorEndpoint CONFIG_PROPS = of("/configprops", HttpMethod.GET);
    public static final ActuatorEndpoint CONFIG_PROPS_BY_PREFIX = of("/configprops/{prefix}", HttpMethod.GET);

    // Environment
    public static final ActuatorEndpoint ENV = of("/env", HttpMethod.GET);
    public static final ActuatorEndpoint ENV_PROPERTY = of("/env/{property.name}", HttpMethod.GET);

    // Flyway
    public static final ActuatorEndpoint FLYWAY = of("/flyway", HttpMethod.GET);

    // Health
    public static final ActuatorEndpoint HEALTH = of("/health", HttpMethod.GET);
    public static final ActuatorEndpoint HEALTH_COMPONENT = of("/health/{component}", HttpMethod.GET);
    public static final ActuatorEndpoint HEALTH_SUB_COMPONENT = of("/health/{component}/{sub-component}", HttpMethod.GET);

    // Heap Dump
    public static final ActuatorEndpoint HEAP_DUMP = of("/heapdump", HttpMethod.GET);

    // Http Exchanges
    public static final ActuatorEndpoint HTTP_EXCHANGES = of("/httpexchanges", HttpMethod.GET);

    // General Info
    public static final ActuatorEndpoint INFO = of("/info", HttpMethod.GET);

    // Integration Graph (Spring Integrations project)
    public static final ActuatorEndpoint INTEGRATIONS_GRAPH = of("/integrationgraph", HttpMethod.GET);
    public static final ActuatorEndpoint REBUILD_INTEGRATIONS_GRAPH = of("/integrationgraph", HttpMethod.POST);

    // Liquibase
    public static final ActuatorEndpoint LIQUIBASE = of("/liquibase", HttpMethod.GET);

    // Log File
    public static final ActuatorEndpoint LOG_FILE = of("/logfile", HttpMethod.GET);

    // Loggers
    public static final ActuatorEndpoint ALL_LOGGERS = of("/loggers", HttpMethod.GET);
    public static final ActuatorEndpoint ONE_LOGGER = of("/loggers/{logger.name}", HttpMethod.GET);
    public static final ActuatorEndpoint LOGGER_GROUP = of("/loggers/{group.name}", HttpMethod.GET);
    public static final ActuatorEndpoint SET_ONE_LOGGER = of("/loggers/{logger.name}", HttpMethod.POST);
    public static final ActuatorEndpoint SET_FOR_LOGGER_GROUP = of("/loggers/{group.name}", HttpMethod.POST);
    public static final ActuatorEndpoint CLEAR_FOR_LOGGER = of("/loggers/{logger.name}", HttpMethod.POST);

    // Mappings
    public static final ActuatorEndpoint MAPPINGS = of("/mappings", HttpMethod.GET);

    // Metadata
    public static final ActuatorEndpoint METADATA = of("/axile-metadata", HttpMethod.GET);

    // Metric
    public static final ActuatorEndpoint METRICS = of("/metrics", HttpMethod.GET);
    public static final ActuatorEndpoint SINGLE_METRIC = of("/metrics/{metric.name}", HttpMethod.GET);

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
    public static final ActuatorEndpoint SCHEDULED_TASKS = of("/scheduledtasks", HttpMethod.GET);
    public static final ActuatorEndpoint ENABLE_SCHEDULED_TASK = of("/scheduled-tasks-management/enable", HttpMethod.POST);
    public static final ActuatorEndpoint DISABLE_SCHEDULED_TASK = of("/scheduled-tasks-management/disable", HttpMethod.POST);

    // Sessions
    public static final ActuatorEndpoint SESSION = of("/sessions", HttpMethod.GET);
    public static final ActuatorEndpoint SINGLE_SESSION = of("/sessions/{id}", HttpMethod.GET);
    public static final ActuatorEndpoint DELETE_SESSION = of("/sessions/{id}", HttpMethod.DELETE);

    // Shutdown
    public static final ActuatorEndpoint SHUTDOWN = of("/shutdown", HttpMethod.POST);

    // Startup
    public static final ActuatorEndpoint STARTUP = of("/startup", HttpMethod.POST);

    // Thread Dump
    public static final ActuatorEndpoint THREAD_DUMP = of("/threaddump", HttpMethod.GET);

    // spotless:on

    public ActuatorEndpoints() {
        this.endpoints = new HashSet<>(List.of(
                BEANS,
                GET_ALL_CACHES,
                GET_SINGLE_CACHE,
                CLEAR_ALL_CACHES,
                CLEAR_SINGLE_CACHES,
                CONDITIONS,
                CONFIG_PROPS,
                CONFIG_PROPS_BY_PREFIX,
                ENV,
                ENV_PROPERTY,
                FLYWAY,
                HEALTH,
                HEALTH_COMPONENT,
                HEALTH_SUB_COMPONENT,
                HEAP_DUMP,
                HTTP_EXCHANGES,
                INFO,
                INTEGRATIONS_GRAPH,
                REBUILD_INTEGRATIONS_GRAPH,
                LIQUIBASE,
                LOG_FILE,
                ALL_LOGGERS,
                ONE_LOGGER,
                LOGGER_GROUP,
                SET_ONE_LOGGER,
                SET_FOR_LOGGER_GROUP,
                CLEAR_FOR_LOGGER,
                MAPPINGS,
                METRICS,
                SINGLE_METRIC,
                PROMETHEUS,
                QUARTZ,
                QUARTZ_JOBS,
                QUARTZ_SINGLE_JOB,
                QUARTZ_TRIGGERS,
                ALL_SBOMS,
                SINGLE_SBOM,
                SCHEDULED_TASKS,
                ENABLE_SCHEDULED_TASK,
                DISABLE_SCHEDULED_TASK,
                SESSION,
                SINGLE_SESSION,
                DELETE_SESSION,
                SHUTDOWN,
                STARTUP,
                THREAD_DUMP));
    }

    @Override
    public Iterator<ActuatorEndpoint> iterator() {
        return endpoints.iterator();
    }
}
