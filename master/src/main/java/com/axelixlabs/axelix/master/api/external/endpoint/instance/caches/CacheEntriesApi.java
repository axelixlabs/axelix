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
package com.axelixlabs.axelix.master.api.external.endpoint.instance.caches;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.service.CacheEntryManagement;
import com.axelixlabs.axelix.master.service.transport.PartiallyUpdatedException;

/**
 * API for checking and evicting entries from a specific cache.
 */
@ExternalApiRestController
public class CacheEntriesApi {

    private final CacheEntryManagement entries;

    public CacheEntriesApi(CacheEntryManagement entries) {
        this.entries = entries;
    }

    @PostMapping(ApiPaths.CachesApi.CACHE_KEY_EXISTS)
    public CacheEntryManagement.KeyPresence exists(@RequestBody CacheKeyRequest request) {
        return entries.exists(request.instanceIds(), request.cacheManagerName(), request.cacheName(), request.key());
    }

    @DeleteMapping(ApiPaths.CachesApi.CACHE_ENTRIES)
    public ResponseEntity<?> evict(@RequestBody CacheKeyRequest request) {
        try {
            entries.evict(request.instanceIds(), request.cacheManagerName(), request.cacheName(), request.key());
            return ResponseEntity.noContent().build();
        } catch (PartiallyUpdatedException e) {
            return ResponseEntity.badRequest().body(new EvictionFailure(e.failedInstanceIds()));
        }
    }

    public record CacheKeyRequest(List<String> instanceIds, String cacheManagerName, String cacheName, String key) {}

    public record EvictionFailure(List<String> failedInstanceIds) {}
}
