package com.nucleonforge.axile.master.api;

import java.util.Map;
import java.util.Objects;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.api.caches.CacheDispatcherClearRequest;
import com.nucleonforge.axile.common.api.caches.CacheDispatcherClearResult;
import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.response.caches.CacheDispatcherClearResponse;
import com.nucleonforge.axile.master.service.convert.Converter;
import com.nucleonforge.axile.master.service.serde.MessageSerializationStrategy;
import com.nucleonforge.axile.master.service.transport.CacheDispatcherClearAllEndpointProber;
import com.nucleonforge.axile.master.service.transport.CacheDispatcherClearEntryEndpointProber;

/**
 * The API for managing cache Managers.
 *
 * @since 02.10.2025
 * @author Nikita Kirillov
 */
@Tag(
        name = "Cache Management API",
        description = "Provides operations for clear caches or specific cache entries in application instances.")
@RestController
@RequestMapping(path = ApiPaths.CacheDispatcherApi.MAIN)
public class CacheDispatcherApi {

    private final CacheDispatcherClearAllEndpointProber cacheDispatcherClearAllEndpointProber;
    private final CacheDispatcherClearEntryEndpointProber cacheDispatcherClearEntryEndpointProber;
    private final Converter<CacheDispatcherClearResult, CacheDispatcherClearResponse> converter;
    private final MessageSerializationStrategy messageSerializationStrategy;

    public CacheDispatcherApi(
            CacheDispatcherClearAllEndpointProber cacheDispatcherClearAllEndpointProber,
            CacheDispatcherClearEntryEndpointProber cacheDispatcherClearEntryEndpointProber,
            Converter<CacheDispatcherClearResult, CacheDispatcherClearResponse> converter,
            MessageSerializationStrategy messageSerializationStrategy) {
        this.cacheDispatcherClearAllEndpointProber = cacheDispatcherClearAllEndpointProber;
        this.cacheDispatcherClearEntryEndpointProber = cacheDispatcherClearEntryEndpointProber;
        this.converter = converter;
        this.messageSerializationStrategy = messageSerializationStrategy;
    }

    @Operation(
            summary = "Clear all cache entries for a given cache manager in a specific application instance.",
            responses = {
                @ApiResponse(
                        description = "Indicates whether all cache entries were successfully cleared",
                        responseCode = "200",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = CacheDispatcherClearResult.class))),
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
        @Parameter(name = "instanceId", description = "Target application instance ID", required = true),
        @Parameter(name = "cacheManagerName", description = "Name of the cache manager", required = true)
    })
    @PostMapping(path = ApiPaths.CacheDispatcherApi.CLEAR_ALL)
    public CacheDispatcherClearResponse clearAll(
            @PathVariable("instanceId") String instanceId, @PathVariable("cacheManagerName") String cacheManagerName) {

        HttpPayload payload = new DefaultHttpPayload(Map.of("cacheManagerName", cacheManagerName));

        CacheDispatcherClearResult result =
                cacheDispatcherClearAllEndpointProber.invoke(InstanceId.of(instanceId), payload);
        return Objects.requireNonNull(converter.convert(result));
    }

    @Operation(
            summary =
                    "Clear a specific cache entry (or the entire cache if key is null) in a given cache manager of an instance.",
            responses = {
                @ApiResponse(
                        description = "Indicates whether the cache entry was successfully cleared",
                        responseCode = "200",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = CacheDispatcherClearResult.class))),
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
        @Parameter(name = "instanceId", description = "Target application instance ID", required = true),
        @Parameter(name = "cacheManagerName", description = "Name of the cache manager", required = true)
    })
    @PostMapping(path = ApiPaths.CacheDispatcherApi.CLEAR_ENTRY)
    public CacheDispatcherClearResponse clearEntry(
            @PathVariable("instanceId") String instanceId,
            @PathVariable("cacheManagerName") String cacheManagerName,
            @RequestBody CacheDispatcherClearRequest request) {

        HttpPayload payload = HttpPayload.json(
                Map.of("cacheManagerName", cacheManagerName), messageSerializationStrategy.serialize(request));
        CacheDispatcherClearResult result =
                cacheDispatcherClearEntryEndpointProber.invoke(InstanceId.of(instanceId), payload);
        return Objects.requireNonNull(converter.convert(result));
    }
}
