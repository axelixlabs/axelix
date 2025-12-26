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

import com.nucleonforge.axile.common.api.ThreadDumpFeed;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.response.ThreadDumpFeedResponse;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.convert.response.Converter;
import com.nucleonforge.axile.master.service.transport.threaddump.ThreadDumpDisableContentionMonitoringProber;
import com.nucleonforge.axile.master.service.transport.threaddump.ThreadDumpEnableContentionMonitoringProber;
import com.nucleonforge.axile.master.service.transport.threaddump.ThreadDumpEndpointProber;

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

    private final ThreadDumpEndpointProber threadDumpEndpointProber;
    private final ThreadDumpEnableContentionMonitoringProber enableContentionMonitoringProber;
    private final ThreadDumpDisableContentionMonitoringProber disableContentionMonitoringProber;
    private final Converter<ThreadDumpFeed, ThreadDumpFeedResponse> converter;

    public ThreadDumpApi(
            ThreadDumpEndpointProber threadDumpEndpointProber,
            ThreadDumpEnableContentionMonitoringProber enableContentionMonitoringProber,
            ThreadDumpDisableContentionMonitoringProber disableContentionMonitoringProber,
            Converter<ThreadDumpFeed, ThreadDumpFeedResponse> converter) {
        this.threadDumpEndpointProber = threadDumpEndpointProber;
        this.enableContentionMonitoringProber = enableContentionMonitoringProber;
        this.disableContentionMonitoringProber = disableContentionMonitoringProber;
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
        ThreadDumpFeed result = threadDumpEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);

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
        enableContentionMonitoringProber.invokeNoValue(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
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
        disableContentionMonitoringProber.invokeNoValue(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
    }
}
