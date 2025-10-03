package com.nucleonforge.axile.spring.cache;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.nucleonforge.axile.common.api.caches.CacheDispatcherClearRequest;
import com.nucleonforge.axile.common.api.caches.CacheDispatcherClearResult;

/**
 * Custom Spring Boot Actuator endpoint
 * that exposes operations for managing cache entries via HTTP.
 *
 * @since 24.06.2025
 * @author Nikita Kirillov
 */
@RestControllerEndpoint(id = "cache-dispatcher")
public class CacheDispatcherEndpoint {

    private final CacheDispatcher dispatcher;

    public CacheDispatcherEndpoint(CacheDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @PostMapping("/{cacheManagerName}/clear-all")
    public CacheDispatcherClearResult clearAll(@PathVariable("cacheManagerName") String cacheManagerName) {
        return new CacheDispatcherClearResult(dispatcher.clearAll(cacheManagerName));
    }

    @PostMapping("/{cacheManagerName}/clear")
    public CacheDispatcherClearResult clearByCacheName(
            @PathVariable("cacheManagerName") String cacheManagerName,
            @RequestBody CacheDispatcherClearRequest request) {
        boolean result = request.key() == null
                ? dispatcher.clear(cacheManagerName, request.cacheName())
                : dispatcher.clear(cacheManagerName, request.cacheName(), request.key());
        return new CacheDispatcherClearResult(result);
    }
}
