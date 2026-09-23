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
package com.axelixlabs.axelix.common.api.registration.insights;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * Immutable DTO representing the result of a single execution of a scheduled task.
 *
 * @author Vyacheslav Yanin
 * @see Insights
 */
public class ScheduledTaskExecution {

    private final UUID taskId;

    private final Instant startedAt;
    private final long durationMillis;

    private final boolean success;

    /**
     * The name of exception
     */
    private final @Nullable String errorType;

    private final @Nullable String errorMessage;

    /**
     * @param errorType The name of exception
     */
    @JsonCreator
    public ScheduledTaskExecution(
            @JsonProperty("taskId") UUID taskId,
            @JsonProperty("startedAt") Instant startedAt,
            @JsonProperty("durationMillis") long durationMillis,
            @JsonProperty("success") boolean success,
            @JsonProperty("errorType") @Nullable String errorType,
            @JsonProperty("errorMessage") @Nullable String errorMessage) {
        this.taskId = taskId;
        this.startedAt = startedAt;
        this.durationMillis = durationMillis;
        this.success = success;
        this.errorType = errorType;
        this.errorMessage = errorMessage;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public boolean isSuccess() {
        return success;
    }

    public @Nullable String getErrorType() {
        return errorType;
    }

    public @Nullable String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "ScheduledTaskExecution{" + "taskId="
                + taskId + ", startedAt="
                + startedAt + ", durationMillis="
                + durationMillis + ", success="
                + success + ", errorType='"
                + errorType + ", errorMessage='"
                + errorMessage + '}';
    }
}
