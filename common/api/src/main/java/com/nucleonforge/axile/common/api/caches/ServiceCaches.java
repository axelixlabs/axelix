package com.nucleonforge.axile.common.api.caches;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The response of the caches actuator endpoint contains a map of all cache managers in the application.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/caches.html">Caches Endpoint</a>
 *
 * @param cacheManagers   The cache managers identified by the cache manager name.
 *
 * @author Sergey Cherkasov
 */
public record ServiceCaches(@JsonProperty("cacheManagers") Map<String, CacheManagers> cacheManagers) {
    public ServiceCaches() {
        this(Collections.emptyMap());
    }

    /**
     * DTO that encapsulates a map of all caches in the cache manager.
     *
     * @param caches   The caches are identified by the cache name.
     */
    public record CacheManagers(@JsonProperty("caches") Map<String, Caches> caches) {

        /**
         * DTO that encapsulates the full cache name.
         *
         * @param target   The fully qualified name of the native cache.
         */
        public record Caches(@JsonProperty("target") String target) {}
    }
}
