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

import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.links.Link;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.axelixlabs.axelix.common.domain.http.DefaultHttpPayload;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.api.error.SimpleApiError;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * The API for managing caches. Endpoints for clearing the application caches.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@Tag(name = "Caches API", description = "The caches endpoint provides access to the application’s caches.")
@ExternalApiRestController
@RequestMapping(path = ApiPaths.CachesApi.MAIN)
public class CachesClearApi {

    private final EndpointInvoker endpointInvoker;

    public CachesClearApi(EndpointInvoker endpointInvoker) {
        this.endpointInvoker = endpointInvoker;
    }

    @Operation(
            summary = "Clears all caches in the application.",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Caches",
                                    description = "https://docs.spring.io/spring-boot/api/rest/actuator/caches.html")
                        }),
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
    @Parameter(name = "instanceId", description = "Application Instance ID", required = true)
    @DeleteMapping(path = ApiPaths.CachesApi.INSTANCE_ID)
    public void clearAllCaches(@PathVariable("instanceId") String instanceId) {
        endpointInvoker.invokeNoValue(
                InstanceId.of(instanceId), ActuatorEndpoints.CLEAR_ALL_CACHES, NoHttpPayload.INSTANCE);
    }

    @Operation(
            summary = "Clears the cache by its name and cache manager name.",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Caches",
                                    description = "https://docs.spring.io/spring-boot/api/rest/actuator/caches.html")
                        }),
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
        @Parameter(name = "cacheName", description = "The name of the cache to clear", required = true),
        @Parameter(
                name = "cacheManager",
                description = "The name of the cache manager where the cache with the given 'cacheName' resides",
                required = true)
    })
    @DeleteMapping(path = ApiPaths.CachesApi.CACHE_NAME)
    public void clearSpecificCacheEntity(
            @PathVariable("instanceId") String instanceId,
            @PathVariable("cacheName") String cacheName,
            @RequestParam("cacheManager") String cacheManager) {
        HttpPayload payload = new DefaultHttpPayload(Map.of("cacheName", cacheName, "cacheManagerName", cacheManager));
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.CLEAR_SINGLE_CACHE, payload);
    }
}
