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
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * {@link TaskSchedulerProvider} that takes the scheduler from the {@link ScheduledTaskRegistrar} and
 * falls back to its own single-threaded one.
 *
 * @since 20.10.2026
 * @author Dmitry Mazurov
 */
public final class ApplicationTaskSchedulerProvider
        implements TaskSchedulerProvider, SchedulingConfigurer, AutoCloseable {

    public static final String FALLBACK_THREAD_NAME_PREFIX = "ax-scheduler-";

    private final ThreadPoolTaskScheduler fallbackScheduler = new ThreadPoolTaskScheduler();

    private volatile @Nullable ScheduledTaskRegistrar registrar;

    public ApplicationTaskSchedulerProvider() {
        fallbackScheduler.setPoolSize(1);
        fallbackScheduler.setThreadNamePrefix(FALLBACK_THREAD_NAME_PREFIX);
        fallbackScheduler.initialize();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        this.registrar = taskRegistrar;
    }

    @Override
    public TaskScheduler getTaskScheduler() {
        ScheduledTaskRegistrar currentRegistrar = this.registrar;
        TaskScheduler scheduler = currentRegistrar != null ? currentRegistrar.getScheduler() : null;

        return scheduler != null ? scheduler : fallbackScheduler;
    }

    @Override
    public void close() {
        fallbackScheduler.destroy();
    }
}
