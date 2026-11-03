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

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.master.repository.ScheduledTaskExecutionResultRepository;

/**
 * Deletes the scheduled task executions that started longer ago than the retention period, and the ones beyond the
 * latest executions of each task.
 *
 * @author Nikita Kirillov
 */
@Service
public class ScheduledTaskExecutionHistoryCleaner {

    private final ScheduledTaskExecutionResultRepository executionResultRepository;

    @SuppressWarnings("NullAway.Init")
    @Value("${axelix.master.scheduled-tasks.history.retention}")
    private Duration retention;

    @Value("${axelix.master.scheduled-tasks.history.max-executions-per-task}")
    private int maxExecutionsPerTask;

    public ScheduledTaskExecutionHistoryCleaner(ScheduledTaskExecutionResultRepository executionResultRepository) {
        this.executionResultRepository = executionResultRepository;
    }

    @Scheduled(cron = "${axelix.master.scheduled-tasks.history.cleanup.schedule}")
    public void deleteExpiredExecutions() {
        executionResultRepository.deleteStartedBefore(Instant.now().minus(retention));
        executionResultRepository.deleteAllButLatest(maxExecutionsPerTask);
    }
}
