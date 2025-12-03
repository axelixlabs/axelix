package com.nucleonforge.axile.common.api.caches;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The response of the caches actuator endpoint contains a map of all cache managers in the application.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/caches.html">Caches Endpoint</a>
 *
 * @param cacheManagers The list of cache managers in the application.
 *
 * @author Sergey Cherkasov
 */
public record CachesFeed(@JsonProperty("cacheManagers") List<CacheManagers> cacheManagers) {
    public CachesFeed() {
        this(Collections.emptyList());
    }

    /**
     * DTO that encapsulates a map of all caches in the cache manager.
     *
     * @param name   The cache manager name.
     * @param caches The caches are identified by the cache name.
     */
    public record CacheManagers(@JsonProperty("name") String name, @JsonProperty("caches") List<Caches> caches) {}

    /**
     * DTO that encapsulates the full cache name.
     *
     * @param name    The cache name.
     * @param target  The fully qualified name of the native cache.
     * @param enabled Whether the cache is enabled ({@code true}) or disabled ({@code false}).
     */
    public record Caches(
            @JsonProperty("name") String name,
            @JsonProperty("target") String target,
            @JsonProperty("enabled") boolean enabled) {}
}
