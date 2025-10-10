package com.nucleonforge.axile.common.api.caches;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * DTO that encapsulates the details of the requested cache.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/caches.html">Caches Endpoint</a>
 *
 * @param name            The cache name.
 * @param target          The fully qualified name of the native cache.
 * @param cacheManager    The cache manager name.
 *
 * @author Sergey Cherkasov
 */
public record SingleCache(
        @JsonProperty("name") String name,
        @JsonProperty("target") String target,
        @JsonProperty("cacheManager") String cacheManager) {}
