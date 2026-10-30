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

import java.time.Instant;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskExecution;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskLaunchType;

/**
 * Creates the {@link ScheduledTaskExecution} records reported to master.
 *
 * @author Nikita Kirillov
 */
public final class ScheduledTaskExecutions {

    // the whole history is reported to master on every heartbeat, so the messages of the errors are bounded
    private static final int MAX_ERROR_MESSAGE_LENGTH = 1000;

    private ScheduledTaskExecutions() {}

    /**
     * @param error the error the execution has failed with, or {@code null} if it has succeeded.
     */
    public static ScheduledTaskExecution of(
            String taskId,
            ScheduledTaskLaunchType launchType,
            Instant startedAt,
            long durationMillis,
            @Nullable Throwable error) {
        return new ScheduledTaskExecution()
                .taskId(taskId)
                .launchType(launchType)
                .startedAt(startedAt.toString())
                .durationMillis(durationMillis)
                .success(error == null)
                .errorType(error != null ? error.getClass().getSimpleName() : null)
                .errorMessage(error != null ? truncate(error.getMessage()) : null);
    }

    private static @Nullable String truncate(@Nullable String message) {
        return message != null && message.length() > MAX_ERROR_MESSAGE_LENGTH
                ? message.substring(0, MAX_ERROR_MESSAGE_LENGTH)
                : message;
    }
}
