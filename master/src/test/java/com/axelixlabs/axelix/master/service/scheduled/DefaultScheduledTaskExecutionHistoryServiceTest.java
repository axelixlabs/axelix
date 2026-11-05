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
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import com.axelixlabs.axelix.common.contract.metadata.ScheduledTaskLaunchType;
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

    private ScheduledTaskExecutionResult execution(int number) {
        return new ScheduledTaskExecutionResult(
                "execution-" + number,
                "com.axelixlabs",
                "sample-app",
                "instance-1",
                "com.example.Job.run#fixedDelay0",
                STARTED_AT.plusSeconds(number),
                42L,
                true,
                ScheduledTaskLaunchType.SCHEDULED,
                null,
                null);
    }
}
