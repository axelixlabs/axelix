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

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskExecution;

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
    void shouldExposeTheIdAndTheQualifier() {
        ScheduledAnnotationRunnable runnable =
                new ScheduledAnnotationRunnable(() -> {}, TASK_ID, "otherScheduler", false, history);

        assertThat(runnable).hasToString(TASK_ID);
        assertThat(runnable.getQualifier()).isEqualTo("otherScheduler");
    }

    @Test
    void run_shouldExposeTheIdOfReactiveTaskWhileSubscribingOnly() {
        AtomicReference<String> taskIdWhileSubscribing = new AtomicReference<>();

        reactive(() -> taskIdWhileSubscribing.set(ScheduledAnnotationRunnable.currentReactiveTaskId()))
                .run();

        assertThat(taskIdWhileSubscribing).hasValue(TASK_ID);
        assertThat(ScheduledAnnotationRunnable.currentReactiveTaskId()).isNull();
        assertThat(history.getExecutions()).isEmpty();
    }

    @Test
    void run_shouldNotExposeTheIdOfRegularTask() {
        AtomicReference<String> taskIdWhileRunning = new AtomicReference<>("not set");

        regular(() -> taskIdWhileRunning.set(ScheduledAnnotationRunnable.currentReactiveTaskId()))
                .run();

        assertThat(taskIdWhileRunning.get()).isNull();
    }

    private ScheduledAnnotationRunnable regular(Runnable delegate) {
        return new ScheduledAnnotationRunnable(delegate, TASK_ID, null, false, history);
    }

    private ScheduledAnnotationRunnable reactive(Runnable delegate) {
        return new ScheduledAnnotationRunnable(delegate, TASK_ID, null, true, history);
    }
}
