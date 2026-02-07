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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.axelixlabs.axelix.common.domain.http.DefaultHttpPayload;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.master.api.error.SimpleApiError;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * The API for managing cache operations - enabling/disabling caches and cache managers.
 *
 * @since 26.11.2025
 * @author Nikita Kirillov
 */
@Tag(
        name = "Caches Management API",
        description = "The caches management endpoint provides operations to manage application's caches.")
@ExternalApiRestController
@RequestMapping(path = ApiPaths.CachesApi.MAIN)
public class CachesManagementApi {

    private final EndpointInvoker endpointInvoker;

    public CachesManagementApi(EndpointInvoker endpointInvoker) {
        this.endpointInvoker = endpointInvoker;
    }

    @Operation(
            summary = "Enables a specific cache in the cache manager",
            description =
                    "Activates caching operations for the specified cache. After enabling, the cache will start storing and retrieving data.",
            responses = {
                @ApiResponse(description = "Cache enabled successfully", responseCode = "200"),
                @ApiResponse(
                        description = "Bad Request",
                        responseCode = "400",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class))),
                @ApiResponse(
                        description = "Internal Server Error",
                        responseCode = "500",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class)))
            })
    @Parameters({
        @Parameter(name = "instanceId", description = "Application Instance ID", required = true),
        @Parameter(name = "cacheManagerName", description = "The name of the cache manager", required = true),
        @Parameter(name = "cacheName", description = "The name of the cache to enable", required = true)
    })
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

    @Operation(
            summary = "Disables a specific cache in the cache manager",
            description =
                    "Deactivates caching operations for the specified cache. After disabling, cache operations become no-op.",
            responses = {
                @ApiResponse(description = "Cache disabled successfully", responseCode = "200"),
                @ApiResponse(
                        description = "Bad Request",
                        responseCode = "400",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class))),
                @ApiResponse(
                        description = "Internal Server Error",
                        responseCode = "500",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class)))
            })
    @Parameters({
        @Parameter(name = "instanceId", description = "Application Instance ID", required = true),
        @Parameter(name = "cacheManagerName", description = "The name of the cache manager", required = true),
        @Parameter(name = "cacheName", description = "The name of the cache to disable", required = true)
    })
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

    @Operation(
            summary = "Enables all caches in the cache manager",
            description = "Activates caching operations for all caches managed by the specified cache manager.",
            responses = {
                @ApiResponse(description = "Cache manager enabled successfully", responseCode = "200"),
                @ApiResponse(
                        description = "Bad Request",
                        responseCode = "400",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class))),
                @ApiResponse(
                        description = "Internal Server Error",
                        responseCode = "500",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class)))
            })
    @Parameters({
        @Parameter(name = "instanceId", description = "Application Instance ID", required = true),
        @Parameter(name = "cacheManagerName", description = "The name of the cache manager to enable", required = true)
    })
    @PostMapping(ApiPaths.CachesApi.ENABLE_CACHE_MANAGER)
    public void enableCacheManager(
            @PathVariable("instanceId") String instanceId, @PathVariable("cacheManagerName") String cacheManagerName) {

        endpointInvoker.invokeNoValue(
                InstanceId.of(instanceId),
                ActuatorEndpoints.ENABLE_CACHE_MANAGER,
                createCacheManagerPayload(cacheManagerName));
    }

    @Operation(
            summary = "Disables all caches in the cache manager",
            description = "Deactivates caching operations for all caches managed by the specified cache manager.",
            responses = {
                @ApiResponse(description = "Cache manager disabled successfully", responseCode = "200"),
                @ApiResponse(
                        description = "Bad Request",
                        responseCode = "400",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class))),
                @ApiResponse(
                        description = "Internal Server Error",
                        responseCode = "500",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class)))
            })
    @Parameters({
        @Parameter(name = "instanceId", description = "Application Instance ID", required = true),
        @Parameter(name = "cacheManagerName", description = "The name of the cache manager to disable", required = true)
    })
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
