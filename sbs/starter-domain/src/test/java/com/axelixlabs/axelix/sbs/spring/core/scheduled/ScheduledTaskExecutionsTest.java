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

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskExecution;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskLaunchType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ScheduledTaskExecutions}.
 *
 * @author Nikita Kirillov
 */
class ScheduledTaskExecutionsTest {

    private static final String TASK_ID = "com.example.Job.run#cron0";
    private static final Instant STARTED_AT = Instant.parse("2026-10-28T10:15:30Z");

    @Test
    void of_shouldCreateTheSuccessfulExecution() {
        ScheduledTaskExecution execution =
                ScheduledTaskExecutions.of(TASK_ID, ScheduledTaskLaunchType.SCHEDULED, STARTED_AT, 42, null);

        assertThat(execution.getTaskId()).isEqualTo(TASK_ID);
        assertThat(execution.getStartedAt()).isEqualTo("2026-10-28T10:15:30Z");
        assertThat(execution.getDurationMillis()).isEqualTo(42);
        assertThat(execution.getSuccess()).isTrue();
        assertThat(execution.getErrorType()).isNull();
        assertThat(execution.getErrorMessage()).isNull();
    }

    @Test
    void of_shouldKeepTheLaunchType() {
        ScheduledTaskExecution execution =
                ScheduledTaskExecutions.of(TASK_ID, ScheduledTaskLaunchType.MANUAL, STARTED_AT, 42, null);

        assertThat(execution.getLaunchType()).isEqualTo(ScheduledTaskLaunchType.MANUAL);
    }

    @Test
    void of_shouldCreateTheFailedExecution() {
        ScheduledTaskExecution execution = ScheduledTaskExecutions.of(
                TASK_ID, ScheduledTaskLaunchType.SCHEDULED, STARTED_AT, 42, new IllegalStateException("boom"));

        assertThat(execution.getSuccess()).isFalse();
        assertThat(execution.getErrorType()).isEqualTo("IllegalStateException");
        assertThat(execution.getErrorMessage()).isEqualTo("boom");
    }

    @Test
    void of_shouldTruncateTheLongErrorMessage() {
        ScheduledTaskExecution execution = ScheduledTaskExecutions.of(
                TASK_ID,
                ScheduledTaskLaunchType.SCHEDULED,
                STARTED_AT,
                42,
                new IllegalStateException("x".repeat(1001)));

        assertThat(execution.getErrorMessage()).isEqualTo("x".repeat(1000));
    }

    @Test
    void of_shouldKeepTheAbsentErrorMessage() {
        ScheduledTaskExecution execution = ScheduledTaskExecutions.of(
                TASK_ID, ScheduledTaskLaunchType.SCHEDULED, STARTED_AT, 42, new IllegalStateException());

        assertThat(execution.getSuccess()).isFalse();
        assertThat(execution.getErrorType()).isEqualTo("IllegalStateException");
        assertThat(execution.getErrorMessage()).isNull();
    }
}
