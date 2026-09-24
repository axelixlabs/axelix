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
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.registration.insights.ScheduledTaskExecution;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.ScheduledTaskExecutionHistory.HistorySnapshot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link ScheduledTaskExecutionHistory}.
 *
 * @author Vyacheslav Yanin
 */
class ScheduledTaskExecutionHistoryTest {

    private static final UUID TASK_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ANOTHER_TASK_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private ScheduledTaskExecutionHistory history;

    @BeforeEach
    void setUp() {
        history = new ScheduledTaskExecutionHistory(properties(30));
    }

    @Test
    void record_shouldAddExecutionToTheHistory() {
        ScheduledTaskExecution execution = execution(TASK_ID, 1);

        history.record(execution);

        HistorySnapshot snapshot = history.mark();
        assertThat(snapshot.getExecutions()).containsExactly(execution);
        assertThat(snapshot.getWatermark()).isEqualTo(1);
    }

    @Test
    void record_shouldEvictOldestExecutionWhenTaskQueueExceedsTheLimit() {
        history = new ScheduledTaskExecutionHistory(properties(2));
        ScheduledTaskExecution first = execution(TASK_ID, 1);
        ScheduledTaskExecution second = execution(TASK_ID, 2);
        ScheduledTaskExecution third = execution(TASK_ID, 3);

        history.record(first);
        history.record(second);
        history.record(third);

        HistorySnapshot snapshot = history.mark();
        assertThat(snapshot.getExecutions()).containsExactly(second, third);
        assertThat(snapshot.getWatermark()).isEqualTo(3);
    }

    @Test
    void record_shouldEvictPerTaskIndependently() {
        history = new ScheduledTaskExecutionHistory(properties(2));
        ScheduledTaskExecution other = execution(ANOTHER_TASK_ID, 4);
        ScheduledTaskExecution first = execution(TASK_ID, 1);
        ScheduledTaskExecution second = execution(TASK_ID, 2);
        ScheduledTaskExecution third = execution(TASK_ID, 3);

        history.record(other);
        history.record(first);
        history.record(second);
        history.record(third);

        HistorySnapshot snapshot = history.mark();
        assertThat(snapshot.getExecutions()).containsExactly(other, second, third);
    }

    @Test
    void mark_shouldReturnEmptySnapshotWhenNoExecutionsRecorded() {
        HistorySnapshot snapshot = history.mark();

        assertThat(snapshot.getExecutions()).isEmpty();
        assertThat(snapshot.getWatermark()).isZero();
    }

    @Test
    void mark_shouldReturnExecutionsOrderedByGenerationAcrossTasks() {
        ScheduledTaskExecution taskOneFirst = execution(TASK_ID, 1);
        ScheduledTaskExecution other = execution(ANOTHER_TASK_ID, 2);
        ScheduledTaskExecution taskOneSecond = execution(TASK_ID, 3);

        history.record(taskOneFirst);
        history.record(other);
        history.record(taskOneSecond);

        HistorySnapshot snapshot = history.mark();
        assertThat(snapshot.getExecutions()).containsExactly(taskOneFirst, other, taskOneSecond);
    }

    @Test
    void mark_shouldReturnImmutableSnapshotAndNewInstances() {
        history.record(execution(TASK_ID, 1));

        HistorySnapshot first = history.mark();
        HistorySnapshot second = history.mark();

        assertThat(first).isEqualTo(second);
        assertThat(first).isNotSameAs(second);
        assertThat(first.getExecutions()).isNotSameAs(second.getExecutions());
        assertThatThrownBy(() -> first.getExecutions().add(execution(TASK_ID, 2)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void mark_shouldBeAPureRead() {
        ScheduledTaskExecution execution = execution(TASK_ID, 1);
        history.record(execution);

        HistorySnapshot first = history.mark();
        HistorySnapshot second = history.mark();

        assertThat(second.getExecutions()).containsExactly(execution);
        assertThat(first).isEqualTo(second);
    }

    @Test
    void commit_shouldRemoveExecutionsUpToTheWatermark() {
        ScheduledTaskExecution first = execution(TASK_ID, 1);
        ScheduledTaskExecution second = execution(TASK_ID, 2);
        history.record(first);
        history.record(second);
        long watermark = history.mark().getWatermark();

        history.commit(watermark);

        HistorySnapshot snapshot = history.mark();
        assertThat(snapshot.getExecutions()).isEmpty();
    }

    @Test
    void commit_shouldKeepExecutionsRecordedAfterTheWatermark() {
        ScheduledTaskExecution first = execution(TASK_ID, 1);
        ScheduledTaskExecution second = execution(TASK_ID, 2);
        ScheduledTaskExecution third = execution(TASK_ID, 3);
        history.record(first);
        history.record(second);
        HistorySnapshot delivered = history.mark();

        history.record(third);
        history.commit(delivered.getWatermark());

        HistorySnapshot snapshot = history.mark();
        assertThat(snapshot.getExecutions()).containsExactly(third);
        assertThat(snapshot.getWatermark()).isEqualTo(3);
    }

    @Test
    void commit_shouldRemoveEmptyTaskQueues() {
        history.record(execution(TASK_ID, 1));
        history.record(execution(ANOTHER_TASK_ID, 2));

        history.commit(history.mark().getWatermark());

        assertThat(history.mark().getExecutions()).isEmpty();
    }

    private static ScheduledTaskHistoryConfigurationProperties properties(int historyMaxSize) {
        var properties = new ScheduledTaskHistoryConfigurationProperties();
        properties.setHistoryMaxSize(historyMaxSize);
        return properties;
    }

    private static ScheduledTaskExecution execution(UUID taskId, long sequence) {
        return new ScheduledTaskExecution(
                taskId, Instant.parse("2026-01-01T00:00:00Z").plusSeconds(sequence), 100L, true, null, null);
    }
}
