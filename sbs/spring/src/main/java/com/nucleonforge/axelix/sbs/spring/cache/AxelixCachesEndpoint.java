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

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nucleonforge.axelix.common.api.caches.CachesFeed;
import com.nucleonforge.axelix.common.api.caches.SingleCache;

/**
 * Custom Spring Boot Actuator endpoint that exposes operations for managing cache entries via HTTP.
 *
 * @since 24.06.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@RestControllerEndpoint(id = "axelix-caches")
public class AxelixCachesEndpoint {

    private final CacheOperationsDispatcher dispatcher;

    public AxelixCachesEndpoint(CacheOperationsDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @GetMapping(path = "{cacheManagerName}/{cacheName}")
    public SingleCache getSingleCache(
            @PathVariable("cacheName") String cacheName, @PathVariable("cacheManagerName") String cacheManagerName) {
        return dispatcher.get(cacheManagerName, cacheName);
    }

    @GetMapping
    public CachesFeed getAllCaches() {
        return dispatcher.getAll();
    }

    @DeleteMapping
    public void clearAllCaches() {
        dispatcher.clearAll();
    }

    @DeleteMapping("/{cacheManagerName}/{cacheName}/clear")
    public void clearKey(
            @PathVariable String cacheManagerName,
            @PathVariable String cacheName,
            @RequestParam(required = false) Object key) {

        if (key == null) {
            dispatcher.clear(cacheManagerName, cacheName);
        } else {
            dispatcher.clear(cacheManagerName, cacheName, key);
        }
    }

    @DeleteMapping("/{cacheManagerName}/clear-all")
    public void clearAll(@PathVariable String cacheManagerName) {
        dispatcher.clear(cacheManagerName);
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
