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
package com.axelixlabs.axelix.master.api.external.endpoint;

import java.util.Objects;

import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.links.Link;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.axelixlabs.axelix.common.api.ConditionsFeed;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.api.error.SimpleApiError;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.response.ConditionsFeedResponse;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.convert.response.Converter;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * The API for managing conditions.
 *
 * @since 16.10.2025
 * @author Nikita Kirillov
 */
@Tag(
        name = "Conditions API",
        description =
                "The conditions endpoint provides information about the application's auto-configuration conditions evaluation.")
@ExternalApiRestController
@RequestMapping(path = ApiPaths.ConditionsApi.MAIN)
public class ConditionsApi {

    private final EndpointInvoker endpointInvoker;
    private final Converter<ConditionsFeed, ConditionsFeedResponse> converter;

    public ConditionsApi(EndpointInvoker endpointInvoker, Converter<ConditionsFeed, ConditionsFeedResponse> converter) {
        this.endpointInvoker = endpointInvoker;
        this.converter = converter;
    }

    @Operation(
            summary = "Returns conditions feed for the given instance",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Conditions",
                                    description =
                                            "https://docs.spring.io/spring-boot/api/rest/actuator/conditions.html")
                        },
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ConditionsFeedResponse.class))),
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
    @GetMapping(path = ApiPaths.ConditionsApi.FEED)
    public ConditionsFeedResponse getConditionsFeed(@PathVariable("instanceId") String instanceId) {
        ConditionsFeed result = endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.GET_CONDITIONS, NoHttpPayload.INSTANCE);
        return Objects.requireNonNull(converter.convert(result));
    }
}
