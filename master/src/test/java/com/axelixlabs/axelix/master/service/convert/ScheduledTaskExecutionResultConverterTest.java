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
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.axelixlabs.axelix.master.contract.initialregistrationrequest.InitialSelfRegistrationRequest;
import com.axelixlabs.axelix.master.contract.metadata.BasicRegistrationMetadata;
import com.axelixlabs.axelix.master.contract.metadata.Insights;
import com.axelixlabs.axelix.master.contract.metadata.ScheduledTaskExecution;
import com.axelixlabs.axelix.master.contract.metadata.ScheduledTaskLaunchType;
import com.axelixlabs.axelix.master.domain.ScheduledTaskExecutionResult;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ScheduledTaskExecutionResultConverter}.
 *
 * @author Vyacheslav Yanin
 * @author Nikita Kirillov
 */
class ScheduledTaskExecutionResultConverterTest {

    private final ScheduledTaskExecutionResultConverter subject = new ScheduledTaskExecutionResultConverter();

    @Test
    void shouldSkipExecutionsWithUnparseableStartedAtTimestamp() {
        // given.
        InitialSelfRegistrationRequest request = new InitialSelfRegistrationRequest()
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
                                                .success(true)
                                                .launchType(ScheduledTaskLaunchType.SCHEDULED),
                                        new ScheduledTaskExecution()
                                                .taskId("com.example.BrokenJob#run()")
                                                .startedAt("not-a-timestamp")
                                                .durationMillis(42L)
                                                .success(true)
                                                .launchType(ScheduledTaskLaunchType.SCHEDULED)))));

        // when.
        Set<ScheduledTaskExecutionResult> results = subject.convert(request);

        // then.
        assertThat(results)
                .singleElement()
                .extracting(ScheduledTaskExecutionResult::taskId)
                .isEqualTo("com.example.OwnerJob#run()");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("executionsWithMissingRequiredFields")
    void shouldSkipExecutionsWithMissingRequiredField(String missingField, ScheduledTaskExecution broken) {
        // given.
        InitialSelfRegistrationRequest request = request(
                execution("com.example.OwnerJob#run()", "2026-09-27T10:00:00.123Z")
                        .launchType(ScheduledTaskLaunchType.MANUAL),
                broken);

        // when.
        Set<ScheduledTaskExecutionResult> results = subject.convert(request);

        // then.
        assertThat(results).singleElement().satisfies(result -> {
            assertThat(result.taskId()).isEqualTo("com.example.OwnerJob#run()");
            assertThat(result.launchType()).isEqualTo(ScheduledTaskLaunchType.MANUAL);
        });
    }

    static Stream<Arguments> executionsWithMissingRequiredFields() {
        return Stream.of(
                Arguments.of("taskId", brokenExecution().taskId(null)),
                Arguments.of("startedAt", brokenExecution().startedAt(null)),
                Arguments.of("durationMillis", brokenExecution().durationMillis(null)),
                Arguments.of("success", brokenExecution().success(null)),
                Arguments.of("launchType", brokenExecution().launchType(null)));
    }

    @Test
    void shouldGiveTheSameIdToTheExecutionReportedAgain() {
        // given.
        InitialSelfRegistrationRequest request = request(
                execution("com.example.OwnerJob#run()", "2026-09-27T10:00:00.123Z")
                        .launchType(ScheduledTaskLaunchType.SCHEDULED),
                execution("com.example.OwnerJob#run()", "2026-09-27T10:00:00.124Z")
                        .launchType(ScheduledTaskLaunchType.SCHEDULED));

        // when.
        Set<ScheduledTaskExecutionResult> first = subject.convert(request);
        Set<ScheduledTaskExecutionResult> second = subject.convert(request);

        // then.
        assertThat(first).extracting(ScheduledTaskExecutionResult::id).doesNotHaveDuplicates();
        assertThat(second)
                .extracting(ScheduledTaskExecutionResult::id)
                .containsExactlyInAnyOrderElementsOf(
                        first.stream().map(ScheduledTaskExecutionResult::id).toList());
    }

    private static InitialSelfRegistrationRequest request(ScheduledTaskExecution... executions) {
        return new InitialSelfRegistrationRequest()
                .instanceId("3c994958-924f-4a12-87d0-a8782e97af10")
                .basicRegistrationMetadata(new BasicRegistrationMetadata()
                        .groupId("org.springframework.samples")
                        .artifactId("petclinic")
                        .insights(new Insights().scheduledTaskExecutions(List.of(executions))));
    }

    private static ScheduledTaskExecution brokenExecution() {
        return execution("com.example.BrokenJob#run()", "2026-09-27T10:00:00.123Z")
                .launchType(ScheduledTaskLaunchType.SCHEDULED);
    }

    private static ScheduledTaskExecution execution(String taskId, String startedAt) {
        return new ScheduledTaskExecution()
                .taskId(taskId)
                .startedAt(startedAt)
                .durationMillis(42L)
                .success(true);
    }
}
