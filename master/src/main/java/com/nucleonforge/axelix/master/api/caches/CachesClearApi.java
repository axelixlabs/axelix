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
package com.nucleonforge.axelix.master.api.caches;

import java.util.Map;

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
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axelix.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axelix.common.domain.http.HttpPayload;
import com.nucleonforge.axelix.common.domain.http.NoHttpPayload;
import com.nucleonforge.axelix.master.api.ApiPaths;
import com.nucleonforge.axelix.master.api.error.SimpleApiError;
import com.nucleonforge.axelix.master.model.instance.InstanceId;
import com.nucleonforge.axelix.master.service.transport.caches.ClearAllCachesEndpointProber;
import com.nucleonforge.axelix.master.service.transport.caches.ClearCacheByNameEndpointProber;

/**
 * The API for managing caches. Endpoints for clearing the application caches.
 *
 * @author Sergey Cherkasov
 */
@Tag(name = "Caches API", description = "The caches endpoint provides access to the application’s caches.")
@RestController
@RequestMapping(path = ApiPaths.CachesApi.MAIN)
public class CachesClearApi {

    private final ClearAllCachesEndpointProber clearAllCachesEndpointProber;
    private final ClearCacheByNameEndpointProber clearCacheByNameEndpointProber;

    public CachesClearApi(
            ClearAllCachesEndpointProber clearAllCachesEndpointProber,
            ClearCacheByNameEndpointProber clearCacheByNameEndpointProber) {
        this.clearAllCachesEndpointProber = clearAllCachesEndpointProber;
        this.clearCacheByNameEndpointProber = clearCacheByNameEndpointProber;
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
        clearAllCachesEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
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
        clearCacheByNameEndpointProber.invoke(InstanceId.of(instanceId), payload);
    }
}
