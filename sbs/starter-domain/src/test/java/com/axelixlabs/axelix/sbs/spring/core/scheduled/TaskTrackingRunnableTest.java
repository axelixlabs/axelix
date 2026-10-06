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
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskExecution;
import com.axelixlabs.axelix.sbs.spring.core.testutils.NoOpLogger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link TaskTrackingRunnable}.
 *
 * @author Vyacheslav Yanin
 */
class TaskTrackingRunnableTest {

    private static final String TASK_ID = "com.example.Job#run()";
    private static final long SLEEP_MILLIS = 30L;

    private ScheduledTaskExecutionHistory history;

    @BeforeEach
    void setUp() {
        history = new ScheduledTaskExecutionHistory(
                new ScheduledTaskHistoryConfigurationProperties(30), new NoOpLogger());
    }

    @Test
    void run_shouldRecordSuccessfulExecution() {
        var before = Instant.now();

        new TaskTrackingRunnable(new NoOpTask(), history).run();

        var after = Instant.now();

        ScheduledTaskExecution execution = recordedExecution();
        assertThat(execution.getSuccess()).isTrue();
        assertThat(execution.getErrorType()).isNull();
        assertThat(execution.getErrorMessage()).isNull();
        assertThat(Instant.parse(execution.getStartedAt())).isBetween(before, after);
    }

    @Test
    void run_shouldMeasureDurationOfTheDelegate() {
        new TaskTrackingRunnable(new SleepingTask(SLEEP_MILLIS), history).run();

        ScheduledTaskExecution execution = recordedExecution();
        assertThat(execution.getDurationMillis()).isGreaterThanOrEqualTo(SLEEP_MILLIS);
    }

    @Test
    void run_shouldRecordFailedExecution() {
        RuntimeException failure = new IllegalStateException("boom");

        assertThatThrownBy(() -> new TaskTrackingRunnable(new FailingTask(failure), history).run())
                .isSameAs(failure);

        ScheduledTaskExecution execution = recordedExecution();
        assertThat(execution.getSuccess()).isFalse();
        assertThat(execution.getErrorType()).isEqualTo(IllegalStateException.class.getSimpleName());
        assertThat(execution.getErrorMessage()).isEqualTo("boom");
    }

    @Test
    void run_shouldRethrowOriginalError() {
        Error failure = new StackOverflowError();

        assertThatThrownBy(() -> new TaskTrackingRunnable(new ErroringTask(failure), history).run())
                .isSameAs(failure);

        ScheduledTaskExecution execution = recordedExecution();
        assertThat(execution.getSuccess()).isFalse();
        assertThat(execution.getErrorType()).isEqualTo(StackOverflowError.class.getSimpleName());
    }

    private ScheduledTaskExecution recordedExecution() {
        List<ScheduledTaskExecution> executions = history.mark();
        assertThat(executions).hasSize(1);
        return executions.get(0);
    }

    private static final class NoOpTask implements Runnable {

        @Override
        public void run() {
            // nothing to do
        }

        @Override
        public String toString() {
            return TASK_ID;
        }
    }

    private static final class SleepingTask implements Runnable {

        private final long sleepMillis;

        private SleepingTask(long sleepMillis) {
            this.sleepMillis = sleepMillis;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public String toString() {
            return TASK_ID;
        }
    }

    private static final class FailingTask implements Runnable {

        private final RuntimeException failure;

        private FailingTask(RuntimeException failure) {
            this.failure = failure;
        }

        @Override
        public void run() {
            throw failure;
        }

        @Override
        public String toString() {
            return TASK_ID;
        }
    }

    private static final class ErroringTask implements Runnable {

        private final Error failure;

        private ErroringTask(Error failure) {
            this.failure = failure;
        }

        @Override
        public void run() {
            throw failure;
        }

        @Override
        public String toString() {
            return TASK_ID;
        }
    }
}
