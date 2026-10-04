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
package com.axelixlabs.axelix.master.service.convert;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.master.contract.heartbeat.HeartBeatMetadata;
import com.axelixlabs.axelix.master.contract.metadata.BasicRegistrationMetadata;
import com.axelixlabs.axelix.master.contract.metadata.Insights;
import com.axelixlabs.axelix.master.contract.metadata.ScheduledTaskExecution;
import com.axelixlabs.axelix.master.domain.ScheduledTaskExecutionResult;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ScheduledTaskExecutionResultConverter}.
 *
 * @author Vyacheslav Yanin
 */
class ScheduledTaskExecutionResultConverterTest {

    private final ScheduledTaskExecutionResultConverter subject = new ScheduledTaskExecutionResultConverter();

    @Test
    void shouldSkipExecutionsWithUnparseableStartedAtTimestamp() {
        // given.
        HeartBeatMetadata heartBeatMetadata = new HeartBeatMetadata()
                .instanceId("3c994958-924f-4a12-87d0-a8782e97af10")
                .basicRegistrationMetadata(new BasicRegistrationMetadata()
                        .groupId("org.springframework.samples")
                        .artifactId("petclinic")
                        .insights(new Insights()
                                .scheduledTaskExecutions(List.of(
                                        new ScheduledTaskExecution()
                                                .taskId("com.example.OwnerJob#run()")
                                                .startedAt("2026-09-27T10:00:00.123Z")
                                                .durationMillis(1500L)
                                                .success(true),
                                        new ScheduledTaskExecution()
                                                .taskId("com.example.BrokenJob#run()")
                                                .startedAt("not-a-timestamp")
                                                .durationMillis(42L)
                                                .success(true)))));

        // when.
        Set<ScheduledTaskExecutionResult> results = subject.convert(heartBeatMetadata);

        // then.
        assertThat(results)
                .singleElement()
                .extracting(ScheduledTaskExecutionResult::taskId)
                .isEqualTo("com.example.OwnerJob#run()");
    }
}
