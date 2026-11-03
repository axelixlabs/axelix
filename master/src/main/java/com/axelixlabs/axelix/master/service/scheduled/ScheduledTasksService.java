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

import com.axelixlabs.axelix.master.api.external.response.scheduled.ScheduledTaskExecutionHistoryResponse;
import com.axelixlabs.axelix.master.api.external.response.scheduled.ScheduledTasksResponse;
import com.axelixlabs.axelix.master.domain.instance.InstanceId;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;
import com.axelixlabs.axelix.master.service.transport.BadRequestException;

/**
 * Provides the scheduled tasks of the managed instances along with their execution history.
 *
 * @author Nikita Kirillov
 */
public interface ScheduledTasksService {

    /**
     * @param instanceId the instance to get the scheduled tasks of.
     * @return the scheduled tasks of the instance, each with the success rate of its executions on all the instances
     *         of the application.
     * @throws InstanceNotFoundException when the instance is not known to this Axelix Master.
     */
    ScheduledTasksResponse getScheduledTasks(InstanceId instanceId);

    /**
     * @param instanceId the instance whose application the task belongs to.
     * @param taskId     the id of the task.
     * @param page       the zero-based number of the page.
     * @param size       the maximum number of executions on a page, at least 1.
     * @return the execution history of the task on all the instances of the application with the requested page of
     *         its executions, the most recent first.
     * @throws InstanceNotFoundException when the instance is not known to this Axelix Master.
     * @throws BadRequestException       when the page or the size is out of bounds.
     */
    ScheduledTaskExecutionHistoryResponse getExecutionHistory(InstanceId instanceId, String taskId, int page, int size);
}
