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
import com.nucleonforge.axelix.master.api.request.scheduled.ScheduledTaskCronExpressionModifyRequest;
import com.nucleonforge.axelix.master.api.request.scheduled.ScheduledTaskExecuteRequest;
import com.nucleonforge.axelix.master.api.request.scheduled.ScheduledTaskIntervalModifyRequest;
import com.nucleonforge.axelix.master.api.request.scheduled.ScheduledTaskToggleRequest;
import com.nucleonforge.axelix.master.api.response.ScheduledTasksResponse;
import com.nucleonforge.axelix.master.model.instance.InstanceId;
import com.nucleonforge.axelix.master.service.convert.response.Converter;
import com.nucleonforge.axelix.master.service.serde.JacksonMessageSerializationStrategy;
import com.nucleonforge.axelix.master.service.transport.scheduled.DisableSingleScheduledTaskEndpointProber;
import com.nucleonforge.axelix.master.service.transport.scheduled.EnableSingleScheduledTaskEndpointProber;
import com.nucleonforge.axelix.master.service.transport.scheduled.ExecuteScheduledTaskEndpointProber;
import com.nucleonforge.axelix.master.service.transport.scheduled.GetAllScheduledTasksEndpointProber;
import com.nucleonforge.axelix.master.service.transport.scheduled.ModifyCronExpressionScheduledTaskEndpointProber;
import com.nucleonforge.axelix.master.service.transport.scheduled.ModifyIntervalScheduledTaskEndpointProber;

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
    private final ModifyCronExpressionScheduledTaskEndpointProber modifyCronExpressionScheduledTaskEndpointProber;
    private final ModifyIntervalScheduledTaskEndpointProber modifyIntervalScheduledTaskEndpointProber;
    private final ExecuteScheduledTaskEndpointProber executeScheduledTaskEndpointProber;
    private final Converter<ServiceScheduledTasks, ScheduledTasksResponse> converter;
    private final JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy;

    public ScheduledTasksApi(
            GetAllScheduledTasksEndpointProber getAllScheduledTasksEndpointProber,
            EnableSingleScheduledTaskEndpointProber enableSingleScheduledTaskEndpointProber,
            DisableSingleScheduledTaskEndpointProber disableSingleScheduledTaskEndpointProber,
            ModifyCronExpressionScheduledTaskEndpointProber modifyCronExpressionScheduledTaskEndpointProber,
            ModifyIntervalScheduledTaskEndpointProber modifyIntervalScheduledTaskEndpointProber,
            ExecuteScheduledTaskEndpointProber executeScheduledTaskEndpointProber,
            Converter<ServiceScheduledTasks, ScheduledTasksResponse> converter,
            JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy) {
        this.getAllScheduledTasksEndpointProber = getAllScheduledTasksEndpointProber;
        this.enableSingleScheduledTaskEndpointProber = enableSingleScheduledTaskEndpointProber;
        this.disableSingleScheduledTaskEndpointProber = disableSingleScheduledTaskEndpointProber;
        this.modifyCronExpressionScheduledTaskEndpointProber = modifyCronExpressionScheduledTaskEndpointProber;
        this.modifyIntervalScheduledTaskEndpointProber = modifyIntervalScheduledTaskEndpointProber;
        this.executeScheduledTaskEndpointProber = executeScheduledTaskEndpointProber;
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
            summary = "Endpoint allows modification of the cron expression for a scheduled task.",
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
    @PostMapping(path = ApiPaths.ScheduledTasksApi.MODIFY_CRON_EXPRESSION)
    public ResponseEntity<Void> modifyCronExpression(
            @PathVariable("instanceId") String instanceId,
            @RequestBody ScheduledTaskCronExpressionModifyRequest request) {

        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        modifyCronExpressionScheduledTaskEndpointProber.invokeNoValue(InstanceId.of(instanceId), payload);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Endpoint allows modification of the interval for a scheduled task.",
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
    @PostMapping(path = ApiPaths.ScheduledTasksApi.MODIFY_INTERVAL)
    public ResponseEntity<Void> modifyInterval(
            @PathVariable("instanceId") String instanceId, @RequestBody ScheduledTaskIntervalModifyRequest request) {

        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        modifyIntervalScheduledTaskEndpointProber.invokeNoValue(InstanceId.of(instanceId), payload);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Endpoint allows forcing a scheduled task to run now.",
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
    @PostMapping(path = ApiPaths.ScheduledTasksApi.EXECUTE)
    public ResponseEntity<Void> executeScheduledTask(
            @PathVariable("instanceId") String instanceId, @RequestBody ScheduledTaskExecuteRequest request) {

        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        executeScheduledTaskEndpointProber.invokeNoValue(InstanceId.of(instanceId), payload);
        return ResponseEntity.noContent().build();
    }
}
