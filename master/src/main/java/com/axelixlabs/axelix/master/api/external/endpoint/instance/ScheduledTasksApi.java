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
package com.axelixlabs.axelix.master.api.external.endpoint.instance;

import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.axelixlabs.axelix.common.contract.scheduledtask.ScheduledTaskCronExpressionModifyRequest;
import com.axelixlabs.axelix.common.contract.scheduledtask.ScheduledTaskDisableRequest;
import com.axelixlabs.axelix.common.contract.scheduledtask.ScheduledTaskEnableRequest;
import com.axelixlabs.axelix.common.contract.scheduledtask.ScheduledTaskExecuteRequest;
import com.axelixlabs.axelix.common.contract.scheduledtask.ScheduledTaskIntervalModifyRequest;
import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.request.ScheduledTaskCronExpressionValidationRequest;
import com.axelixlabs.axelix.master.api.external.response.ScheduledTaskCronExpressionValidationResponse;
import com.axelixlabs.axelix.master.api.external.response.scheduled.ScheduledTaskExecutionHistoryResponse;
import com.axelixlabs.axelix.master.api.external.response.scheduled.ScheduledTasksResponse;
import com.axelixlabs.axelix.master.domain.instance.InstanceId;
import com.axelixlabs.axelix.master.exception.InvalidCronExpressionException;
import com.axelixlabs.axelix.master.service.scheduled.ScheduledTasksService;
import com.axelixlabs.axelix.master.service.serde.JacksonMessageSerializationStrategy;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * The API for managing scheduled-tasks (i.e. those that are represented by {@link Scheduled @Scheduled} methods).
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
@ExternalApiRestController
public class ScheduledTasksApi {

    private final EndpointInvoker endpointInvoker;
    private final JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy;
    private final ScheduledTasksService scheduledTasksService;

    public ScheduledTasksApi(
            EndpointInvoker endpointInvoker,
            JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy,
            ScheduledTasksService scheduledTasksService) {
        this.endpointInvoker = endpointInvoker;
        this.jacksonMessageSerializationStrategy = jacksonMessageSerializationStrategy;
        this.scheduledTasksService = scheduledTasksService;
    }

    @GetMapping(path = ApiPaths.ScheduledTasksApi.INSTANCE_ID)
    public ScheduledTasksResponse getAllScheduledTasks(@PathVariable("instanceId") String instanceId) {
        return scheduledTasksService.getScheduledTasks(InstanceId.of(instanceId));
    }

    /**
     * @param taskId the id of the task.
     * @param page   the zero-based number of the page.
     * @param size   the maximum number of executions on a page, at least 1.
     */
    @GetMapping(path = ApiPaths.ScheduledTasksApi.EXECUTION_HISTORY)
    public ScheduledTaskExecutionHistoryResponse getExecutionHistory(
            @PathVariable("instanceId") String instanceId,
            @RequestParam("taskId") String taskId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "30") int size) {
        return scheduledTasksService.getExecutionHistory(InstanceId.of(instanceId), taskId, page, size);
    }

    @PostMapping(path = ApiPaths.ScheduledTasksApi.ENABLE_TASK)
    public void enableSingleScheduledTask(
            @PathVariable("instanceId") String instanceId, @RequestBody ScheduledTaskEnableRequest request) {
        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.ENABLE_SCHEDULED_TASK, payload);
    }

    @PostMapping(path = ApiPaths.ScheduledTasksApi.DISABLE_TASK)
    public void disableSingleScheduledTask(
            @PathVariable("instanceId") String instanceId, @RequestBody ScheduledTaskDisableRequest request) {
        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.DISABLE_SCHEDULED_TASK, payload);
    }

    @PostMapping(path = ApiPaths.ScheduledTasksApi.VALIDATE_CRON_EXPRESSION)
    public ScheduledTaskCronExpressionValidationResponse validateCronExpression(
            @RequestBody ScheduledTaskCronExpressionValidationRequest request) {

        return new ScheduledTaskCronExpressionValidationResponse(
                CronExpression.isValidExpression(request.cronExpression()));
    }

    @PostMapping(path = ApiPaths.ScheduledTasksApi.MODIFY_CRON_EXPRESSION)
    public ResponseEntity<?> modifyCronExpression(
            @PathVariable("instanceId") String instanceId,
            @RequestBody ScheduledTaskCronExpressionModifyRequest request) {

        if (!CronExpression.isValidExpression(request.getCronExpression())) {
            throw new InvalidCronExpressionException(request.getCronExpression());
        }

        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        endpointInvoker.invokeNoValue(
                InstanceId.of(instanceId), ActuatorEndpoints.MODIFY_CRON_EXPRESSION_SCHEDULED_TASK, payload);

        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = ApiPaths.ScheduledTasksApi.MODIFY_INTERVAL)
    public ResponseEntity<Void> modifyInterval(
            @PathVariable("instanceId") String instanceId, @RequestBody ScheduledTaskIntervalModifyRequest request) {

        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        endpointInvoker.invokeNoValue(
                InstanceId.of(instanceId), ActuatorEndpoints.MODIFY_INTERVAL_SCHEDULED_TASK, payload);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = ApiPaths.ScheduledTasksApi.EXECUTE)
    public ResponseEntity<Void> executeScheduledTask(
            @PathVariable("instanceId") String instanceId, @RequestBody ScheduledTaskExecuteRequest request) {

        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.EXECUTE_SCHEDULED_TASK, payload);
        return ResponseEntity.noContent().build();
    }
}
