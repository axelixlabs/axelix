package com.nucleonforge.axile.common.domain.spring.actuator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.nucleonforge.axile.common.domain.http.HttpMethod;

/**
 * Represents all possible {@link ActuatorEndpoint actuator endpoints} that are being used in the system:
 * <ul>
 *     <li>Custom</li>
 *     <li>Spring Boot</li>
 *     <li>Spring Cloud Config</li>
 *     <li>Spring Cloud Gateway etc.</li>
 * </ul>
 *
 * @apiNote <a href="https://docs.spring.io/spring-boot/reference/actuator/endpoints.html">Actuator Documentation</a>
 * @author Mikhail Polivakha
 */
public class ActuatorEndpoints implements Iterable<ActuatorEndpoint> {

    private final Set<ActuatorEndpoint> endpoints;

    // TODO: check if the HTTP methods are correct
    public static final ActuatorEndpoint CACHES = new ActuatorEndpoint("/caches", HttpMethod.GET);
    public static final ActuatorEndpoint BEANS = new ActuatorEndpoint("/beans", HttpMethod.GET);
    public static final ActuatorEndpoint CONDITIONS = new ActuatorEndpoint("/conditions", HttpMethod.GET);
    public static final ActuatorEndpoint ENV = new ActuatorEndpoint("/env", HttpMethod.GET);
    public static final ActuatorEndpoint FLYWAY = new ActuatorEndpoint("/flyway", HttpMethod.GET);
    public static final ActuatorEndpoint HEALTH = new ActuatorEndpoint("/health", HttpMethod.GET);
    public static final ActuatorEndpoint INFO = new ActuatorEndpoint("/info", HttpMethod.GET);
    public static final ActuatorEndpoint LOGGERS_GET = new ActuatorEndpoint("/loggers", HttpMethod.GET);
    public static final ActuatorEndpoint LOGGERS_POST = new ActuatorEndpoint("/loggers", HttpMethod.POST);
    public static final ActuatorEndpoint LIQUIBASE = new ActuatorEndpoint("/liquibase", HttpMethod.GET);
    public static final ActuatorEndpoint METRICS = new ActuatorEndpoint("/metrics", HttpMethod.GET);
    public static final ActuatorEndpoint MAPPINGS = new ActuatorEndpoint("/mappings", HttpMethod.GET);
    public static final ActuatorEndpoint SCHEDULED_TASKS = new ActuatorEndpoint("/scheduledtasks", HttpMethod.GET);
    public static final ActuatorEndpoint THREAD_DUMP = new ActuatorEndpoint("/threaddump", HttpMethod.GET);
    public static final ActuatorEndpoint HEAP_DUMP = new ActuatorEndpoint("/heapdump", HttpMethod.GET);

    public ActuatorEndpoints() {
        this.endpoints = new HashSet<>(List.of(
                BEANS,
                CACHES,
                CONDITIONS,
                ENV,
                FLYWAY,
                HEALTH,
                INFO,
                LOGGERS_GET,
                LOGGERS_POST,
                LIQUIBASE,
                METRICS,
                MAPPINGS,
                SCHEDULED_TASKS,
                THREAD_DUMP,
                HEAP_DUMP));
    }

    @Override
    public Iterator<ActuatorEndpoint> iterator() {
        return endpoints.iterator();
    }
}
