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

import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.master.api.error.SimpleApiError;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.request.PropertyUpdatedRequest;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.serde.MessageSerializationStrategy;
import com.axelixlabs.axelix.master.service.state.InstanceStatusModifier;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * The API for managing properties.
 *
 * @since 25.09.2025
 * @author Nikita Kirillov
 */
@Tag(
        name = "Property Management API",
        description = "Provides operations for managing Spring properties of application instances.")
@ExternalApiRestController
@RequestMapping(path = ApiPaths.PropertyManagementApi.MAIN)
public class PropertyManagementApi {

    private final EndpointInvoker endpointInvoker;
    private final MessageSerializationStrategy messageSerializationStrategy;
    private final InstanceStatusModifier instanceStatusModifier;

    public PropertyManagementApi(
            EndpointInvoker endpointInvoker,
            MessageSerializationStrategy messageSerializationStrategy,
            InstanceStatusModifier instanceStatusModifier) {
        this.endpointInvoker = endpointInvoker;
        this.messageSerializationStrategy = messageSerializationStrategy;
        this.instanceStatusModifier = instanceStatusModifier;
    }

    @Operation(
            summary = "Updates a configuration property for the given application instance.",
            responses = {
                @ApiResponse(description = "OK", responseCode = "204"),
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
    @Parameters({@Parameter(name = "instanceId", description = "Application Instance ID", required = true)})
    @PostMapping(path = ApiPaths.PropertyManagementApi.INSTANCE_ID)
    public ResponseEntity<Void> changePropertyValue(
            @PathVariable("instanceId") String instanceId, @RequestBody PropertyUpdatedRequest request) {

        HttpPayload payload = HttpPayload.json(messageSerializationStrategy.serialize(request));
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.PROPERTY_MANAGEMENT, payload);
        instanceStatusModifier.modifyStatus(instanceId, Instance.InstanceStatus.RELOAD);
        return ResponseEntity.noContent().build();
    }
}
