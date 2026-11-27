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
package com.axelixlabs.axelix.sbs.spring.core.quartz;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.contract.metadata.QuartzJobExecutionHistory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link QuartzExecutionHistory}.
 *
 * @author Nikita Kirillov
 */
class QuartzExecutionHistoryTest {

    private static final String SCHEDULER_NAME = "quartzScheduler";
    private static final String ANOTHER_SCHEDULER_NAME = "anotherScheduler";
    private static final String JOB_NAME = "job";
    private static final String JOB_GROUP = "jobs";

    private QuartzExecutionHistory history;

    @BeforeEach
    void setUp() {
        history = new QuartzExecutionHistory();
    }

    @Test
    void record_shouldAddExecutionToTheSchedulerHistory() {
        QuartzJobExecutionHistory execution = execution(SCHEDULER_NAME, JOB_NAME, JOB_GROUP, "1");

        history.record(SCHEDULER_NAME, execution);

        assertThat(history.getExecutions(SCHEDULER_NAME)).containsExactly(execution);
        assertThat(history.getExecutions()).containsExactly(execution);
    }

    @Test
    void record_shouldKeepJobAndSchedulerHistoriesIndependent() {
        QuartzJobExecutionHistory anotherJob = execution(SCHEDULER_NAME, "anotherJob", JOB_GROUP, "other-job");
        QuartzJobExecutionHistory anotherScheduler =
                execution(ANOTHER_SCHEDULER_NAME, JOB_NAME, JOB_GROUP, "other-scheduler");
        List<QuartzJobExecutionHistory> executions = executions(SCHEDULER_NAME, JOB_NAME, JOB_GROUP, 11);

        history.record(SCHEDULER_NAME, anotherJob);
        history.record(ANOTHER_SCHEDULER_NAME, anotherScheduler);
        executions.forEach(execution -> history.record(SCHEDULER_NAME, execution));
        List<QuartzJobExecutionHistory> schedulerExecutions = new ArrayList<>(executions.subList(1, 11));
        schedulerExecutions.add(anotherJob);
        List<QuartzJobExecutionHistory> allExecutions = new ArrayList<>(schedulerExecutions);
        allExecutions.add(anotherScheduler);

        assertThat(history.getExecutions(SCHEDULER_NAME)).containsExactlyInAnyOrderElementsOf(schedulerExecutions);
        assertThat(history.getExecutions(ANOTHER_SCHEDULER_NAME)).containsExactly(anotherScheduler);
        assertThat(history.getExecutions()).containsExactlyInAnyOrderElementsOf(allExecutions);
    }

    private static List<QuartzJobExecutionHistory> executions(
            String schedulerName, String jobName, String jobGroup, int count) {
        List<QuartzJobExecutionHistory> executions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            executions.add(execution(schedulerName, jobName, jobGroup, String.valueOf(i)));
        }
        return executions;
    }

    private static QuartzJobExecutionHistory execution(
            String schedulerName, String jobName, String jobGroup, String executionId) {
        return new QuartzJobExecutionHistory()
                .executionId(executionId)
                .schedulerName(schedulerName)
                .jobName(jobName)
                .jobGroup(jobGroup)
                .triggerName("trigger")
                .triggerGroup("triggers")
                .startedAt("2026-01-01T00:00:00Z")
                .durationMillis(10L)
                .success(true);
    }
}
