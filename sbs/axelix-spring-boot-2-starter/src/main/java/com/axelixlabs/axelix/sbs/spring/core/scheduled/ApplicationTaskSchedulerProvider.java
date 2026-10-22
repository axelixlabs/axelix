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
package com.axelixlabs.axelix.sbs.spring.core.scheduled;

import org.jspecify.annotations.Nullable;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * {@link TaskSchedulerProvider} that takes the scheduler from the {@link ScheduledTaskRegistrar}.
 *
 * @since 20.10.2026
 * @author Dmitry Mazurov
 */
public final class ApplicationTaskSchedulerProvider implements TaskSchedulerProvider, SchedulingConfigurer {

    private volatile @Nullable ScheduledTaskRegistrar registrar;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        this.registrar = taskRegistrar;
    }

    /**
     * @throws IllegalStateException if the scheduler of the application is not available
     */
    @Override
    public TaskScheduler getTaskScheduler() {
        ScheduledTaskRegistrar currentRegistrar = this.registrar;
        TaskScheduler scheduler = currentRegistrar != null ? currentRegistrar.getScheduler() : null;

        if (scheduler == null) {
            throw new IllegalStateException("Could not obtain the application's TaskScheduler to re-schedule the task: "
                    + "re-scheduling is supported only when scheduling is enabled with @EnableScheduling");
        }

        return scheduler;
    }
}
