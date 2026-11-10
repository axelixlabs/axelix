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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;
import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.contract.scheduledtask.ScheduledTaskRunnable;
import com.axelixlabs.axelix.common.contract.scheduledtask.ServiceScheduledTasks;
import com.axelixlabs.axelix.master.api.external.response.scheduled.ScheduledTaskExecutionHistoryResponse;
import com.axelixlabs.axelix.master.api.external.response.scheduled.ScheduledTasksResponse;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.domain.http.NoHttpPayload;
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

    private static final Pattern METHOD_TASK_ID = Pattern.compile(
            "^([\\p{javaJavaIdentifierStart}][\\p{javaJavaIdentifierPart}]*(?:\\.[\\p{javaJavaIdentifierStart}][\\p{javaJavaIdentifierPart}]*)*)\\.([\\p{javaJavaIdentifierStart}][\\p{javaJavaIdentifierPart}]*)(?:#(?:cron|fixedDelay|fixedRate)[0-9]+)?$");

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
        Set<String> taskIds = new HashSet<>();
        tasks.getCron().forEach(task -> taskIds.add(task.getRunnable().getTarget()));
        tasks.getFixedDelay().forEach(task -> taskIds.add(task.getRunnable().getTarget()));
        tasks.getFixedRate().forEach(task -> taskIds.add(task.getRunnable().getTarget()));
        tasks.getCustom().forEach(task -> taskIds.add(task.getRunnable().getTarget()));

        Map<String, Double> successRates =
                scheduledTaskExecutionHistoryService.findSuccessRates(instance.applicationId(), taskIds);

        return new ScheduledTasksResponse(
                tasks.getCron().stream()
                        .map(task -> new ScheduledTasksResponse.CronTask(
                                task.getRunnable().getTarget(),
                                className(task.getRunnable()),
                                methodName(task.getRunnable()),
                                task.getExpression(),
                                task.getEnabled(),
                                successRates.get(task.getRunnable().getTarget())))
                        .toList(),
                tasks.getFixedDelay().stream()
                        .map(task -> new ScheduledTasksResponse.FixedDelayTask(
                                task.getRunnable().getTarget(),
                                className(task.getRunnable()),
                                methodName(task.getRunnable()),
                                task.getInterval(),
                                task.getInitialDelay(),
                                task.getEnabled(),
                                successRates.get(task.getRunnable().getTarget())))
                        .toList(),
                tasks.getFixedRate().stream()
                        .map(task -> new ScheduledTasksResponse.FixedRateTask(
                                task.getRunnable().getTarget(),
                                className(task.getRunnable()),
                                methodName(task.getRunnable()),
                                task.getInterval(),
                                task.getInitialDelay(),
                                task.getEnabled(),
                                successRates.get(task.getRunnable().getTarget())))
                        .toList(),
                tasks.getCustom().stream()
                        .map(task -> new ScheduledTasksResponse.CustomTask(
                                task.getRunnable().getTarget(),
                                className(task.getRunnable()),
                                methodName(task.getRunnable()),
                                task.getTrigger(),
                                task.getEnabled(),
                                successRates.get(task.getRunnable().getTarget())))
                        .toList());
    }

    private static @Nullable String className(ScheduledTaskRunnable runnable) {
        if (runnable.getClassName() != null) {
            return runnable.getClassName();
        }
        Matcher matcher = METHOD_TASK_ID.matcher(runnable.getTarget());
        return matcher.matches() ? matcher.group(1) : null;
    }

    private static @Nullable String methodName(ScheduledTaskRunnable runnable) {
        if (runnable.getMethodName() != null) {
            return runnable.getMethodName();
        }
        Matcher matcher = METHOD_TASK_ID.matcher(runnable.getTarget());
        return matcher.matches() ? matcher.group(2) : null;
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
