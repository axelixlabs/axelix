/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.sbs.spring.cache;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.axelixlabs.axelix.common.api.caches.CachesFeed;
import com.axelixlabs.axelix.common.api.caches.SingleCache;

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
