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

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.master.contract.heartbeat.HeartBeatMetadata;
import com.axelixlabs.axelix.master.contract.metadata.BasicRegistrationMetadata;
import com.axelixlabs.axelix.master.contract.metadata.ScheduledTaskExecution;
import com.axelixlabs.axelix.master.domain.ScheduledTaskExecutionResult;
import com.axelixlabs.axelix.master.service.discovery.DiscoveredInstanceProfile;

/**
 * Converter that is capable to conver the {@link BasicRegistrationMetadata} and {@link DiscoveredInstanceProfile}
 * into {@link java.util.Set} of {@link ScheduledTaskExecutionResult}.
 *
 * @author Vyacheslav Yanin
 */
@Component
public class ScheduledTaskExecutionResultConverter {

    public Set<ScheduledTaskExecutionResult> convert(DiscoveredInstanceProfile discoveredInstanceProfile) {
        return convert(
                discoveredInstanceProfile.metadata(),
                discoveredInstanceProfile.instance().id().instanceId());
    }

    public Set<ScheduledTaskExecutionResult> convert(HeartBeatMetadata heartBeatMetadata) {
        return convert(heartBeatMetadata.getBasicRegistrationMetadata(), heartBeatMetadata.getInstanceId());
    }

    private Set<ScheduledTaskExecutionResult> convert(BasicRegistrationMetadata metadata, String instanceId) {
        String groupId = metadata.getGroupId();
        String artifactId = metadata.getArtifactId();
        List<ScheduledTaskExecution> scheduledTaskExecutions =
                metadata.getInsights().getScheduledTaskExecutions();

        if (scheduledTaskExecutions == null) {
            return Set.of();
        }

        return scheduledTaskExecutions.stream()
                .map(scheduledTaskExecution -> new ScheduledTaskExecutionResult(
                        UUID.randomUUID().toString(),
                        groupId,
                        artifactId,
                        instanceId,
                        scheduledTaskExecution.getTaskId(),
                        Instant.parse(scheduledTaskExecution.getStartedAt()),
                        scheduledTaskExecution.getDurationMillis(),
                        scheduledTaskExecution.getSuccess(),
                        scheduledTaskExecution.getErrorType(),
                        scheduledTaskExecution.getErrorMessage()))
                .collect(Collectors.toUnmodifiableSet());
    }
}
