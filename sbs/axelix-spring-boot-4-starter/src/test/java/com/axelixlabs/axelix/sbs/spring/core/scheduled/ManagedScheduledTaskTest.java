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
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

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
    void shouldKeepFirstOriginalTask_whenModifiedSeveralTimes() {
        // given
        FixedDelayTask original = fixedDelay(HOUR);
        ManagedScheduledTask task = managed(original);

        // when
        task.replaceScheduledState(scheduled(fixedDelay(Duration.ofHours(2))));
        task.replaceScheduledState(scheduled(fixedDelay(Duration.ofHours(3))));

        // then
        assertThat(task.getOriginalTask()).isSameAs(original);
    }

    @Test // GH-1835
    void shouldCancelPreviousScheduledTask_whenReplaced() {
        // given
        ManagedScheduledTask task = managed(fixedDelay(HOUR));
        ScheduledFuture<?> previousFuture = task.getFuture();
        ScheduledTask newScheduledTask = scheduled(fixedDelay(Duration.ofHours(2)));

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
        ManagedScheduledTask task = managed(fixedDelay(HOUR));
        ScheduledTask scheduledTaskBefore = task.getScheduledTask();

        // when
        task.replaceScheduledState(null);

        // then
        assertThat(task.getScheduledTask()).isSameAs(scheduledTaskBefore);
        assertThat(task.isEnabled()).isTrue();
        assertThat(task.getOriginalTask()).isNull();
    }

    private ManagedScheduledTask managed(FixedDelayTask task) {
        return new ManagedScheduledTask("test-task", scheduled(task));
    }

    private ScheduledTask scheduled(FixedDelayTask task) {
        return Objects.requireNonNull(registrar.scheduleFixedDelayTask(task));
    }

    private static FixedDelayTask fixedDelay(Duration interval) {
        return new FixedDelayTask(() -> {}, interval, Duration.ZERO);
    }
}
