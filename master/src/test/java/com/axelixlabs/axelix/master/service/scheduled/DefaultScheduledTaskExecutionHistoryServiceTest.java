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
package com.axelixlabs.axelix.master.service.scheduled;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import com.axelixlabs.axelix.common.contract.metadata.ScheduledTaskLaunchType;
import com.axelixlabs.axelix.master.api.external.response.scheduled.ScheduledTaskExecutionHistoryResponse;
import com.axelixlabs.axelix.master.api.external.response.scheduled.ScheduledTaskExecutionHistoryResponse.Execution;
import com.axelixlabs.axelix.master.api.external.response.scheduled.ScheduledTaskExecutionHistoryResponse.Summary;
import com.axelixlabs.axelix.master.domain.ApplicationId;
import com.axelixlabs.axelix.master.domain.ScheduledTaskExecutionResult;
import com.axelixlabs.axelix.master.repository.ScheduledTaskExecutionResultRepository;
import com.axelixlabs.axelix.master.utils.database.DatabaseMatrixTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultScheduledTaskExecutionHistoryService}.
 *
 * @author Nikita Kirillov
 */
@DatabaseMatrixTest
class DefaultScheduledTaskExecutionHistoryServiceTest {

    private static final Instant STARTED_AT = Instant.parse("2026-09-27T10:00:00.123Z");
    private static final ApplicationId APPLICATION_ID = new ApplicationId("com.axelixlabs", "sample-app");
    private static final String TASK_ID = "com.example.Job.run#fixedDelay0";

    @Autowired
    private DefaultScheduledTaskExecutionHistoryService subject;

    @Autowired
    private ScheduledTaskExecutionResultRepository repository;

    @BeforeEach
    @AfterEach
    void cleanup() {
        repository.deleteAll();
    }

    @Test
    void append_shouldStoreTheExecutionsReportedAgainOnce() {
        // given
        subject.append(Set.of(execution(0), execution(1)));

        // when.
        subject.append(Set.of(execution(1), execution(2)));

        // then.
        assertThat(repository.findAll())
                .extracting(ScheduledTaskExecutionResult::id)
                .containsExactlyInAnyOrder("execution-0", "execution-1", "execution-2");
    }

    @Test
    void findHistory_shouldReturnThePageOfTheExecutionsOfTheTaskMostRecentFirst() {
        // given.
        repository.saveAll(List.of(execution(1), execution(2), execution(3)));

        // when.
        ScheduledTaskExecutionHistoryResponse lastPage = subject.findHistory(APPLICATION_ID, TASK_ID, 1, 2);
        ScheduledTaskExecutionHistoryResponse pageBeyond = subject.findHistory(APPLICATION_ID, TASK_ID, 5, 2);

        // then.
        assertThat(lastPage.executions()).extracting(Execution::startedAt).containsExactly(STARTED_AT.plusSeconds(1));
        assertThat(lastPage.totalExecutions()).isEqualTo(3);
        assertThat(pageBeyond.executions()).isEmpty();
        assertThat(pageBeyond.totalExecutions()).isEqualTo(3);
    }

    @Test
    void findHistory_shouldSummarizeAllTheExecutionsOfTheTask() {
        // given.
        saveFortyExecutions();

        // when.
        Summary summary = subject.findHistory(APPLICATION_ID, TASK_ID, 0, 10).summary();

        // then.
        assertThat(summary).isEqualTo(new Summary(STARTED_AT.plusSeconds(40), 40, 10, 75.0, 205, 380, 400));
    }

    @Test
    void findHistory_shouldBeEmptyWhenTheTaskHasNoExecutions() {
        assertThat(subject.findHistory(APPLICATION_ID, TASK_ID, 0, 10))
                .isEqualTo(new ScheduledTaskExecutionHistoryResponse(null, List.of(), List.of(), 0, 0, 10));
    }

    @Test
    void findSuccessRate_shouldReturnTheShareOfTheSucceededExecutionsOfTheTask() {
        // given.
        saveFortyExecutions();

        // when.
        Double successRate = subject.findSuccessRate(APPLICATION_ID, TASK_ID);

        // then.
        assertThat(successRate).isEqualTo(75.0);
    }

    @Test
    void findSuccessRate_shouldBeAbsentWhenTheTaskHasNoExecutions() {
        assertThat(subject.findSuccessRate(APPLICATION_ID, TASK_ID)).isNull();
    }

    private void saveFortyExecutions() {
        repository.saveAll(IntStream.rangeClosed(1, 40)
                .mapToObj(number -> execution(number, number * 10L, number % 4 != 0))
                .toList());
    }

    private ScheduledTaskExecutionResult execution(int number) {
        return execution(number, 42L, true);
    }

    private ScheduledTaskExecutionResult execution(int number, long durationMillis, boolean success) {
        return new ScheduledTaskExecutionResult(
                "execution-" + number,
                "com.axelixlabs",
                "sample-app",
                "instance-1",
                "instance-1-name",
                TASK_ID,
                STARTED_AT.plusSeconds(number),
                durationMillis,
                success,
                ScheduledTaskLaunchType.SCHEDULED,
                success ? null : "IllegalStateException",
                success ? null : "boom");
    }
}
