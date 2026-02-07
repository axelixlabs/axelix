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

import com.axelixlabs.axelix.common.api.ProfileMutationResult;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.master.api.error.SimpleApiError;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.request.ProfileUpdatedRequest;
import com.axelixlabs.axelix.master.api.external.response.ProfileUpdateResponse;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.convert.response.Converter;
import com.axelixlabs.axelix.master.service.serde.MessageSerializationStrategy;
import com.axelixlabs.axelix.master.service.state.InstanceStatusModifier;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * The API for managing profiles.
 *
 * @since 24.09.2025
 * @author Nikita Kirillov
 */
@Tag(
        name = "Profile Management API",
        description = "Provides operations for managing Spring profiles of application instances.")
@ExternalApiRestController
@RequestMapping(path = ApiPaths.ProfileManagementApi.MAIN)
public class ProfileManagementApi {

    private final EndpointInvoker endpointInvoker;
    private final Converter<ProfileMutationResult, ProfileUpdateResponse> converter;
    private final MessageSerializationStrategy messageSerializationStrategy;
    private final InstanceStatusModifier instanceStatusModifier;

    public ProfileManagementApi(
            EndpointInvoker endpointInvoker,
            Converter<ProfileMutationResult, ProfileUpdateResponse> converter,
            MessageSerializationStrategy messageSerializationStrategy,
            InstanceStatusModifier instanceStatusModifier) {
        this.endpointInvoker = endpointInvoker;
        this.converter = converter;
        this.messageSerializationStrategy = messageSerializationStrategy;
        this.instanceStatusModifier = instanceStatusModifier;
    }

    @Operation(
            summary = "Replaces active profiles for the given application instance.",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ProfileUpdateResponse.class))),
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
    @Parameters(@Parameter(name = "instanceId", description = "Application Instance ID", required = true))
    @PostMapping(path = ApiPaths.ProfileManagementApi.INSTANCE_ID)
    public ProfileUpdateResponse replaceProfile(
            @PathVariable("instanceId") String instanceId, @RequestBody ProfileUpdatedRequest request) {

        HttpPayload payload = HttpPayload.json(messageSerializationStrategy.serialize(request));
        ProfileMutationResult result =
                endpointInvoker.invoke(InstanceId.of(instanceId), ActuatorEndpoints.PROFILE_MANAGEMENT, payload);
        instanceStatusModifier.modifyStatus(instanceId, Instance.InstanceStatus.RELOAD);
        return Objects.requireNonNull(converter.convert(result));
    }
}
