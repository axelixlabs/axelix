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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import org.springframework.scheduling.SchedulingAwareRunnable;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskExecution;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskLaunchType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ScheduledAnnotationRunnable}.
 *
 * @author Nikita Kirillov
 */
class ScheduledAnnotationRunnableTest {

    private static final String TASK_ID = "com.example.Job.run#cron0";

    private final ScheduledTaskExecutionHistory history = new ScheduledTaskExecutionHistory();

    @Test
    void run_shouldRunTheDelegateAndRecordTheExecutionUnderTheIdOfTheTask() {
        AtomicBoolean ran = new AtomicBoolean();

        regular(() -> ran.set(true)).run();

        assertThat(ran).isTrue();
        assertThat(history.getExecutions())
                .extracting(ScheduledTaskExecution::getTaskId)
                .containsExactly(TASK_ID);
    }

    @Test
    void shouldExposeTheIdAndTheQualifierOfTheDelegate() {
        ScheduledAnnotationRunnable runnable = regular(new QualifiedRunnable("otherScheduler"));

        assertThat(runnable).hasToString(TASK_ID);
        assertThat(runnable.getQualifier()).isEqualTo("otherScheduler");
    }

    @Test
    void shouldHaveNoQualifierWhenTheDelegateIsPlainRunnable() {
        assertThat(regular(() -> {}).getQualifier()).isNull();
    }

    @Test
    void run_shouldExposeTheReactiveTaskWhileSubscribingOnly() {
        AtomicReference<ScheduledAnnotationRunnable> taskWhileSubscribing = new AtomicReference<>();
        ScheduledAnnotationRunnable reactive =
                reactive(() -> taskWhileSubscribing.set(ScheduledAnnotationRunnable.currentReactiveTask()));

        reactive.run();

        assertThat(taskWhileSubscribing).hasValue(reactive);
        assertThat(ScheduledAnnotationRunnable.currentReactiveTask()).isNull();
        assertThat(history.getExecutions()).isEmpty();
    }

    @Test
    void run_shouldNotExposeTheRegularTask() {
        AtomicReference<ScheduledAnnotationRunnable> taskWhileRunning = new AtomicReference<>(regular(() -> {}));

        regular(() -> taskWhileRunning.set(ScheduledAnnotationRunnable.currentReactiveTask()))
                .run();

        assertThat(taskWhileRunning.get()).isNull();
    }

    @Test
    void run_shouldRecordTheScheduledLaunch() {
        regular(() -> {}).run();

        assertThat(history.getExecutions())
                .extracting(ScheduledTaskExecution::getLaunchType)
                .containsExactly(ScheduledTaskLaunchType.SCHEDULED);
    }

    @Test
    void manual_shouldKeepTheTaskAndRecordTheManualLaunch() {
        ScheduledAnnotationRunnable manual =
                regular(new QualifiedRunnable("otherScheduler")).manual();

        manual.run();

        assertThat(manual).hasToString(TASK_ID);
        assertThat(manual.getQualifier()).isEqualTo("otherScheduler");
        assertThat(history.getExecutions())
                .extracting(ScheduledTaskExecution::getLaunchType)
                .containsExactly(ScheduledTaskLaunchType.MANUAL);
    }

    private ScheduledAnnotationRunnable regular(Runnable delegate) {
        return new ScheduledAnnotationRunnable(delegate, TASK_ID, false, history);
    }

    private ScheduledAnnotationRunnable reactive(Runnable delegate) {
        return new ScheduledAnnotationRunnable(delegate, TASK_ID, true, history);
    }

    private record QualifiedRunnable(@Nullable String qualifier) implements SchedulingAwareRunnable {

        @Override
        public void run() {}

        @Override
        public @Nullable String getQualifier() {
            return qualifier;
        }
    }
}
