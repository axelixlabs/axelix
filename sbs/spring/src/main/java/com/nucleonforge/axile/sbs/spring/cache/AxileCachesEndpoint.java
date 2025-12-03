package com.nucleonforge.axile.sbs.spring.cache;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.actuate.cache.CachesEndpoint;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nucleonforge.axile.common.api.caches.CachesFeed;
import com.nucleonforge.axile.common.api.caches.CachesFeed.CacheManagers;
import com.nucleonforge.axile.common.api.caches.CachesFeed.Caches;

/**
 * Custom Spring Boot Actuator endpoint that exposes operations for managing cache entries via HTTP.
 *
 * @since 24.06.2025
 * @author Nikita Kirillov
 */
@RestControllerEndpoint(id = "axile-caches")
public class AxileCachesEndpoint {

    private final CacheDispatcher dispatcher;

    private final CachesEndpoint delegate;

    public AxileCachesEndpoint(CacheDispatcher dispatcher, CachesEndpoint delegate) {
        this.dispatcher = dispatcher;
        this.delegate = delegate;
    }

    @GetMapping
    public CachesFeed caches() {
        CachesEndpoint.CachesDescriptor originalDescriptor = delegate.caches();

        List<CacheManagers> extendedCacheManagers = new ArrayList<>();

        originalDescriptor.getCacheManagers().forEach((managerName, cacheManagerDescriptor) -> {
            List<Caches> extendedCaches = new ArrayList<>();

            cacheManagerDescriptor.getCaches().forEach((cacheName, cacheDescriptor) -> {
                boolean isEnabled = dispatcher.isCacheEnabled(managerName, cacheName);

                Caches extendedCache = new Caches(cacheName, cacheDescriptor.getTarget(), isEnabled);
                extendedCaches.add(extendedCache);
            });

            CacheManagers extendedCacheManager = new CacheManagers(managerName, extendedCaches);
            extendedCacheManagers.add(extendedCacheManager);
        });

        return new CachesFeed(extendedCacheManagers);
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
}
