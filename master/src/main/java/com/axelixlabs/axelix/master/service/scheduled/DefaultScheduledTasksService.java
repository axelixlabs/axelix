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
package com.axelixlabs.axelix.master.service.scheduled;

import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.api.external.response.scheduled.ScheduledTaskExecutionHistoryResponse;
import com.axelixlabs.axelix.master.api.external.response.scheduled.ScheduledTasksResponse;
import com.axelixlabs.axelix.master.api.external.response.scheduled.ScheduledTasksResponse.TaskRunnable;
import com.axelixlabs.axelix.master.contract.scheduledtask.ServiceScheduledTasks;
import com.axelixlabs.axelix.master.domain.instance.Instance;
import com.axelixlabs.axelix.master.domain.instance.InstanceId;
import com.axelixlabs.axelix.master.service.serde.DefaultJacksonMessageDeserializationStrategy;
import com.axelixlabs.axelix.master.service.serde.MessageDeserializationStrategy;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.service.transport.BadRequestException;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * Default {@link ScheduledTasksService}.
 *
 * @author Nikita Kirillov
 */
@Service
public class DefaultScheduledTasksService implements ScheduledTasksService {

    private final EndpointInvoker endpointInvoker;
    private final MessageDeserializationStrategy<ServiceScheduledTasks> scheduledTasksDeserializationStrategy;
    private final InstanceRegistry instanceRegistry;
    private final ScheduledTaskExecutionHistoryService scheduledTaskExecutionHistoryService;

    public DefaultScheduledTasksService(
            EndpointInvoker endpointInvoker,
            ObjectMapper objectMapper,
            InstanceRegistry instanceRegistry,
            ScheduledTaskExecutionHistoryService scheduledTaskExecutionHistoryService) {
        this.endpointInvoker = endpointInvoker;
        this.scheduledTasksDeserializationStrategy =
                new DefaultJacksonMessageDeserializationStrategy<>(objectMapper, ServiceScheduledTasks.class);
        this.instanceRegistry = instanceRegistry;
        this.scheduledTaskExecutionHistoryService = scheduledTaskExecutionHistoryService;
    }

    @Override
    public ScheduledTasksResponse getScheduledTasks(InstanceId instanceId) {
        Instance instance = instanceRegistry.getOrThrow(instanceId);

        byte[] body = endpointInvoker.invoke(instanceId, ActuatorEndpoints.GET_SCHEDULED_TASKS, NoHttpPayload.INSTANCE);
        ServiceScheduledTasks tasks = scheduledTasksDeserializationStrategy.deserialize(body);

        return new ScheduledTasksResponse(
                tasks.getCron().stream()
                        .map(task -> new ScheduledTasksResponse.CronTask(
                                new TaskRunnable(task.getRunnable().getTarget()),
                                task.getExpression(),
                                task.getEnabled(),
                                scheduledTaskExecutionHistoryService.findSuccessRate(
                                        instance.applicationId(),
                                        task.getRunnable().getTarget())))
                        .toList(),
                tasks.getFixedDelay().stream()
                        .map(task -> new ScheduledTasksResponse.FixedDelayTask(
                                new TaskRunnable(task.getRunnable().getTarget()),
                                task.getInterval(),
                                task.getInitialDelay(),
                                task.getEnabled(),
                                scheduledTaskExecutionHistoryService.findSuccessRate(
                                        instance.applicationId(),
                                        task.getRunnable().getTarget())))
                        .toList(),
                tasks.getFixedRate().stream()
                        .map(task -> new ScheduledTasksResponse.FixedRateTask(
                                new TaskRunnable(task.getRunnable().getTarget()),
                                task.getInterval(),
                                task.getInitialDelay(),
                                task.getEnabled(),
                                scheduledTaskExecutionHistoryService.findSuccessRate(
                                        instance.applicationId(),
                                        task.getRunnable().getTarget())))
                        .toList(),
                tasks.getCustom().stream()
                        .map(task -> new ScheduledTasksResponse.CustomTask(
                                new TaskRunnable(task.getRunnable().getTarget()),
                                task.getTrigger(),
                                task.getEnabled(),
                                scheduledTaskExecutionHistoryService.findSuccessRate(
                                        instance.applicationId(),
                                        task.getRunnable().getTarget())))
                        .toList());
    }

    @Override
    public ScheduledTaskExecutionHistoryResponse getExecutionHistory(
            InstanceId instanceId, String taskId, int page, int size) {
        if (page < 0 || size < 1) {
            throw new BadRequestException("The page must be at least 0 and the size at least 1");
        }
        Instance instance = instanceRegistry.getOrThrow(instanceId);

        return scheduledTaskExecutionHistoryService.findHistory(instance.applicationId(), taskId, page, size);
    }
}
