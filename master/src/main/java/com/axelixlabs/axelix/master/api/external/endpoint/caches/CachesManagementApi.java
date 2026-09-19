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
package com.axelixlabs.axelix.master.api.external.endpoint.caches;

import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.http.DefaultHttpPayload;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * The API for managing cache operations - enabling/disabling caches and cache managers.
 *
 * @since 26.11.2025
 * @author Nikita Kirillov
 */
@ExternalApiRestController
public class CachesManagementApi {

    private final EndpointInvoker endpointInvoker;

    public CachesManagementApi(EndpointInvoker endpointInvoker) {
        this.endpointInvoker = endpointInvoker;
    }

    @PostMapping(ApiPaths.CachesApi.ENABLE_CACHE)
    public void enableCache(
            @PathVariable("instanceId") String instanceId,
            @PathVariable("cacheManagerName") String cacheManagerName,
            @PathVariable("cacheName") String cacheName) {

        endpointInvoker.invokeNoValue(
                InstanceId.of(instanceId),
                ActuatorEndpoints.ENABLE_CACHE,
                createCachePayload(cacheManagerName, cacheName));
    }

    @PostMapping(ApiPaths.CachesApi.DISABLE_CACHE)
    public void disableCache(
            @PathVariable("instanceId") String instanceId,
            @PathVariable("cacheManagerName") String cacheManagerName,
            @PathVariable("cacheName") String cacheName) {

        endpointInvoker.invokeNoValue(
                InstanceId.of(instanceId),
                ActuatorEndpoints.DISABLE_CACHE,
                createCachePayload(cacheManagerName, cacheName));
    }

    @PostMapping(ApiPaths.CachesApi.ENABLE_CACHE_MANAGER)
    public void enableCacheManager(
            @PathVariable("instanceId") String instanceId, @PathVariable("cacheManagerName") String cacheManagerName) {

        endpointInvoker.invokeNoValue(
                InstanceId.of(instanceId),
                ActuatorEndpoints.ENABLE_CACHE_MANAGER,
                createCacheManagerPayload(cacheManagerName));
    }

    @PostMapping(ApiPaths.CachesApi.DISABLE_CACHE_MANAGER)
    public void disableCacheManager(
            @PathVariable("instanceId") String instanceId, @PathVariable("cacheManagerName") String cacheManagerName) {

        endpointInvoker.invokeNoValue(
                InstanceId.of(instanceId),
                ActuatorEndpoints.DISABLE_CACHES_MANAGER,
                createCacheManagerPayload(cacheManagerName));
    }

    private HttpPayload createCachePayload(String cacheManagerName, String cacheName) {
        return new DefaultHttpPayload(Map.of("cacheManagerName", cacheManagerName, "cacheName", cacheName));
    }

    private HttpPayload createCacheManagerPayload(String cacheManagerName) {
        return new DefaultHttpPayload(Map.of("cacheManagerName", cacheManagerName));
    }
}
