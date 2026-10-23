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

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.Task;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ManagedScheduledTask}, mostly for tracking of the original schedule.
 *
 * @since 23.10.2026
 * @author Dmitry Mazurov
 */
class ManagedScheduledTaskTest {

    private static final Duration HOUR = Duration.ofHours(1);

    private ThreadPoolTaskScheduler scheduler;

    private ScheduledTaskRegistrar registrar;

    @BeforeEach
    void setUp() {
        scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        registrar = new ScheduledTaskRegistrar();
        registrar.setTaskScheduler(scheduler);
    }

    @AfterEach
    void tearDown() {
        scheduler.shutdown();
    }

    @Test // GH-1835
    void shouldNotBeModified_whenNothingWasReplaced() {
        // given
        ManagedScheduledTask task = managed(fixedDelay(HOUR, Duration.ZERO));

        // then
        assertThat(task.getOriginalTask()).isNull();
    }

    @Test // GH-1835
    void shouldBeModified_whenFixedDelayIntervalDiffers() {
        // given
        FixedDelayTask original = fixedDelay(HOUR, Duration.ZERO);
        ManagedScheduledTask task = managed(original);

        // when
        replace(task, fixedDelay(Duration.ofHours(2), Duration.ZERO));

        // then
        assertThat(task.getOriginalTask()).isSameAs(original);
    }

    @Test // GH-1835
    void shouldNotBeModified_whenIntervalIsBackToOriginalValue() {
        // given
        ManagedScheduledTask task = managed(fixedRate(HOUR, Duration.ofMinutes(5)));
        replace(task, fixedRate(Duration.ofHours(2), Duration.ofMinutes(5)));

        // when: equal by value to the original one, but a different instance
        replace(task, fixedRate(HOUR, Duration.ofMinutes(5)));

        // then
        assertThat(task.getOriginalTask()).isNull();
    }

    @Test // GH-1835
    void shouldNotBeModified_whenOriginalTaskIsRestoredByTheSameInstance() {
        // given
        FixedDelayTask original = fixedDelay(HOUR, Duration.ZERO);
        ManagedScheduledTask task = managed(original);
        replace(task, fixedDelay(Duration.ofHours(2), Duration.ZERO));

        // when
        replace(task, original);

        // then
        assertThat(task.getOriginalTask()).isNull();
    }

    @Test // GH-1835
    void shouldKeepFirstOriginalTask_whenModifiedSeveralTimes() {
        // given
        FixedDelayTask original = fixedDelay(HOUR, Duration.ZERO);
        ManagedScheduledTask task = managed(original);

        // when
        replace(task, fixedDelay(Duration.ofHours(2), Duration.ZERO));
        replace(task, fixedDelay(Duration.ofHours(3), Duration.ZERO));

        // then
        assertThat(task.getOriginalTask()).isSameAs(original);
    }

    @Test // GH-1835
    void shouldCancelPreviousScheduledTask_whenReplaced() {
        // given
        ManagedScheduledTask task = managed(fixedDelay(HOUR, Duration.ZERO));
        ScheduledFuture<?> previousFuture = task.getFuture();
        ScheduledTask newScheduledTask = scheduled(fixedDelay(Duration.ofHours(2), Duration.ZERO));

        // when
        task.replaceScheduledState(newScheduledTask);

        // then
        assertThat(previousFuture.isCancelled()).isTrue();
        assertThat(task.getScheduledTask()).isSameAs(newScheduledTask);
        assertThat(task.isEnabled()).isTrue();
    }

    @Test // GH-1835
    void shouldKeepCurrentScheduledTask_whenReplacedWithNull() {
        // given
        ManagedScheduledTask task = managed(fixedDelay(HOUR, Duration.ZERO));
        ScheduledTask scheduledTaskBefore = task.getScheduledTask();

        // when
        task.replaceScheduledState(null);

        // then
        assertThat(task.getScheduledTask()).isSameAs(scheduledTaskBefore);
        assertThat(task.isEnabled()).isTrue();
        assertThat(task.getOriginalTask()).isNull();
    }

    private ManagedScheduledTask managed(Task task) {
        return new ManagedScheduledTask("test-task", scheduled(task));
    }

    private void replace(ManagedScheduledTask managedTask, Task newTask) {
        managedTask.replaceScheduledState(scheduled(newTask));
    }

    private ScheduledTask scheduled(Task task) {
        ScheduledTask scheduledTask;

        if (task instanceof FixedDelayTask fixedDelayTask) {
            scheduledTask = registrar.scheduleFixedDelayTask(fixedDelayTask);
        } else if (task instanceof FixedRateTask fixedRateTask) {
            scheduledTask = registrar.scheduleFixedRateTask(fixedRateTask);
        } else {
            throw new IllegalArgumentException("Unsupported task type: " + task.getClass());
        }

        return Objects.requireNonNull(scheduledTask);
    }

    private static FixedDelayTask fixedDelay(Duration interval, Duration initialDelay) {
        return new FixedDelayTask(() -> {}, interval, initialDelay);
    }

    private static FixedRateTask fixedRate(Duration interval, Duration initialDelay) {
        return new FixedRateTask(() -> {}, interval, initialDelay);
    }
}
