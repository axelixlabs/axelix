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
package com.axelixlabs.axelix.master.service.convert;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.common.contract.heartbeatv2.HeartBeatMetadataV2;
import com.axelixlabs.axelix.common.contract.initialregistrationrequest.InitialSelfRegistrationRequest;
import com.axelixlabs.axelix.common.contract.metadata.BasicRegistrationMetadata;
import com.axelixlabs.axelix.common.contract.metadata.ScheduledTaskExecution;
import com.axelixlabs.axelix.master.domain.ScheduledTaskExecutionResult;
import com.axelixlabs.axelix.master.domain.instance.Instance;
import com.axelixlabs.axelix.master.service.discovery.DiscoveredInstanceProfile;

/**
 * Converter that is capable to conver the scheduled task executions reported by the instances into {@link java.util.Set}
 * of {@link ScheduledTaskExecutionResult}.
 *
 * @author Vyacheslav Yanin
 * @author Nikita Kirillov
 */
@Component
public class ScheduledTaskExecutionResultConverter {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskExecutionResultConverter.class);

    public Set<ScheduledTaskExecutionResult> convert(DiscoveredInstanceProfile.Registration discoveredInstanceProfile) {
        Instance instance = discoveredInstanceProfile.instance();
        return convert(discoveredInstanceProfile.metadata(), instance.id().instanceId(), instance.name());
    }

    public Set<ScheduledTaskExecutionResult> convert(InitialSelfRegistrationRequest heartBeatMetadata) {
        return convert(
                heartBeatMetadata.getBasicRegistrationMetadata(),
                heartBeatMetadata.getInstanceId(),
                heartBeatMetadata.getInstanceName());
    }

    public Set<ScheduledTaskExecutionResult> convert(DiscoveredInstanceProfile.Refresh refresh) {
        return convert(refresh.instance(), refresh.masterPing().getScheduledTaskExecutions());
    }

    public Set<ScheduledTaskExecutionResult> convert(Instance instance, HeartBeatMetadataV2 heartBeat) {
        return convert(instance, heartBeat.getScheduledTaskExecutions());
    }

    private Set<ScheduledTaskExecutionResult> convert(
            BasicRegistrationMetadata metadata, String instanceId, String instanceName) {
        return convert(
                metadata.getInsights().getScheduledTaskExecutions(),
                metadata.getGroupId(),
                metadata.getArtifactId(),
                instanceId,
                instanceName);
    }

    private Set<ScheduledTaskExecutionResult> convert(
            Instance instance, @Nullable List<ScheduledTaskExecution> scheduledTaskExecutions) {
        return convert(
                scheduledTaskExecutions,
                instance.applicationId().groupId(),
                instance.applicationId().artifactId(),
                instance.id().instanceId(),
                instance.name());
    }

    private Set<ScheduledTaskExecutionResult> convert(
            @Nullable List<ScheduledTaskExecution> scheduledTaskExecutions,
            String groupId,
            String artifactId,
            String instanceId,
            String instanceName) {
        if (scheduledTaskExecutions == null) {
            return Set.of();
        }

        return scheduledTaskExecutions.stream()
                .map(scheduledTaskExecution ->
                        convert(scheduledTaskExecution, groupId, artifactId, instanceId, instanceName))
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Nullable
    private ScheduledTaskExecutionResult convert(
            ScheduledTaskExecution scheduledTaskExecution,
            String groupId,
            String artifactId,
            String instanceId,
            String instanceName) {
        if (!hasRequiredFields(scheduledTaskExecution)) {
            logger.warn(
                    "The scheduled task {} reported by the instance {} misses required fields. Skipping this execution result.",
                    scheduledTaskExecution.getTaskId(),
                    instanceId);
            return null;
        }

        Instant startedAt;
        try {
            startedAt = Instant.parse(scheduledTaskExecution.getStartedAt());
        } catch (DateTimeParseException e) {
            logger.warn(
                    "Unable to parse the startedAt timestamp '{}' of the scheduled task {} reported by the instance {}. Skipping this execution result.",
                    scheduledTaskExecution.getStartedAt(),
                    scheduledTaskExecution.getTaskId(),
                    instanceId,
                    e);
            return null;
        }

        return new ScheduledTaskExecutionResult(
                id(instanceId, scheduledTaskExecution.getTaskId(), startedAt),
                groupId,
                artifactId,
                instanceId,
                instanceName,
                scheduledTaskExecution.getTaskId(),
                startedAt,
                scheduledTaskExecution.getDurationMillis(),
                scheduledTaskExecution.getSuccess(),
                Objects.requireNonNull(scheduledTaskExecution.getLaunchType()),
                scheduledTaskExecution.getErrorType(),
                scheduledTaskExecution.getErrorMessage());
    }

    private static boolean hasRequiredFields(ScheduledTaskExecution execution) {
        return execution.getTaskId() != null
                && execution.getStartedAt() != null
                && execution.getDurationMillis() != null
                && execution.getSuccess() != null
                && execution.getLaunchType() != null;
    }

    /**
     * @return the id of the execution, which is the same every time the starter reports the execution again.
     *     Epoch milliseconds keep two runs of the same task in one second distinct.
     */
    private static String id(String instanceId, String taskId, Instant startedAt) {
        String execution = instanceId + taskId + startedAt.toEpochMilli();
        return UUID.nameUUIDFromBytes(execution.getBytes(StandardCharsets.UTF_8))
                .toString();
    }
}
