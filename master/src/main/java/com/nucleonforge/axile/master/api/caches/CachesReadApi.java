package com.nucleonforge.axile.master.api.caches;

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

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.api.caches.CachesFeed;
import com.nucleonforge.axile.common.api.caches.SingleCache;
import com.nucleonforge.axile.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.common.domain.http.SingleValueQueryParameter;
import com.nucleonforge.axile.master.api.ApiPaths;
import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.response.caches.CacheProfileResponse;
import com.nucleonforge.axile.master.api.response.caches.CachesResponse;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.convert.response.Converter;
import com.nucleonforge.axile.master.service.transport.caches.GetAllCachesEndpointProber;
import com.nucleonforge.axile.master.service.transport.caches.GetCacheByNameEndpointProber;

/**
 * The API for managing caches. Endpoints for retrieving information about the application caches.
 *
 * @author Sergey Cherkasov
 */
@Tag(name = "Caches API", description = "The caches endpoint provides access to the application’s caches.")
@RestController
@RequestMapping(path = ApiPaths.CachesApi.MAIN)
public class CachesReadApi {

    private final GetAllCachesEndpointProber getAllCachesEndpointProber;
    private final GetCacheByNameEndpointProber getCacheByNameEndpointProber;
    private final Converter<CachesFeed, CachesResponse> cachesFeedConverter;
    private final Converter<SingleCache, CacheProfileResponse> singleCacheConverter;

    public CachesReadApi(
            GetAllCachesEndpointProber getAllCachesEndpointProber,
            GetCacheByNameEndpointProber getCacheByNameEndpointProber,
            Converter<CachesFeed, CachesResponse> cachesFeedConverter,
            Converter<SingleCache, CacheProfileResponse> singleCacheConverter) {
        this.getAllCachesEndpointProber = getAllCachesEndpointProber;
        this.getCacheByNameEndpointProber = getCacheByNameEndpointProber;
        this.cachesFeedConverter = cachesFeedConverter;
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
        CachesFeed response = getAllCachesEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
        return Objects.requireNonNull(cachesFeedConverter.convert(response));
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
                description = "The name of the cache manager where the cache with the given 'cacheName' resides",
                required = true)
    })
    @GetMapping(path = ApiPaths.CachesApi.CACHE_NAME)
    public CacheProfileResponse getCacheByNameWithQueryParameter(
            @PathVariable("instanceId") String instanceId,
            @PathVariable("cacheName") String cacheName,
            @RequestParam("cacheManager") String cacheManager) {

        SingleValueQueryParameter queryParameter = new SingleValueQueryParameter("cacheManager", cacheManager);
        HttpPayload payload = new DefaultHttpPayload(List.of(queryParameter), Map.of("name", cacheName));
        SingleCache response = getCacheByNameEndpointProber.invoke(InstanceId.of(instanceId), payload);
        return Objects.requireNonNull(singleCacheConverter.convert(response));
    }
}
