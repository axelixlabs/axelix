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

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.axelixlabs.axelix.common.api.ServiceScheduledTasks;
import com.axelixlabs.axelix.common.api.scheduledtask.ScheduledTaskCronExpressionModifyRequest;
import com.axelixlabs.axelix.common.api.scheduledtask.ScheduledTaskExecuteRequest;
import com.axelixlabs.axelix.common.api.scheduledtask.ScheduledTaskIntervalModifyRequest;
import com.axelixlabs.axelix.common.api.scheduledtask.ScheduledTaskToggleRequest;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.api.error.SimpleApiError;
import com.axelixlabs.axelix.master.api.error.handle.ApiErrorCodes;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.request.ScheduledTaskCronExpressionValidationRequest;
import com.axelixlabs.axelix.master.api.external.response.ScheduledTaskCronExpressionValidationResponse;
import com.axelixlabs.axelix.master.api.external.swagger.DefaultApiResponse;
import com.axelixlabs.axelix.master.api.external.swagger.InstanceIdParameter;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.serde.JacksonMessageSerializationStrategy;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * The API for managing scheduled-tasks (i.e. those that are represented by {@link Scheduled @Scheduled} methods).
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@Tag(
        name = "ScheduledTasks API",
        description = "The scheduled-tasks endpoint provides information about the application’s scheduled tasks.")
@ExternalApiRestController
@RequestMapping(path = ApiPaths.ScheduledTasksApi.MAIN)
public class ScheduledTasksApi {

    private final EndpointInvoker endpointInvoker;
    private final JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy;

    public ScheduledTasksApi(
            EndpointInvoker endpointInvoker, JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy) {
        this.endpointInvoker = endpointInvoker;
        this.jacksonMessageSerializationStrategy = jacksonMessageSerializationStrategy;
    }

    @DefaultApiResponse(summary = "Returns the feed of the application’s scheduled tasks")
    @ApiResponse(
            description = "OK",
            responseCode = "200",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServiceScheduledTasks.class)))
    @InstanceIdParameter
    @GetMapping(path = ApiPaths.ScheduledTasksApi.INSTANCE_ID, produces = MediaType.APPLICATION_JSON_VALUE)
    public byte[] getAllScheduledTasks(@PathVariable("instanceId") String instanceId) {
        return endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.GET_SCHEDULED_TASKS, NoHttpPayload.INSTANCE);
    }

    @DefaultApiResponse(
            summary =
                    "Allows enabling a scheduled task either according to its configured schedule or forcibly, ignoring the schedule.")
    @ApiResponse(description = "OK", responseCode = "200")
    @InstanceIdParameter
    @PostMapping(path = ApiPaths.ScheduledTasksApi.ENABLE_TASK)
    public void enableSingleScheduledTask(
            @PathVariable("instanceId") String instanceId, @RequestBody ScheduledTaskToggleRequest request) {
        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.ENABLE_SCHEDULED_TASK, payload);
    }

    @DefaultApiResponse(summary = "Allows disabling a scheduled task.")
    @ApiResponse(description = "OK", responseCode = "200")
    @InstanceIdParameter
    @PostMapping(path = ApiPaths.ScheduledTasksApi.DISABLE_TASK)
    public void disableSingleScheduledTask(
            @PathVariable("instanceId") String instanceId, @RequestBody ScheduledTaskToggleRequest request) {
        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.DISABLE_SCHEDULED_TASK, payload);
    }

    @DefaultApiResponse(summary = "Endpoint to validate cron expression")
    @ApiResponse(
            description = "OK",
            responseCode = "200",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ScheduledTaskCronExpressionValidationResponse.class)))
    @PostMapping(path = ApiPaths.ScheduledTasksApi.VALIDATE_CRON_EXPRESSION)
    public ScheduledTaskCronExpressionValidationResponse validateCronExpression(
            @RequestBody ScheduledTaskCronExpressionValidationRequest request) {
        return new ScheduledTaskCronExpressionValidationResponse(
                CronExpression.isValidExpression(request.cronExpression()));
    }

    @DefaultApiResponse(summary = "Endpoint allows modification of the cron expression for a scheduled task.")
    @ApiResponse(description = "Cron expression successfully modified", responseCode = "204")
    @ApiResponse(description = "Cron expression is invalid", responseCode = "400")
    @InstanceIdParameter
    @PostMapping(path = ApiPaths.ScheduledTasksApi.MODIFY_CRON_EXPRESSION)
    public ResponseEntity<?> modifyCronExpression(
            @PathVariable("instanceId") String instanceId,
            @RequestBody ScheduledTaskCronExpressionModifyRequest request) {

        if (!CronExpression.isValidExpression(request.getCronExpression())) {
            // TODO: Again, that is bad to pass status code to SimpleApiError
            return ResponseEntity.badRequest()
                    .body(new SimpleApiError(ApiErrorCodes.INVALID_CRON_EXPRESSION.getErrorCode(), 400));
        }

        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        endpointInvoker.invokeNoValue(
                InstanceId.of(instanceId), ActuatorEndpoints.MODIFY_CRON_EXPRESSION_SCHEDULED_TASK, payload);
        return ResponseEntity.noContent().build();
    }

    @DefaultApiResponse(summary = "Endpoint allows modification of the interval for a scheduled task.")
    @ApiResponse(description = "No Content", responseCode = "204")
    @InstanceIdParameter
    @PostMapping(path = ApiPaths.ScheduledTasksApi.MODIFY_INTERVAL)
    public ResponseEntity<Void> modifyInterval(
            @PathVariable("instanceId") String instanceId, @RequestBody ScheduledTaskIntervalModifyRequest request) {

        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        endpointInvoker.invokeNoValue(
                InstanceId.of(instanceId), ActuatorEndpoints.MODIFY_INTERVAL_SCHEDULED_TASK, payload);
        return ResponseEntity.noContent().build();
    }

    @DefaultApiResponse(summary = "Endpoint allows forcing a scheduled task to run now.")
    @ApiResponse(description = "No Content", responseCode = "204")
    @InstanceIdParameter
    @PostMapping(path = ApiPaths.ScheduledTasksApi.EXECUTE)
    public ResponseEntity<Void> executeScheduledTask(
            @PathVariable("instanceId") String instanceId, @RequestBody ScheduledTaskExecuteRequest request) {

        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.EXECUTE_SCHEDULED_TASK, payload);
        return ResponseEntity.noContent().build();
    }
}
