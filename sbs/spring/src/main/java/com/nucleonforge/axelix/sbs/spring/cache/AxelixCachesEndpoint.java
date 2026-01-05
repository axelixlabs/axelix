/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.sbs.spring.cache;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.actuate.cache.CachesEndpoint;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nucleonforge.axelix.common.api.caches.CachesFeed;
import com.nucleonforge.axelix.common.api.caches.CachesFeed.CacheManagers;
import com.nucleonforge.axelix.common.api.caches.CachesFeed.Caches;

/**
 * Custom Spring Boot Actuator endpoint that exposes operations for managing cache entries via HTTP.
 *
 * @since 24.06.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@RestControllerEndpoint(id = "axelix-caches")
public class AxelixCachesEndpoint {

    private final CacheDispatcher dispatcher;
    private final CachesEndpoint delegate;

    public AxelixCachesEndpoint(CacheDispatcher dispatcher, CachesEndpoint delegate) {
        this.dispatcher = dispatcher;
        this.delegate = delegate;
    }

    @GetMapping(path = "{cacheManagerName}/{cacheName}")
    public CachesEndpoint.CacheEntryDescriptor getSingleCache(
            @PathVariable("cacheName") String cacheName, @PathVariable("cacheManagerName") String cacheManagerName) {
        return delegate.cache(cacheName, cacheManagerName);
    }

    @GetMapping
    public CachesFeed getAllCaches() {
        CachesEndpoint.CachesDescriptor originalDescriptor = delegate.caches();

        List<CacheManagers> extendedCacheManagers = new ArrayList<>();

        originalDescriptor.getCacheManagers().forEach((managerName, cacheManagerDescriptor) -> {
            List<Caches> extendedCaches = new ArrayList<>();

            cacheManagerDescriptor.getCaches().forEach((cacheName, cacheDescriptor) -> {
                boolean isEnabled = dispatcher.isCacheEnabled(managerName, cacheName);

                Caches extendedCache = new Caches(
                        cacheName,
                        cacheDescriptor.getTarget(),
                        dispatcher.getHitsCount(managerName, cacheName),
                        dispatcher.getMissesCount(managerName, cacheName),
                        dispatcher.getEstimatedCacheSize(managerName, cacheName),
                        isEnabled);
                extendedCaches.add(extendedCache);
            });

            CacheManagers extendedCacheManager = new CacheManagers(managerName, extendedCaches);
            extendedCacheManagers.add(extendedCacheManager);
        });

        return new CachesFeed(extendedCacheManagers);
    }

    @DeleteMapping
    public void clearAllCaches() {
        delegate.clearCaches();
    }

    @DeleteMapping("/{cacheManagerName}/{cacheName}/clear")
    public CacheClearResponse clearKey(
            @PathVariable String cacheManagerName,
            @PathVariable String cacheName,
            @RequestParam(required = false) Object key) {

        boolean result = key == null
                ? dispatcher.clear(cacheManagerName, cacheName)
                : dispatcher.clear(cacheManagerName, cacheName, key);
        return new CacheClearResponse(result);
    }

    @DeleteMapping("/{cacheManagerName}/clear-all")
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
