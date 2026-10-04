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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.axelixlabs.axelix.master.domain.ScheduledTaskExecutionResult;
import com.axelixlabs.axelix.master.repository.ScheduledTaskExecutionResultRepository;

/**
 * Default implementation for interface {@code ScheduledTaskExecutionHistoryService}
 *
 * @author Vyacheslav Yanin
 */
@Service
public class DefaultScheduledTaskExecutionHistoryService implements ScheduledTaskExecutionHistoryService {

    private final ScheduledTaskExecutionResultRepository executionResultRepository;

    public DefaultScheduledTaskExecutionHistoryService(
            ScheduledTaskExecutionResultRepository executionResultRepository) {
        this.executionResultRepository = executionResultRepository;
    }

    @Override
    @Transactional
    public void append(Set<ScheduledTaskExecutionResult> scheduledTaskExecutionResults) {
        executionResultRepository.saveAll(scheduledTaskExecutionResults);
    }
}
