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
package com.nucleonforge.axelix.master.api;

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

import com.nucleonforge.axelix.common.api.env.EnvironmentFeed;
import com.nucleonforge.axelix.common.api.env.EnvironmentProperty;
import com.nucleonforge.axelix.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axelix.common.domain.http.HttpPayload;
import com.nucleonforge.axelix.common.domain.http.NoHttpPayload;
import com.nucleonforge.axelix.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axelix.master.api.error.SimpleApiError;
import com.nucleonforge.axelix.master.api.response.EnvironmentFeedResponse;
import com.nucleonforge.axelix.master.api.response.EnvironmentPropertyResponse;
import com.nucleonforge.axelix.master.model.instance.InstanceId;
import com.nucleonforge.axelix.master.service.convert.response.Converter;
import com.nucleonforge.axelix.master.service.transport.EndpointInvoker;

/**
 * The API for managing environment.
 *
 * @since 27.08.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@Tag(
        name = "Environment API",
        description = "The env endpoint provides information about the application’s Environment.")
@RestController
@RequestMapping(path = ApiPaths.EnvironmentApi.MAIN)
public class EnvironmentApi {

    private final EndpointInvoker endpointInvoker;
    private final Converter<EnvironmentFeed, EnvironmentFeedResponse> envConverter;
    private final Converter<EnvironmentProperty, EnvironmentPropertyResponse> envPropertyConverter;

    public EnvironmentApi(
            EndpointInvoker endpointInvoker,
            Converter<EnvironmentFeed, EnvironmentFeedResponse> envConverter,
            Converter<EnvironmentProperty, EnvironmentPropertyResponse> envPropertyConverter) {
        this.endpointInvoker = endpointInvoker;
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
    @GetMapping(path = ApiPaths.EnvironmentApi.FEED)
    public EnvironmentFeedResponse getAllEnvironmentProperties(@PathVariable("instanceId") String instanceId) {
        EnvironmentFeed result = endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.GET_ALL_ENV_PROPERTIES, NoHttpPayload.INSTANCE);
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
        @Parameter(name = "propertyName", description = "Name of the environment property", required = true)
    })
    @GetMapping(path = ApiPaths.EnvironmentApi.PROPERTY)
    public EnvironmentPropertyResponse getSingleEnvironmentProperty(
            @PathVariable("instanceId") String instanceId, @PathVariable("propertyName") String propertyName) {
        HttpPayload payload = new DefaultHttpPayload(Map.of("property.name", propertyName));

        EnvironmentProperty result =
                endpointInvoker.invoke(InstanceId.of(instanceId), ActuatorEndpoints.GET_SINGLE_ENV_PROPERTY, payload);
        return Objects.requireNonNull(envPropertyConverter.convert(result));
    }
}
