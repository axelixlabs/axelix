package com.nucleonforge.axile.sbs.spring.cache;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Custom Spring Boot Actuator endpoint that exposes operations for managing cache entries via HTTP.
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

    @PostMapping("/{cacheManagerName}/{cacheName}/clear")
    public CacheClearResponse clearKey(
            @PathVariable String cacheManagerName,
            @PathVariable String cacheName,
            @RequestParam(required = false) Object key) {

        boolean result = key == null
                ? dispatcher.clear(cacheManagerName, cacheName)
                : dispatcher.clear(cacheManagerName, cacheName, key);
        return new CacheClearResponse(result);
    }

    @PostMapping("/{cacheManagerName}/clear-all")
    public CacheClearResponse clearAll(@PathVariable String cacheManagerName) {
        return new CacheClearResponse(dispatcher.clearAll(cacheManagerName));
    }

    @PostMapping("/{cacheManagerName}/enable")
    public void enableManager(@PathVariable String cacheManagerName) {
        dispatcher.enableCacheManager(cacheManagerName);
    }

    @PostMapping("/{cacheManagerName}/disable")
    public void disableManager(@PathVariable String cacheManagerName) {
        dispatcher.disableCacheManager(cacheManagerName);
    }

    @PostMapping("/{cacheManagerName}/{cacheName}/enable")
    public void enableCache(@PathVariable String cacheManagerName, @PathVariable String cacheName) {
        dispatcher.enableCache(cacheManagerName, cacheName);
    }

    @PostMapping("/{cacheManagerName}/{cacheName}/disable")
    public void disableCache(@PathVariable String cacheManagerName, @PathVariable String cacheName) {
        dispatcher.disableCache(cacheManagerName, cacheName);
    }

    @PostMapping("/enable-all-cache")
    public void enableAllCache() {
        dispatcher.enableAllCache();
    }
}
