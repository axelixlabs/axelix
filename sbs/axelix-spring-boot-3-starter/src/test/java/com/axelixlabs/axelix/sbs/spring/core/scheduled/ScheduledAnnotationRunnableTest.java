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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskExecution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link ScheduledAnnotationRunnable}.
 *
 * @author Nikita Kirillov
 */
class ScheduledAnnotationRunnableTest {

    private static final String TASK_ID = "com.example.Job.run#cron0";
    private static final long SLEEP_MILLIS = 30L;

    private ScheduledTaskExecutionHistory history;

    @BeforeEach
    void setUp() {
        history = new ScheduledTaskExecutionHistory(new ScheduledTaskHistoryConfigurationProperties(30));
    }

    @Test
    void run_shouldRecordSuccessfulExecution() {
        Instant before = Instant.now();

        regular(() -> {}).run();

        Instant after = Instant.now();
        ScheduledTaskExecution execution = recordedExecution();
        assertThat(execution.getTaskId()).isEqualTo(TASK_ID);
        assertThat(execution.getSuccess()).isTrue();
        assertThat(execution.getErrorType()).isNull();
        assertThat(execution.getErrorMessage()).isNull();
        assertThat(Instant.parse(execution.getStartedAt())).isBetween(before, after);
    }

    @Test
    void run_shouldMeasureDurationOfTheDelegate() {
        regular(() -> sleep(SLEEP_MILLIS)).run();

        assertThat(recordedExecution().getDurationMillis()).isGreaterThanOrEqualTo(SLEEP_MILLIS);
    }

    @Test
    void run_shouldRecordFailedExecutionAndRethrowTheFailure() {
        RuntimeException failure = new IllegalStateException("boom");

        assertThatThrownBy(() -> regular(() -> {
                            throw failure;
                        })
                        .run())
                .isSameAs(failure);

        ScheduledTaskExecution execution = recordedExecution();
        assertThat(execution.getSuccess()).isFalse();
        assertThat(execution.getErrorType()).isEqualTo("IllegalStateException");
        assertThat(execution.getErrorMessage()).isEqualTo("boom");
    }

    @Test
    void run_shouldRecordFailedExecutionAndRethrowTheError() {
        Error failure = new StackOverflowError();

        assertThatThrownBy(() -> regular(() -> {
                            throw failure;
                        })
                        .run())
                .isSameAs(failure);

        assertThat(recordedExecution().getErrorType()).isEqualTo("StackOverflowError");
    }

    @Test
    void shouldExposeTheIdAndTheQualifier() {
        ScheduledAnnotationRunnable runnable =
                new ScheduledAnnotationRunnable(() -> {}, TASK_ID, "otherScheduler", history);

        assertThat(runnable).hasToString(TASK_ID);
        assertThat(runnable.getQualifier()).isEqualTo("otherScheduler");
    }

    private ScheduledAnnotationRunnable regular(Runnable delegate) {
        return new ScheduledAnnotationRunnable(delegate, TASK_ID, null, history);
    }

    private ScheduledTaskExecution recordedExecution() {
        assertThat(history.getExecutions()).hasSize(1);
        return history.getExecutions().get(0);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
