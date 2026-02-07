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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.axelixlabs.axelix.common.api.gclog.GcLogEnableRequest;
import com.axelixlabs.axelix.common.api.gclog.GcLogStatusResponse;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.api.error.SimpleApiError;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.serde.JacksonMessageSerializationStrategy;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * The API for garbage-collector.
 *
 * @since 10.01.2026
 * @author Nikita Kirillov
 */
@Tag(name = "GC Log File API", description = "API for managing GC logging and retrieving GC logs")
@ExternalApiRestController
@RequestMapping(path = ApiPaths.GcLogFileApi.MAIN)
public class GcLogFileApi {

    private final EndpointInvoker endpointInvoker;
    private final JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy;

    public GcLogFileApi(
            EndpointInvoker endpointInvoker, JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy) {
        this.endpointInvoker = endpointInvoker;
        this.jacksonMessageSerializationStrategy = jacksonMessageSerializationStrategy;
    }

    @Operation(
            summary = "Get GC log file for the given instance",
            description = "Returns GC log file as plain text",
            responses = {
                @ApiResponse(
                        description = "GC log file content",
                        responseCode = "200",
                        content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
                @ApiResponse(
                        description = "Bad Request - instance not found",
                        responseCode = "400",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class))),
                @ApiResponse(
                        description = "GC logging not enabled",
                        responseCode = "404",
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
    @GetMapping(path = ApiPaths.GcLogFileApi.INSTANCE_ID, produces = MediaType.TEXT_PLAIN_VALUE)
    public Resource getGcLogFile(@PathVariable("instanceId") String instanceId) {
        return endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.GET_GC_LOG_FILE, NoHttpPayload.INSTANCE);
    }

    @Operation(
            summary = "Get GC logging status",
            description = "Returns current GC logging status for the instance",
            responses = {
                @ApiResponse(
                        description = "GC logging status",
                        responseCode = "200",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = GcLogStatusResponse.class))),
                @ApiResponse(
                        description = "Bad Request - instance not found",
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
    @GetMapping(path = ApiPaths.GcLogFileApi.STATUS_GC_LOGGING)
    public GcLogStatusResponse getStatus(@PathVariable("instanceId") String instanceId) {
        return endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.GET_STATUS_GC_LOGGING, NoHttpPayload.INSTANCE);
    }

    @Operation(
            summary = "Trigger garbage collection",
            description = "Manually triggers garbage collection on the target instance",
            responses = {
                @ApiResponse(description = "GC triggered successfully", responseCode = "200"),
                @ApiResponse(
                        description = "Bad Request - instance not found",
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
    @PostMapping(path = ApiPaths.GcLogFileApi.TRIGGER_GC)
    public void triggerGc(@PathVariable("instanceId") String instanceId) {
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.GC_TRIGGER, NoHttpPayload.INSTANCE);
    }

    @Operation(
            summary = "Enable GC logging",
            description = "Enables GC logging with specified log level",
            responses = {
                @ApiResponse(description = "GC logging enabled successfully", responseCode = "200"),
                @ApiResponse(
                        description = "Bad Request - instance not found or invalid log level",
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
    @PostMapping(path = ApiPaths.GcLogFileApi.ENABLE_GC_LOGGING)
    public void enableGcLogging(
            @PathVariable("instanceId") String instanceId, @RequestBody GcLogEnableRequest request) {
        HttpPayload httpPayload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.ENABLE_GC_LOGGING, httpPayload);
    }

    @Operation(
            summary = "Disable GC logging",
            description = "Disables GC logging for the instance",
            responses = {
                @ApiResponse(description = "GC logging disabled successfully", responseCode = "200"),
                @ApiResponse(
                        description = "Bad Request - instance not found",
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
    @PostMapping(path = ApiPaths.GcLogFileApi.DISABLE_GC_LOGGING)
    public void disableGcLogging(@PathVariable("instanceId") String instanceId) {
        endpointInvoker.invokeNoValue(
                InstanceId.of(instanceId), ActuatorEndpoints.DISABLE_GC_LOGGING, NoHttpPayload.INSTANCE);
    }
}
