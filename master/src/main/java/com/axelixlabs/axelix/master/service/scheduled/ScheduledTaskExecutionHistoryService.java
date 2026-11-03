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

import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.master.api.external.response.scheduled.ScheduledTaskExecutionHistoryResponse;
import com.axelixlabs.axelix.master.domain.ApplicationId;
import com.axelixlabs.axelix.master.domain.ScheduledTaskExecutionResult;

/**
 * The service managing the persistent history of the scheduled task executions delivered by the
 * managed instances.
 *
 * @author Vyacheslav Yanin
 * @author Nikita Kirillov
 * @see ScheduledTaskExecutionResult
 * @see DefaultScheduledTaskExecutionHistoryService
 */
public interface ScheduledTaskExecutionHistoryService {

    /**
     * Appends the given execution results to the history, except the ones already appended.
     *
     * @param scheduledTaskExecutionResults the execution results of a single instance to persist.
     */
    void append(Set<ScheduledTaskExecutionResult> scheduledTaskExecutionResults);

    /**
     * @return the share of the succeeded executions of the task on all the instances of the application in percent,
     *         absent when there are no executions.
     */
    @Nullable
    Double findSuccessRate(ApplicationId applicationId, String taskId);

    /**
     * @param applicationId the application the task belongs to.
     * @param taskId        the id of the task.
     * @param page          the zero-based number of the page.
     * @param size          the maximum number of executions on a page.
     * @return the execution history of the task on all the instances of the application with the requested page of
     *         its executions, the most recent first.
     */
    ScheduledTaskExecutionHistoryResponse findHistory(ApplicationId applicationId, String taskId, int page, int size);
}
