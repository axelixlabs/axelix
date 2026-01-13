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
package com.nucleonforge.axelix.master.api;

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
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axelix.common.api.gclog.GcLogEnableRequest;
import com.nucleonforge.axelix.common.api.gclog.GcLogStatusResponse;
import com.nucleonforge.axelix.common.domain.http.HttpPayload;
import com.nucleonforge.axelix.common.domain.http.NoHttpPayload;
import com.nucleonforge.axelix.master.api.error.SimpleApiError;
import com.nucleonforge.axelix.master.model.instance.InstanceId;
import com.nucleonforge.axelix.master.service.serde.JacksonMessageSerializationStrategy;
import com.nucleonforge.axelix.master.service.transport.gclog.DisableGcLoggingEndpointProber;
import com.nucleonforge.axelix.master.service.transport.gclog.EnableGcLoggingEndpointProber;
import com.nucleonforge.axelix.master.service.transport.gclog.GcLogFileEndpointProber;
import com.nucleonforge.axelix.master.service.transport.gclog.GcLogStatusEndpointProber;
import com.nucleonforge.axelix.master.service.transport.gclog.GcTriggerEndpointProber;

/**
 * The API for garbage-collector.
 *
 * @since 10.01.2026
 * @author Nikita Kirillov
 */
@Tag(name = "GC Log File API", description = "API for managing GC logging and retrieving GC logs")
@RestController
@RequestMapping(path = ApiPaths.GcLogFileApi.MAIN)
public class GcLogFileApi {

    private final GcLogFileEndpointProber gcLogFileEndpointProber;
    private final GcTriggerEndpointProber gcTriggerEndpointProber;
    private final GcLogStatusEndpointProber gcLogStatusEndpointProber;
    private final EnableGcLoggingEndpointProber enableGcLoggingEndpointProber;
    private final DisableGcLoggingEndpointProber disableGcLoggingEndpointProber;
    private final JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy;

    public GcLogFileApi(
            GcLogFileEndpointProber gcLogFileEndpointProber,
            GcTriggerEndpointProber gcTriggerEndpointProber,
            GcLogStatusEndpointProber gcLogStatusEndpointProber,
            EnableGcLoggingEndpointProber enableGcLoggingEndpointProber,
            DisableGcLoggingEndpointProber disableGcLoggingEndpointProber,
            JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy) {
        this.gcLogFileEndpointProber = gcLogFileEndpointProber;
        this.gcTriggerEndpointProber = gcTriggerEndpointProber;
        this.gcLogStatusEndpointProber = gcLogStatusEndpointProber;
        this.enableGcLoggingEndpointProber = enableGcLoggingEndpointProber;
        this.disableGcLoggingEndpointProber = disableGcLoggingEndpointProber;
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
        return gcLogFileEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
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
        return gcLogStatusEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
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
        gcTriggerEndpointProber.invokeNoValue(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
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
        enableGcLoggingEndpointProber.invokeNoValue(InstanceId.of(instanceId), httpPayload);
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
        disableGcLoggingEndpointProber.invokeNoValue(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
    }
}
