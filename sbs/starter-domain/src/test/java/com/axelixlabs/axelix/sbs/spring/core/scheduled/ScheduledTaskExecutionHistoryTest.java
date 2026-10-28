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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskExecution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link ScheduledTaskExecutionHistory}.
 *
 * @author Vyacheslav Yanin
 * @author Nikita Kirillov
 */
class ScheduledTaskExecutionHistoryTest {

    private static final String TASK_ID = "com.example.Job#run()";
    private static final String ANOTHER_TASK_ID = "com.example.AnotherJob#run()";

    private ScheduledTaskExecutionHistory history;

    @BeforeEach
    void setUp() {
        history = new ScheduledTaskExecutionHistory();
    }

    @Test
    void record_shouldAddExecutionToTheHistory() {
        ScheduledTaskExecution execution = execution(TASK_ID);

        history.record(execution);

        assertThat(history.getExecutions()).containsExactly(execution);
    }

    @Test
    void record_shouldEvictOldestExecutionWhenTaskQueueExceedsTheLimit() {
        List<ScheduledTaskExecution> executions = executions(TASK_ID, 11);

        executions.forEach(history::record);

        assertThat(history.getExecutions()).containsExactlyElementsOf(executions.subList(1, 11));
    }

    @Test
    void record_shouldEvictPerTaskIndependently() {
        ScheduledTaskExecution other = execution(ANOTHER_TASK_ID);
        List<ScheduledTaskExecution> executions = executions(TASK_ID, 11);

        history.record(other);
        executions.forEach(history::record);

        List<ScheduledTaskExecution> expected = new ArrayList<>(executions.subList(1, 11));
        expected.add(other);
        assertThat(history.getExecutions()).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    void getExecutions_shouldReturnEmptyListWhenNoExecutionsRecorded() {
        assertThat(history.getExecutions()).isEmpty();
    }

    @Test
    void getExecutions_shouldReturnImmutableList() {
        history.record(execution(TASK_ID));

        List<ScheduledTaskExecution> first = history.getExecutions();
        List<ScheduledTaskExecution> second = history.getExecutions();

        assertThat(first).isEqualTo(second).isNotSameAs(second);
        assertThatThrownBy(() -> first.add(execution(TASK_ID))).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void getExecutions_shouldKeepTheExecutionsInTheHistory() {
        ScheduledTaskExecution execution = execution(TASK_ID);
        history.record(execution);

        assertThat(history.getExecutions()).containsExactly(execution);
        assertThat(history.getExecutions()).containsExactly(execution);
    }

    private static List<ScheduledTaskExecution> executions(String taskId, int count) {
        List<ScheduledTaskExecution> executions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            executions.add(execution(taskId).durationMillis((long) i));
        }
        return executions;
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
