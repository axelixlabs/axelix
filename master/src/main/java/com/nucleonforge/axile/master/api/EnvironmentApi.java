package com.nucleonforge.axile.master.api;

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
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.api.env.EnvironmentFeed;
import com.nucleonforge.axile.common.api.env.EnvironmentProperty;
import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.api.response.EnvironmentFeedResponse;
import com.nucleonforge.axile.master.api.response.EnvironmentPropertyResponse;
import com.nucleonforge.axile.master.service.convert.Converter;
import com.nucleonforge.axile.master.service.transport.EnvironmentEndpointProber;
import com.nucleonforge.axile.master.service.transport.EnvironmentPropertyEndpointProber;

/**
 * The API for managing environment.
 *
 * @since 27.08.2025
 * @author Nikita Kirillov
 */
@Tag(
        name = "Environment API Controller",
        description = "The env endpoint provides information about the application’s Environment.")
@RestController
@RequestMapping(path = ApiPaths.EnvironmentApi.MAIN)
public class EnvironmentApi {

    private final EnvironmentEndpointProber environmentEndpointProber;
    private final EnvironmentPropertyEndpointProber environmentPropertyEndpointProber;
    private final Converter<EnvironmentFeed, EnvironmentFeedResponse> envConverter;
    private final Converter<EnvironmentProperty, EnvironmentPropertyResponse> envPropertyConverter;

    public EnvironmentApi(
            EnvironmentEndpointProber environmentEndpointProber,
            EnvironmentPropertyEndpointProber environmentPropertyEndpointProber,
            Converter<EnvironmentFeed, EnvironmentFeedResponse> envConverter,
            Converter<EnvironmentProperty, EnvironmentPropertyResponse> envPropertyConverter) {
        this.environmentEndpointProber = environmentEndpointProber;
        this.environmentPropertyEndpointProber = environmentPropertyEndpointProber;
        this.envConverter = envConverter;
        this.envPropertyConverter = envPropertyConverter;
    }

    @Operation(
            summary = "Returns information about the application’s Environment.",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Spring Boot / Actuator / Environment (env)",
                                    description = "https://docs.spring.io/spring-boot/api/rest/actuator/env.html")
                        },
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = EnvironmentFeedResponse.class))),
                @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            })
    @Parameter(name = "instanceId", description = "Application Instance ID", required = true)
    @GetMapping(path = ApiPaths.EnvironmentApi.FEED)
    public EnvironmentFeedResponse getEnvironment(@PathVariable("instanceId") String instanceId) {
        EnvironmentFeed result = environmentEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
        return Objects.requireNonNull(envConverter.convert(result));
    }

    @Operation(
            summary = "Returns a specific property of an instance",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Environment(env)",
                                    description = "https://docs.spring.io/spring-boot/api/rest/actuator/env.html")
                        },
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = EnvironmentPropertyResponse.class))),
                @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            })
    @Parameters({
        @Parameter(name = "instanceId", description = "Application Instance ID", required = true),
        @Parameter(name = "propertyName", description = "Name of the environment property", required = true)
    })
    @GetMapping(path = ApiPaths.EnvironmentApi.PROPERTY)
    public EnvironmentPropertyResponse getProperty(
            @PathVariable("instanceId") String instanceId, @PathVariable("propertyName") String propertyName) {
        HttpPayload payload = new DefaultHttpPayload(Map.of("property.name", propertyName));

        EnvironmentProperty result = environmentPropertyEndpointProber.invoke(InstanceId.of(instanceId), payload);
        return Objects.requireNonNull(envPropertyConverter.convert(result));
    }
}
