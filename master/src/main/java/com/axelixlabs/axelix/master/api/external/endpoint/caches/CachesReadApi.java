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

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.axelixlabs.axelix.common.api.caches.CachesFeed;
import com.axelixlabs.axelix.common.api.caches.SingleCache;
import com.axelixlabs.axelix.common.domain.http.DefaultHttpPayload;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.swagger.DefaultApiResponse;
import com.axelixlabs.axelix.master.api.external.swagger.InstanceIdParameter;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * The API for managing caches. Endpoints for retrieving information about the application caches.
 *
 * @author Sergey Cherkasov
 */
@Tag(name = "Caches API", description = "The caches endpoint provides access to the application’s caches.")
@ExternalApiRestController
@RequestMapping(path = ApiPaths.CachesApi.MAIN)
public class CachesReadApi {

    private final EndpointInvoker endpointInvoker;

    public CachesReadApi(EndpointInvoker endpointInvoker) {
        this.endpointInvoker = endpointInvoker;
    }

    @DefaultApiResponse(summary = "Returns details of the application's caches.")
    @ApiResponse(
            description = "OK",
            responseCode = "200",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CachesFeed.class)))
    @InstanceIdParameter
    @GetMapping(path = ApiPaths.CachesApi.INSTANCE_ID, produces = MediaType.APPLICATION_JSON_VALUE)
    public byte[] getAllCaches(@PathVariable("instanceId") String instanceId) {
        return endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.GET_ALL_CACHES, NoHttpPayload.INSTANCE);
    }

    @DefaultApiResponse(summary = "Returns details of the requested cache by its name and cache manager name.")
    @ApiResponse(
            description = "OK",
            responseCode = "200",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = SingleCache.class)))
    @Parameter(name = "cacheName", description = "The name of the cache to find", required = true)
    @Parameter(
            name = "cacheManager",
            description = "The name of the cache manager where the cache with the given 'cacheName' resides",
            required = true)
    @InstanceIdParameter
    @GetMapping(path = ApiPaths.CachesApi.CACHE_NAME, produces = MediaType.APPLICATION_JSON_VALUE)
    public byte[] getCacheByNameWithQueryParameter(
            @PathVariable("instanceId") String instanceId,
            @PathVariable("cacheName") String cacheName,
            @RequestParam("cacheManager") String cacheManager) {

        HttpPayload payload = new DefaultHttpPayload(Map.of("cacheManagerName", cacheManager, "cacheName", cacheName));

        return endpointInvoker.invoke(InstanceId.of(instanceId), ActuatorEndpoints.GET_SINGLE_CACHE, payload);
    }
}
