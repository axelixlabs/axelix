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

import java.util.Objects;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axelix.common.api.ServiceScheduledTasks;
import com.nucleonforge.axelix.common.domain.http.HttpPayload;
import com.nucleonforge.axelix.common.domain.http.NoHttpPayload;
import com.nucleonforge.axelix.master.api.error.SimpleApiError;
import com.nucleonforge.axelix.master.api.request.scheduled.ScheduledTaskMutationRequest;
import com.nucleonforge.axelix.master.api.request.scheduled.ScheduledTaskToggleRequest;
import com.nucleonforge.axelix.master.api.response.ScheduledTasksResponse;
import com.nucleonforge.axelix.master.model.instance.InstanceId;
import com.nucleonforge.axelix.master.service.convert.response.Converter;
import com.nucleonforge.axelix.master.service.serde.JacksonMessageSerializationStrategy;
import com.nucleonforge.axelix.master.service.transport.scheduled.DisableSingleScheduledTaskEndpointProber;
import com.nucleonforge.axelix.master.service.transport.scheduled.EnableSingleScheduledTaskEndpointProber;
import com.nucleonforge.axelix.master.service.transport.scheduled.GetAllScheduledTasksEndpointProber;
import com.nucleonforge.axelix.master.service.transport.scheduled.MutateScheduledTaskEndpointProber;

/**
 * The API for managing scheduled-tasks (i.e. those that are represented by {@link Scheduled @Scheduled} methods).
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@Tag(
        name = "ScheduledTasks API",
        description = "The scheduled-tasks endpoint provides information about the application’s scheduled tasks.")
@RestController
@RequestMapping(path = ApiPaths.ScheduledTasksApi.MAIN)
public class ScheduledTasksApi {

    private final GetAllScheduledTasksEndpointProber getAllScheduledTasksEndpointProber;
    private final EnableSingleScheduledTaskEndpointProber enableSingleScheduledTaskEndpointProber;
    private final DisableSingleScheduledTaskEndpointProber disableSingleScheduledTaskEndpointProber;
    private final MutateScheduledTaskEndpointProber mutateScheduledTaskEndpointProber;
    private final Converter<ServiceScheduledTasks, ScheduledTasksResponse> converter;
    private final JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy;

    public ScheduledTasksApi(
            GetAllScheduledTasksEndpointProber getAllScheduledTasksEndpointProber,
            EnableSingleScheduledTaskEndpointProber enableSingleScheduledTaskEndpointProber,
            DisableSingleScheduledTaskEndpointProber disableSingleScheduledTaskEndpointProber,
            MutateScheduledTaskEndpointProber mutateScheduledTaskEndpointProber,
            Converter<ServiceScheduledTasks, ScheduledTasksResponse> converter,
            JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy) {
        this.getAllScheduledTasksEndpointProber = getAllScheduledTasksEndpointProber;
        this.enableSingleScheduledTaskEndpointProber = enableSingleScheduledTaskEndpointProber;
        this.disableSingleScheduledTaskEndpointProber = disableSingleScheduledTaskEndpointProber;
        this.mutateScheduledTaskEndpointProber = mutateScheduledTaskEndpointProber;
        this.converter = converter;
        this.jacksonMessageSerializationStrategy = jacksonMessageSerializationStrategy;
    }

    @Operation(
            summary = "Returns details of the application’s scheduled tasks",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ScheduledTasksResponse.class))),
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
    @GetMapping(path = ApiPaths.ScheduledTasksApi.INSTANCE_ID)
    public ScheduledTasksResponse getAllScheduledTasks(@PathVariable("instanceId") String instanceId) {
        ServiceScheduledTasks serviceScheduledTasks =
                getAllScheduledTasksEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
        return Objects.requireNonNull(converter.convert(serviceScheduledTasks));
    }

    @Operation(
            summary =
                    "Allows enabling a scheduled task either according to its configured schedule or forcibly, ignoring the schedule.",
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
    @PostMapping(path = ApiPaths.ScheduledTasksApi.ENABLE_TASK)
    public void enableSingleScheduledTask(
            @PathVariable("instanceId") String instanceId, @RequestBody ScheduledTaskToggleRequest request) {
        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        enableSingleScheduledTaskEndpointProber.invokeNoValue(InstanceId.of(instanceId), payload);
    }

    @Operation(
            summary = "Allows disabling a scheduled task.",
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
    @PostMapping(path = ApiPaths.ScheduledTasksApi.DISABLE_TASK)
    public void disableSingleScheduledTask(
            @PathVariable("instanceId") String instanceId, @RequestBody ScheduledTaskToggleRequest request) {
        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        disableSingleScheduledTaskEndpointProber.invokeNoValue(InstanceId.of(instanceId), payload);
    }

    @Operation(
            summary = "Allows disabling a scheduled task.",
            responses = {
                @ApiResponse(description = "No Content", responseCode = "204"),
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
    @PostMapping(path = ApiPaths.ScheduledTasksApi.INSTANCE_ID)
    public ResponseEntity<Void> mutateCronExpression(
            @PathVariable("instanceId") String instanceId, @RequestBody ScheduledTaskMutationRequest request) {

        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        mutateScheduledTaskEndpointProber.invokeNoValue(InstanceId.of(instanceId), payload);
        return ResponseEntity.noContent().build();
    }
}
