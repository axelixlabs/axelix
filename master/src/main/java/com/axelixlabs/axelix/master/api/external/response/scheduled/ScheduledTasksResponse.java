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
package com.axelixlabs.axelix.master.api.external.response.scheduled;

import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * The scheduled tasks of the instance.
 *
 * @author Nikita Kirillov
 */
public record ScheduledTasksResponse(
        List<CronTask> cron, List<FixedDelayTask> fixedDelay, List<FixedRateTask> fixedRate, List<CustomTask> custom) {

    public record CronTask(
            String taskId,
            String className,
            @Nullable String methodName,
            String expression,
            boolean enabled,
            @Nullable Double successRate) {}

    /**
     * @param interval     the delay between the end of an execution and the start of the next one, in milliseconds.
     * @param initialDelay the delay before the first execution, in milliseconds.
     */
    public record FixedDelayTask(
            String taskId,
            String className,
            @Nullable String methodName,
            long interval,
            long initialDelay,
            boolean enabled,
            @Nullable Double successRate) {}

    /**
     * @param interval     the interval between the starts of the consecutive executions, in milliseconds.
     * @param initialDelay the delay before the first execution, in milliseconds.
     */
    public record FixedRateTask(
            String taskId,
            String className,
            @Nullable String methodName,
            long interval,
            long initialDelay,
            boolean enabled,
            @Nullable Double successRate) {}

    public record CustomTask(
            String taskId,
            String className,
            @Nullable String methodName,
            String trigger,
            boolean enabled,
            @Nullable Double successRate) {}
}
