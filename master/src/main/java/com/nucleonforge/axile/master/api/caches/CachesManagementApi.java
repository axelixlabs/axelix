package com.nucleonforge.axile.master.api.caches;

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
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.api.ApiPaths;
import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.transport.caches.DisableCacheEndpointProber;
import com.nucleonforge.axile.master.service.transport.caches.DisableCacheManagerEndpointProber;
import com.nucleonforge.axile.master.service.transport.caches.EnableAllCacheEndpointProber;
import com.nucleonforge.axile.master.service.transport.caches.EnableCacheEndpointProber;
import com.nucleonforge.axile.master.service.transport.caches.EnableCacheManagerEndpointProber;

/**
 * The API for managing cache operations - enabling/disabling caches and cache managers.
 *
 * @since 26.11.2025
 * @author Nikita Kirillov
 */
@Tag(
        name = "Caches Management API",
        description = "The caches management endpoint provides operations to manage application's caches.")
@RestController
@RequestMapping(path = ApiPaths.CachesApi.MAIN)
public class CachesManagementApi {

    private final EnableCacheEndpointProber enableCacheEndpointProber;
    private final DisableCacheEndpointProber disableCacheEndpointProber;
    private final EnableCacheManagerEndpointProber enableCacheManagerEndpointProber;
    private final DisableCacheManagerEndpointProber disableCacheManagerEndpointProber;
    private final EnableAllCacheEndpointProber enableAllCacheEndpointProber;

    public CachesManagementApi(
            EnableCacheEndpointProber enableCacheEndpointProber,
            DisableCacheEndpointProber disableCacheEndpointProber,
            EnableCacheManagerEndpointProber enableCacheManagerEndpointProber,
            DisableCacheManagerEndpointProber disableCacheManagerEndpointProber,
            EnableAllCacheEndpointProber enableAllCacheEndpointProber) {
        this.enableCacheEndpointProber = enableCacheEndpointProber;
        this.disableCacheEndpointProber = disableCacheEndpointProber;
        this.enableCacheManagerEndpointProber = enableCacheManagerEndpointProber;
        this.disableCacheManagerEndpointProber = disableCacheManagerEndpointProber;
        this.enableAllCacheEndpointProber = enableAllCacheEndpointProber;
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
        enableCacheEndpointProber.invoke(InstanceId.of(instanceId), createCachePayload(cacheManagerName, cacheName));
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
        disableCacheEndpointProber.invoke(InstanceId.of(instanceId), createCachePayload(cacheManagerName, cacheName));
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
        enableCacheManagerEndpointProber.invoke(InstanceId.of(instanceId), createCacheManagerPayload(cacheManagerName));
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
        disableCacheManagerEndpointProber.invoke(
                InstanceId.of(instanceId), createCacheManagerPayload(cacheManagerName));
    }

    @Operation(
            summary = "Enable all cache managers in the application",
            description = "Activates caching operations for all cache managers and their caches in the application.",
            responses = {
                @ApiResponse(description = "All cache managers enabled successfully", responseCode = "200"),
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
    @PostMapping(ApiPaths.CachesApi.ENABLE_ALL_CACHE)
    public void enableAllCache(@PathVariable("instanceId") String instanceId) {
        enableAllCacheEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
    }

    private HttpPayload createCachePayload(String cacheManagerName, String cacheName) {
        return new DefaultHttpPayload(Map.of("cacheManagerName", cacheManagerName, "cacheName", cacheName));
    }

    private HttpPayload createCacheManagerPayload(String cacheManagerName) {
        return new DefaultHttpPayload(Map.of("cacheManagerName", cacheManagerName));
    }
}
