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

import org.junit.jupiter.api.Test;

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

        new ScheduledAnnotationRunnable(() -> ran.set(true), TASK_ID, history).run();

        assertThat(ran).isTrue();
        assertThat(history.getExecutions())
                .extracting(ScheduledTaskExecution::getTaskId)
                .containsExactly(TASK_ID);
    }

    @Test
    void run_shouldRecordTheScheduledLaunch() {
        new ScheduledAnnotationRunnable(() -> {}, TASK_ID, history).run();

        assertThat(history.getExecutions())
                .extracting(ScheduledTaskExecution::getLaunchType)
                .containsExactly(ScheduledTaskLaunchType.SCHEDULED);
    }

    @Test
    void manual_shouldRunTheSameTaskAndRecordTheManualLaunch() {
        AtomicBoolean ran = new AtomicBoolean();
        ScheduledAnnotationRunnable manual =
                new ScheduledAnnotationRunnable(() -> ran.set(true), TASK_ID, history).manual();

        manual.run();

        assertThat(ran).isTrue();
        assertThat(manual).hasToString(TASK_ID);
        assertThat(history.getExecutions())
                .extracting(ScheduledTaskExecution::getLaunchType)
                .containsExactly(ScheduledTaskLaunchType.MANUAL);
    }

    @Test
    void shouldExposeTheId() {
        assertThat(new ScheduledAnnotationRunnable(() -> {}, TASK_ID, history)).hasToString(TASK_ID);
    }
}
