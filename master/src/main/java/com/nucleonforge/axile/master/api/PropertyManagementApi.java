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
package com.nucleonforge.axile.master.api;

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
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.request.PropertyUpdatedRequest;
import com.nucleonforge.axile.master.model.instance.Instance;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.serde.MessageSerializationStrategy;
import com.nucleonforge.axile.master.service.state.InstanceStatusModifier;
import com.nucleonforge.axile.master.service.transport.PropertyManagementEndpointProber;

/**
 * The API for managing properties.
 *
 * @since 25.09.2025
 * @author Nikita Kirillov
 */
@Tag(
        name = "Property Management API",
        description = "Provides operations for managing Spring properties of application instances.")
@RestController
@RequestMapping(path = ApiPaths.PropertyManagementApi.MAIN)
public class PropertyManagementApi {

    private final PropertyManagementEndpointProber propertyManagementEndpointProber;
    private final MessageSerializationStrategy messageSerializationStrategy;
    private final InstanceStatusModifier instanceStatusModifier;

    public PropertyManagementApi(
            PropertyManagementEndpointProber profileManagementEndpointProber,
            MessageSerializationStrategy messageSerializationStrategy,
            InstanceStatusModifier instanceStatusModifier) {
        this.propertyManagementEndpointProber = profileManagementEndpointProber;
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
        propertyManagementEndpointProber.invokeNoValue(InstanceId.of(instanceId), payload);
        instanceStatusModifier.modifyStatus(instanceId, Instance.InstanceStatus.RELOAD);
        return ResponseEntity.noContent().build();
    }
}
