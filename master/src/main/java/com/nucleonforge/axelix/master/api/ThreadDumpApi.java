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

import java.util.Objects;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.links.Link;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axelix.common.api.ThreadDumpFeed;
import com.nucleonforge.axelix.common.domain.http.NoHttpPayload;
import com.nucleonforge.axelix.common.domain.spring.actuator.ActuatorEndpoints;
import com.nucleonforge.axelix.master.api.error.SimpleApiError;
import com.nucleonforge.axelix.master.api.response.ThreadDumpFeedResponse;
import com.nucleonforge.axelix.master.model.instance.InstanceId;
import com.nucleonforge.axelix.master.service.convert.response.Converter;
import com.nucleonforge.axelix.master.service.transport.EndpointInvoker;

/**
 * The API for thread dump.
 *
 * @since 18.11.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@Tag(
        name = "Thread Dump API",
        description = "The threaddump endpoint provides access to the thread dump of the application’s JVM.")
@RestController
@RequestMapping(path = ApiPaths.ThreadDumpApi.MAIN)
public class ThreadDumpApi {

    private final EndpointInvoker endpointInvoker;
    private final Converter<ThreadDumpFeed, ThreadDumpFeedResponse> converter;

    public ThreadDumpApi(EndpointInvoker endpointInvoker, Converter<ThreadDumpFeed, ThreadDumpFeedResponse> converter) {
        this.endpointInvoker = endpointInvoker;
        this.converter = converter;
    }

    @Operation(
            summary = "The threaddump endpoint provides a thread dump from the application’s JVM.",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Thread Dump",
                                    description =
                                            "https://docs.spring.io/spring-boot/api/rest/actuator/threaddump.html")
                        },
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ThreadDumpFeedResponse.class))),
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
    @GetMapping(ApiPaths.ThreadDumpApi.INSTANCE_ID)
    public ThreadDumpFeedResponse getThreadDump(@PathVariable("instanceId") String instanceId) {
        ThreadDumpFeed result = endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.GET_THREAD_DUMP, NoHttpPayload.INSTANCE);

        return Objects.requireNonNull(converter.convert(result));
    }

    @Operation(
            summary = "Endpoint allows enabling thread contention monitoring.",
            responses = {
                @ApiResponse(description = "OK", responseCode = "200"),
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
    @PostMapping(ApiPaths.ThreadDumpApi.ENABLE_CONTENTION_MONITORING)
    public void enableContentionMonitoring(@PathVariable("instanceId") String instanceId) {
        endpointInvoker.invokeNoValue(
                InstanceId.of(instanceId),
                ActuatorEndpoints.THREAD_DUMP_ENABLE_CONTENTION_MONITORING,
                NoHttpPayload.INSTANCE);
    }

    @Operation(
            summary = "Endpoint allows disabling thread contention monitoring.",
            responses = {
                @ApiResponse(description = "OK", responseCode = "200"),
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
    @PostMapping(ApiPaths.ThreadDumpApi.DISABLE_CONTENTION_MONITORING)
    public void disableContentionMonitoring(@PathVariable("instanceId") String instanceId) {
        endpointInvoker.invokeNoValue(
                InstanceId.of(instanceId),
                ActuatorEndpoints.THREAD_DUMP_DISABLE_CONTENTION_MONITORING,
                NoHttpPayload.INSTANCE);
    }
}
