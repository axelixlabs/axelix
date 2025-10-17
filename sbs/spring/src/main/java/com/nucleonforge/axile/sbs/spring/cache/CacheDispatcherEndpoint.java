package com.nucleonforge.axile.sbs.spring.cache;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.lang.Nullable;

/**
 * Custom Spring Boot Actuator endpoint
 * that exposes operations for managing cache entries via HTTP.
 *
 * <p>This endpoint delegates all cache operations to the {@link CacheDispatcher} implementation.
 *
 * <p>All operations are exposed via HTTP POST requests under the {@code /actuator/cache-dispatcher} path.
 *
 * <p>Supported operations:
 * <ul>
 *     <li>{@code clearKey(cacheManagerName, cache)} — clears the entire cache with the given name using the specified cache manager,</li>
 *     <li>{@code clearKey(cacheManagerName, cache, key)} — evicts a specific entry (by key) from the given cache,</li>
 *     <li>{@code clearAll(cacheManagerName)} — clears all caches managed by the specified cache manager.</li>
 * </ul>
 *
 * @since 24.06.2025
 * @author Nikita Kirillov
 */
@Endpoint(id = "cache-dispatcher")
public class CacheDispatcherEndpoint {

    private final CacheDispatcher dispatcher;

    public CacheDispatcherEndpoint(CacheDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @WriteOperation
    public CacheClearResponse clearKey(
            @Selector String cacheManagerName, @Selector String cacheName, @Nullable Object key) {
        boolean result = key == null
                ? dispatcher.clear(cacheManagerName, cacheName)
                : dispatcher.clear(cacheManagerName, cacheName, key);
        return new CacheClearResponse(result);
    }

    @WriteOperation
    public CacheClearResponse clearAll(@Selector String cacheManagerName) {
        return new CacheClearResponse(dispatcher.clearAll(cacheManagerName));
    }
}
