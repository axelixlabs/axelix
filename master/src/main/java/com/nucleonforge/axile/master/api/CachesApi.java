package com.nucleonforge.axile.master.api;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.links.Link;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.api.caches.ServiceCaches;
import com.nucleonforge.axile.common.api.caches.SingleCache;
import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.common.domain.http.SingleValueQueryParameter;
import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.response.caches.CacheProfileResponse;
import com.nucleonforge.axile.master.api.response.caches.CachesResponse;
import com.nucleonforge.axile.master.service.convert.Converter;
import com.nucleonforge.axile.master.service.transport.caches.CacheByNameEndpointProber;
import com.nucleonforge.axile.master.service.transport.caches.CachesAllEndpointProber;
import com.nucleonforge.axile.master.service.transport.caches.EvictAllCachesEndpointProber;
import com.nucleonforge.axile.master.service.transport.caches.EvictCacheByNameEndpointProber;

/**
 * The API for managing caches.
 *
 * @author Sergey Cherkasov
 */
@Tag(name = "Caches API Controller", description = "The caches endpoint provides access to the application’s caches.")
@RestController
@RequestMapping(path = ApiPaths.CachesApi.MAIN)
public class CachesApi {

    private final CachesAllEndpointProber cachesAllEndpointProber;
    private final CacheByNameEndpointProber cacheByNameEndpointProber;
    private final EvictAllCachesEndpointProber evictAllCachesEndpointProber;
    private final EvictCacheByNameEndpointProber deleteCacheByNameEndpointProber;
    private final Converter<ServiceCaches, CachesResponse> serviceCachesConverter;
    private final Converter<SingleCache, CacheProfileResponse> singleCacheConverter;

    public CachesApi(
            CachesAllEndpointProber cachesAllEndpointProber,
            CacheByNameEndpointProber cacheByNameEndpointProber,
            EvictAllCachesEndpointProber evictAllCachesEndpointProber,
            EvictCacheByNameEndpointProber evictCacheByNameEndpointProber,
            Converter<ServiceCaches, CachesResponse> serviceCachesConverter,
            Converter<SingleCache, CacheProfileResponse> singleCacheConverter) {
        this.cachesAllEndpointProber = cachesAllEndpointProber;
        this.cacheByNameEndpointProber = cacheByNameEndpointProber;
        this.evictAllCachesEndpointProber = evictAllCachesEndpointProber;
        this.deleteCacheByNameEndpointProber = evictCacheByNameEndpointProber;
        this.serviceCachesConverter = serviceCachesConverter;
        this.singleCacheConverter = singleCacheConverter;
    }

    @Operation(
            summary = "Returns details of the application's caches.",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Caches",
                                    description = "https://docs.spring.io/spring-boot/api/rest/actuator/caches.html")
                        },
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = CachesResponse.class))),
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
    @GetMapping(path = ApiPaths.CachesApi.INSTANCE_ID)
    public CachesResponse getAllCaches(@PathVariable("instanceId") String instanceId) {
        ServiceCaches response = cachesAllEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
        return Objects.requireNonNull(serviceCachesConverter.convert(response));
    }

    @Operation(
            summary = "Returns details of the requested cache by its name and cache manager name.",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Caches",
                                    description = "https://docs.spring.io/spring-boot/api/rest/actuator/caches.html")
                        },
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = CacheProfileResponse.class))),
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
        @Parameter(name = "cacheName", description = "The name of the cache to find", required = true),
        @Parameter(
                name = "cacheManager",
                description = "The name of the cache manager to find (query parameter)",
                required = true)
    })
    @GetMapping(path = ApiPaths.CachesApi.CACHE_NAME)
    public CacheProfileResponse getCacheByName(
            @PathVariable("instanceId") String instanceId,
            @PathVariable("cacheName") String cacheName,
            @RequestParam("cacheManager") String cacheManager) {

        SingleValueQueryParameter queryParameter = new SingleValueQueryParameter("cacheManager", cacheManager);
        HttpPayload payload = new DefaultHttpPayload(List.of(queryParameter), Map.of("name", cacheName));
        SingleCache response = cacheByNameEndpointProber.invoke(InstanceId.of(instanceId), payload);
        return Objects.requireNonNull(singleCacheConverter.convert(response));
    }

    @Operation(
            summary = "Clears all caches in the application.",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Loggers",
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
    public void evictAllCaches(@PathVariable("instanceId") String instanceId) {
        evictAllCachesEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
    }

    @Operation(
            summary = "Clears the cache by its name and cache manager name.",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Loggers",
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
        @Parameter(name = "cacheName", description = "The name of the cache to find", required = true),
        @Parameter(
                name = "cacheManager",
                description = "The name of the cache manager to find (query parameter)",
                required = true)
    })
    @DeleteMapping(path = ApiPaths.CachesApi.CACHE_NAME)
    public void evictCacheByName(
            @PathVariable("instanceId") String instanceId,
            @PathVariable("cacheName") String cacheName,
            @RequestParam("cacheManager") String cacheManager) {

        SingleValueQueryParameter queryParameter = new SingleValueQueryParameter("cacheManager", cacheManager);
        HttpPayload payload = new DefaultHttpPayload(List.of(queryParameter), Map.of("name", cacheName));
        deleteCacheByNameEndpointProber.invoke(InstanceId.of(instanceId), payload);
    }
}
