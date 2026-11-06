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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.master.api.external.response.scheduled.ScheduledTaskExecutionHistoryResponse;
import com.axelixlabs.axelix.master.api.external.response.scheduled.ScheduledTaskExecutionHistoryResponse.Execution;
import com.axelixlabs.axelix.master.api.external.response.scheduled.ScheduledTaskExecutionHistoryResponse.FailuresByErrorType;
import com.axelixlabs.axelix.master.api.external.response.scheduled.ScheduledTaskExecutionHistoryResponse.Summary;
import com.axelixlabs.axelix.master.domain.ApplicationId;
import com.axelixlabs.axelix.master.domain.ScheduledTaskExecutionResult;
import com.axelixlabs.axelix.master.repository.ScheduledTaskExecutionResultRepository;

/**
 * Default implementation for interface {@code ScheduledTaskExecutionHistoryService}
 *
 * @author Vyacheslav Yanin
 * @author Nikita Kirillov
 */
@Service
public class DefaultScheduledTaskExecutionHistoryService implements ScheduledTaskExecutionHistoryService {

    private static final double P95 = 0.95;

    private final ScheduledTaskExecutionResultRepository executionResultRepository;

    public DefaultScheduledTaskExecutionHistoryService(
            ScheduledTaskExecutionResultRepository executionResultRepository) {
        this.executionResultRepository = executionResultRepository;
    }

    @Override
    public void append(Set<ScheduledTaskExecutionResult> scheduledTaskExecutionResults) {
        if (scheduledTaskExecutionResults.isEmpty()) {
            return;
        }
        List<String> ids = scheduledTaskExecutionResults.stream()
                .map(ScheduledTaskExecutionResult::id)
                .toList();
        Set<String> alreadyStoredIds = executionResultRepository.findStoredIds(ids);
        executionResultRepository.saveAll(scheduledTaskExecutionResults.stream()
                .filter(execution -> !alreadyStoredIds.contains(execution.id()))
                .toList());
    }

    @Override
    public Map<String, Double> findSuccessRates(ApplicationId applicationId, Set<String> taskIds) {
        if (taskIds.isEmpty()) {
            return Map.of();
        }

        return executionResultRepository
                .getExecutionStats(applicationId.groupId(), applicationId.artifactId(), taskIds)
                .stream()
                .collect(
                        HashMap::new,
                        (rates, stats) -> rates.put(stats.taskId(), stats.successful() * 100.0 / stats.total()),
                        HashMap::putAll);
    }

    @Override
    public ScheduledTaskExecutionHistoryResponse findHistory(
            ApplicationId applicationId, String taskId, int page, int size) {
        List<ScheduledTaskExecutionResult> executions = findAllOfTask(applicationId, taskId);
        int from = (int) Math.min((long) page * size, executions.size());
        int to = (int) Math.min((long) from + size, executions.size());
        return new ScheduledTaskExecutionHistoryResponse(
                summary(executions),
                failuresByErrorType(executions),
                executions.subList(from, to).stream()
                        .map(DefaultScheduledTaskExecutionHistoryService::execution)
                        .toList(),
                executions.size(),
                page,
                size);
    }

    private List<ScheduledTaskExecutionResult> findAllOfTask(ApplicationId applicationId, String taskId) {
        return executionResultRepository.findAllOfTask(applicationId.groupId(), applicationId.artifactId(), taskId);
    }

    /**
     * @param executions the executions of the task, the most recent first.
     */
    private static @Nullable Summary summary(List<ScheduledTaskExecutionResult> executions) {
        if (executions.isEmpty()) {
            return null;
        }
        long failed =
                executions.stream().filter(execution -> !execution.success()).count();
        long[] durations = executions.stream()
                .mapToLong(ScheduledTaskExecutionResult::durationMillis)
                .sorted()
                .toArray();
        return new Summary(
                executions.get(0).startedAt(),
                executions.size(),
                failed,
                successRate(executions),
                Math.round(Arrays.stream(durations).average().orElseThrow()),
                durations[(int) Math.ceil(P95 * durations.length) - 1],
                durations[durations.length - 1]);
    }

    private static double successRate(List<ScheduledTaskExecutionResult> executions) {
        long succeeded = executions.stream()
                .filter(ScheduledTaskExecutionResult::success)
                .count();
        return succeeded * 100.0 / executions.size();
    }

    /**
     * @param executions the executions of the task, the most recent first.
     * @return the failures grouped by the error type, the most frequent first.
     */
    private static List<FailuresByErrorType> failuresByErrorType(List<ScheduledTaskExecutionResult> executions) {
        Map<String, List<ScheduledTaskExecutionResult>> failuresByErrorType = new LinkedHashMap<>();
        for (ScheduledTaskExecutionResult execution : executions) {
            if (!execution.success()) {
                failuresByErrorType
                        .computeIfAbsent(Objects.requireNonNull(execution.errorType()), errorType -> new ArrayList<>())
                        .add(execution);
            }
        }
        return failuresByErrorType.entrySet().stream()
                .map(failures -> new FailuresByErrorType(
                        failures.getKey(),
                        failures.getValue().size(),
                        failures.getValue().get(0).errorMessage(),
                        failures.getValue().get(0).startedAt()))
                .sorted(Comparator.comparingLong(FailuresByErrorType::count).reversed())
                .toList();
    }

    private static Execution execution(ScheduledTaskExecutionResult execution) {
        return new Execution(
                execution.startedAt(),
                execution.instanceId(),
                execution.instanceName(),
                execution.durationMillis(),
                execution.success(),
                execution.launchType(),
                execution.errorType(),
                execution.errorMessage());
    }
}
