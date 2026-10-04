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
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskExecution;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.ScheduledTaskExecutionHistory.HistorySnapshot;

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
        history = new ScheduledTaskExecutionHistory(properties(30));
    }

    @Test
    void record_shouldAddExecutionToTheHistory() {
        ScheduledTaskExecution execution = execution(TASK_ID);

        history.record(execution);

        HistorySnapshot snapshot = history.mark();
        assertThat(snapshot.getExecutions()).containsExactly(Map.entry(TASK_ID, List.of(execution)));
        assertThat(snapshot.getGeneration()).isEqualTo(1);
    }

    @Test
    void record_shouldEvictOldestExecutionWhenTaskQueueExceedsTheLimit() {
        history = new ScheduledTaskExecutionHistory(properties(2));
        ScheduledTaskExecution first = execution(TASK_ID);
        ScheduledTaskExecution second = execution(TASK_ID);
        ScheduledTaskExecution third = execution(TASK_ID);

        history.record(first);
        history.record(second);
        history.record(third);

        HistorySnapshot snapshot = history.mark();
        assertThat(snapshot.getExecutions()).containsExactly(Map.entry(TASK_ID, List.of(second, third)));
        assertThat(snapshot.getGeneration()).isEqualTo(3);
    }

    @Test
    void record_shouldEvictPerTaskIndependently() {
        history = new ScheduledTaskExecutionHistory(properties(2));
        ScheduledTaskExecution other = execution(ANOTHER_TASK_ID);
        ScheduledTaskExecution first = execution(TASK_ID);
        ScheduledTaskExecution second = execution(TASK_ID);
        ScheduledTaskExecution third = execution(TASK_ID);

        history.record(other);
        history.record(first);
        history.record(second);
        history.record(third);

        HistorySnapshot snapshot = history.mark();
        assertThat(snapshot.getExecutions())
                .containsExactlyInAnyOrderEntriesOf(
                        Map.of(ANOTHER_TASK_ID, List.of(other), TASK_ID, List.of(second, third)));
    }

    @Test
    void mark_shouldReturnEmptySnapshotWhenNoExecutionsRecorded() {
        HistorySnapshot snapshot = history.mark();

        assertThat(snapshot.getExecutions()).isEmpty();
        assertThat(snapshot.getGeneration()).isZero();
    }

    @Test
    void mark_shouldReturnImmutableSnapshotAndNewInstances() {
        history.record(execution(TASK_ID));

        HistorySnapshot first = history.mark();
        HistorySnapshot second = history.mark();

        assertThat(first).isEqualTo(second);
        assertThat(first).isNotSameAs(second);
        assertThat(first.getExecutions()).isNotSameAs(second.getExecutions());
        assertThatThrownBy(() -> first.getExecutions().get(TASK_ID).add(execution(TASK_ID)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void mark_shouldBeAPureRead() {
        ScheduledTaskExecution execution = execution(TASK_ID);
        history.record(execution);

        HistorySnapshot first = history.mark();
        HistorySnapshot second = history.mark();

        assertThat(second.getExecutions()).containsExactly(Map.entry(TASK_ID, List.of(execution)));
        assertThat(first).isEqualTo(second);
    }

    @Test
    void commit_shouldRemoveExecutionsUpToTheProvidedGeneration() {
        ScheduledTaskExecution first = execution(TASK_ID);
        ScheduledTaskExecution second = execution(TASK_ID);
        history.record(first);
        history.record(second);
        long generation = history.mark().getGeneration();

        history.commit(generation);

        HistorySnapshot snapshot = history.mark();
        assertThat(snapshot.getExecutions()).isEmpty();
    }

    @Test
    void commit_shouldKeepExecutionsRecordedAfterTheProvidedGeneration() {
        ScheduledTaskExecution first = execution(TASK_ID);
        ScheduledTaskExecution second = execution(TASK_ID);
        ScheduledTaskExecution third = execution(TASK_ID);
        history.record(first);
        history.record(second);
        HistorySnapshot delivered = history.mark();

        history.record(third);
        history.commit(delivered.getGeneration());

        HistorySnapshot snapshot = history.mark();
        assertThat(snapshot.getExecutions()).containsExactly(Map.entry(TASK_ID, List.of(third)));
        assertThat(snapshot.getGeneration()).isEqualTo(3);
    }

    @Test
    void commit_shouldRemoveEmptyTaskQueues() {
        history.record(execution(TASK_ID));
        history.record(execution(ANOTHER_TASK_ID));

        history.commit(history.mark().getGeneration());

        assertThat(history.mark().getExecutions()).isEmpty();
    }

    private static ScheduledTaskHistoryConfigurationProperties properties(int historyMaxSize) {
        return new ScheduledTaskHistoryConfigurationProperties(historyMaxSize);
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
