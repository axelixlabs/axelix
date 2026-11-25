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
package com.axelixlabs.axelix.sbs.spring.core.quartz;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.contract.quartz.ServiceQuartzJobs;
import com.axelixlabs.axelix.common.contract.quartz.ServiceQuartzSchedulers;

/**
 * Provides Quartz schedulers and jobs.
 *
 * @author Nikita Kirillov
 */
public interface AxelixQuartzService {

    /**
     * @return metadata of available schedulers; schedulers whose metadata cannot be retrieved are skipped.
     */
    ServiceQuartzSchedulers getQuartzSchedulers();

    /**
     * @param schedulerName the scheduler name.
     * @return the scheduler's jobs and triggers, or {@code null} if no available scheduler has the given name.
     * @throws QuartzAccessException if jobs or triggers cannot be retrieved.
     */
    @Nullable
    ServiceQuartzJobs getQuartzJobs(String schedulerName);
}
