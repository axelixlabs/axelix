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
package com.axelixlabs.axelix.master.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import com.axelixlabs.axelix.common.contract.metadata.ScheduledTaskLaunchType;
import com.axelixlabs.axelix.master.domain.ScheduledTaskExecutionResult;
import com.axelixlabs.axelix.master.utils.database.DatabaseMatrixTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the {@link ScheduledTaskExecutionResultRepository} that verify both the
 * Liquibase migration and the entity mapping on every supported {@code OssRdbms}.
 *
 * @author Vyacheslav Yanin
 * @author Nikita Kirillov
 */
@DatabaseMatrixTest
class ScheduledTaskExecutionResultRepositoryTest {

    @Autowired
    private ScheduledTaskExecutionResultRepository subject;

    @BeforeEach
    @AfterEach
    void cleanup() {
        subject.deleteAll();
    }

    @Test
    void save_shouldPersistAndReadBackScheduledTaskExecutionResult() {
        var id = UUID.randomUUID().toString();
        var startedAt = Instant.parse("2026-09-27T10:00:00.123Z");
        var executionResult = new ScheduledTaskExecutionResult(
                id,
                "com.axelixlabs",
                "sample-app",
                "instance-1",
                "instance-1-name",
                "com.example.Job#run()",
                startedAt,
                1500L,
                false,
                ScheduledTaskLaunchType.SCHEDULED,
                "NullPointerException",
                "boom");
        // A second execution of the same instance and task within the same second must be
        // distinguishable from the first one by its millisecond timestamp.
        var sameSecondExecution = new ScheduledTaskExecutionResult(
                UUID.randomUUID().toString(),
                "com.axelixlabs",
                "sample-app",
                "instance-1",
                "instance-1-name",
                "com.example.Job#run()",
                startedAt.plusMillis(1),
                2000L,
                true,
                ScheduledTaskLaunchType.MANUAL,
                null,
                null);

        subject.save(executionResult);
        subject.save(sameSecondExecution);

        ScheduledTaskExecutionResult loaded = subject.findById(id).orElseThrow();
        assertThat(loaded).isEqualTo(executionResult);
        ScheduledTaskExecutionResult sameSecondLoaded =
                subject.findById(sameSecondExecution.id()).orElseThrow();
        assertThat(sameSecondLoaded).isEqualTo(sameSecondExecution);
    }

    @Test
    void findAllOfTask_shouldReturnTheExecutionsOfTheTaskOfTheApplicationOnlyMostRecentFirst() {
        var startedAt = Instant.parse("2026-09-27T10:00:00Z");
        var oldest = execution("com.axelixlabs", "sample-app", "com.example.Job#run()", startedAt);
        var middle = execution("com.axelixlabs", "sample-app", "com.example.Job#run()", startedAt.plusSeconds(1));
        var latest = execution("com.axelixlabs", "sample-app", "com.example.Job#run()", startedAt.plusSeconds(2));
        var otherTask = execution("com.axelixlabs", "sample-app", "com.example.Other#run()", startedAt.plusSeconds(3));
        var otherApplication =
                execution("com.axelixlabs", "other-app", "com.example.Job#run()", startedAt.plusSeconds(3));
        subject.saveAll(List.of(oldest, middle, latest, otherTask, otherApplication));

        assertThat(subject.findAllOfTask("com.axelixlabs", "sample-app", "com.example.Job#run()"))
                .containsExactly(latest, middle, oldest);
    }

    @Test
    void deleteStartedBefore_shouldDeleteTheOlderExecutionsOnly() {
        var threshold = Instant.parse("2026-09-27T10:00:00Z");
        var expired = execution("com.axelixlabs", "sample-app", "com.example.Job#run()", threshold.minusMillis(1));
        var kept = execution("com.axelixlabs", "sample-app", "com.example.Job#run()", threshold);
        subject.saveAll(List.of(expired, kept));

        int deleted = subject.deleteStartedBefore(threshold);

        assertThat(deleted).isEqualTo(1);
        assertThat(subject.findAll()).containsExactly(kept);
    }

    @Test
    void deleteAllButLatest_shouldKeepTheLatestExecutionsOfEachTaskOfEachApplication() {
        var startedAt = Instant.parse("2026-09-27T10:00:00Z");
        var oldest = execution("com.axelixlabs", "sample-app", "com.example.Job#run()", startedAt);
        var middle = execution("com.axelixlabs", "sample-app", "com.example.Job#run()", startedAt.plusSeconds(1));
        var latest = execution("com.axelixlabs", "sample-app", "com.example.Job#run()", startedAt.plusSeconds(2));
        var otherTask = execution("com.axelixlabs", "sample-app", "com.example.Other#run()", startedAt);
        var otherApplication = execution("com.axelixlabs", "other-app", "com.example.Job#run()", startedAt);
        subject.saveAll(List.of(oldest, middle, latest, otherTask, otherApplication));

        int deleted = subject.deleteAllButLatest(2);

        assertThat(deleted).isEqualTo(1);
        assertThat(subject.findAll()).containsExactlyInAnyOrder(middle, latest, otherTask, otherApplication);
    }

    private static ScheduledTaskExecutionResult execution(
            String groupId, String artifactId, String taskId, Instant startedAt) {
        return new ScheduledTaskExecutionResult(
                UUID.randomUUID().toString(),
                groupId,
                artifactId,
                "instance-1",
                "instance-1-name",
                taskId,
                startedAt,
                100L,
                true,
                ScheduledTaskLaunchType.SCHEDULED,
                null,
                null);
    }
}
