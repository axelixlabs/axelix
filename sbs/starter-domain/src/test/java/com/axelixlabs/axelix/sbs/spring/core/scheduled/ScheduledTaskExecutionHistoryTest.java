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

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskExecution;
import com.axelixlabs.axelix.sbs.spring.core.testutils.NoOpLogger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link ScheduledTaskExecutionHistory}.
 *
 * @author Vyacheslav Yanin
 */
class ScheduledTaskExecutionHistoryTest {

    private static final String TASK_ID = "com.example.Job#run()";
    private static final String ANOTHER_TASK_ID = "com.example.AnotherJob#run()";

    private ScheduledTaskExecutionHistory history;

    @BeforeEach
    void setUp() {
        history = history(30);
    }

    @Test
    void record_shouldAddExecutionToTheHistory() {
        ScheduledTaskExecution execution = execution(TASK_ID);

        history.record(execution);

        assertThat(history.mark()).containsExactly(execution);
    }

    @Test
    void record_shouldEvictOldestExecutionWhenTaskQueueExceedsTheLimit() {
        history = history(2);
        ScheduledTaskExecution first = execution(TASK_ID);
        ScheduledTaskExecution second = execution(TASK_ID);
        ScheduledTaskExecution third = execution(TASK_ID);

        history.record(first);
        history.record(second);
        history.record(third);

        assertThat(history.mark()).containsExactly(second, third);
    }

    @Test
    void record_shouldEvictPerTaskIndependently() {
        history = history(2);
        ScheduledTaskExecution other = execution(ANOTHER_TASK_ID);
        ScheduledTaskExecution first = execution(TASK_ID);
        ScheduledTaskExecution second = execution(TASK_ID);
        ScheduledTaskExecution third = execution(TASK_ID);

        history.record(other);
        history.record(first);
        history.record(second);
        history.record(third);

        assertThat(history.mark()).containsExactlyInAnyOrder(other, second, third);
    }

    @Test
    void mark_shouldReturnEmptyListWhenNoExecutionsRecorded() {
        assertThat(history.mark()).isEmpty();
    }

    @Test
    void mark_shouldReturnImmutableList() {
        history.record(execution(TASK_ID));

        List<ScheduledTaskExecution> first = history.mark();
        List<ScheduledTaskExecution> second = history.mark();

        assertThat(first).isEqualTo(second).isNotSameAs(second);
        assertThatThrownBy(() -> first.add(execution(TASK_ID))).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void mark_shouldBeAPureRead() {
        ScheduledTaskExecution execution = execution(TASK_ID);
        history.record(execution);

        assertThat(history.mark()).containsExactly(execution);
        assertThat(history.mark()).containsExactly(execution);
    }

    @Test
    void commit_shouldRemoveExecutionsUpToTheMarkedWatermark() {
        ScheduledTaskExecution first = execution(TASK_ID);
        ScheduledTaskExecution second = execution(TASK_ID);
        history.record(first);
        history.record(second);
        history.mark();

        history.commit();

        assertThat(history.mark()).isEmpty();
    }

    @Test
    void commit_shouldKeepExecutionsRecordedAfterTheMarkedWatermark() {
        ScheduledTaskExecution first = execution(TASK_ID);
        ScheduledTaskExecution second = execution(TASK_ID);
        ScheduledTaskExecution third = execution(TASK_ID);
        history.record(first);
        history.record(second);
        history.mark();

        history.record(third);
        history.commit();

        assertThat(history.mark()).containsExactly(third);
    }

    @Test
    void commit_shouldRemoveEmptyTaskQueues() {
        history.record(execution(TASK_ID));
        history.record(execution(ANOTHER_TASK_ID));
        history.mark();

        history.commit();

        assertThat(history.mark()).isEmpty();
    }

    @Test
    void commit_shouldDoNothingWhenNothingHasBeenMarked() {
        ScheduledTaskExecution execution = execution(TASK_ID);
        history.record(execution);

        history.commit();

        assertThat(history.mark()).containsExactly(execution);
    }

    @Test
    void rollback_shouldReleaseTheMarkedWatermarkWithoutDroppingExecutions() {
        ScheduledTaskExecution first = execution(TASK_ID);
        history.record(first);
        history.mark();

        history.rollback();

        assertThat(history.mark()).containsExactly(first);
    }

    @Test
    void rollback_shouldLetTheNextMarkAdvanceTheWatermark() {
        ScheduledTaskExecution first = execution(TASK_ID);
        history.record(first);
        history.mark();
        history.rollback();

        ScheduledTaskExecution second = execution(TASK_ID);
        history.record(second);
        history.mark();

        history.commit();

        assertThat(history.mark()).isEmpty();
    }

    @Test
    void mark_shouldKeepThePreviousWatermarkWhenAnotherMarkIsPending() {
        ScheduledTaskExecution first = execution(TASK_ID);
        history.record(first);
        history.mark();

        ScheduledTaskExecution second = execution(TASK_ID);
        history.record(second);
        assertThat(history.mark()).containsExactly(first, second);

        history.commit();

        assertThat(history.mark()).containsExactly(second);
    }

    @Test
    void commit_shouldFreeTheWatermarkForTheFollowingMark() {
        ScheduledTaskExecution first = execution(TASK_ID);
        history.record(first);
        history.mark();
        history.commit();

        ScheduledTaskExecution second = execution(TASK_ID);
        history.record(second);
        history.mark();
        history.commit();

        assertThat(history.mark()).isEmpty();
    }

    private static ScheduledTaskExecutionHistory history(int historyMaxSize) {
        return new ScheduledTaskExecutionHistory(
                new ScheduledTaskHistoryConfigurationProperties(historyMaxSize), new NoOpLogger());
    }

    private static ScheduledTaskExecution execution(String taskId) {
        return new ScheduledTaskExecution()
                .taskId(taskId)
                .startedAt("2026-01-01T00:00:00Z")
                .durationMillis(100L)
                .success(true)
                .errorType(null)
                .errorMessage(null);
    }
}
