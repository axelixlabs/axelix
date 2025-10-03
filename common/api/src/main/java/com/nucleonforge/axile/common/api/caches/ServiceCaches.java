package com.nucleonforge.axile.common.api.caches;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The response to caches actuator endpoint.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/caches.html">Caches Endpoint</a>
 *
 * @author Sergey Cherkasov
 */
public record ServiceCaches(@JsonProperty("cacheManagers") Map<String, CacheManagers> cacheManagers) {
    public ServiceCaches() {
        this(Collections.emptyMap());
    }

    public record CacheManagers(@JsonProperty("caches") Map<String, Caches> caches) {
        public record Caches(@JsonProperty("target") String target) {}
    }
}
