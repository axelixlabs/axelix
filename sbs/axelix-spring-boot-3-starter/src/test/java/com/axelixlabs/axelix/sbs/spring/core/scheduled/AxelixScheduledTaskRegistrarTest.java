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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.Task;

import com.axelixlabs.axelix.common.utils.Lazy;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskExecution;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.AxelixScheduledTaskRegistrar.ScheduledAnnotationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link AxelixScheduledTaskRegistrar}.
 *
 * @author Nikita Kirillov
 */
class AxelixScheduledTaskRegistrarTest {

    private static final String METHOD = "com.example.Job.run";
    private static final Duration INTERVAL = Duration.ofHours(1);
    private static final Duration INITIAL_DELAY = Duration.ofHours(2);

    private AxelixScheduledTaskRegistrar registrar;
    private ScheduledTaskExecutionHistory history;

    @BeforeEach
    void setUp() {
        history = new ScheduledTaskExecutionHistory();
        registrar = new AxelixScheduledTaskRegistrar();
        registrar.setHistory(Lazy.resolved(history));
    }

    @AfterEach
    void tearDown() {
        registrar.destroy();
    }

    @Test
    void shouldWrapTheTaskScheduledForScheduledAnnotation() {
        // given: the task is registered before the scheduler is resolved, as Spring does on the startup
        ScheduledTask scheduledTask = scheduleFor(
                new ScheduledAnnotationContext("cron0", null, false),
                () -> registrar.scheduleCronTask(new CronTask(new MethodRunnable(), "0 0 1 * * *")));

        // when: the scheduler is resolved and the task is scheduled again
        registrar.afterPropertiesSet();

        // then
        Task task = scheduledTask.getTask();
        assertThat(task)
                .isInstanceOfSatisfying(
                        CronTask.class,
                        cronTask -> assertThat(cronTask.getExpression()).isEqualTo("0 0 1 * * *"));
        assertThat(task.getRunnable()).hasToString(METHOD + "#cron0");

        task.getRunnable().run();
        assertThat(history.getExecutions())
                .extracting(ScheduledTaskExecution::getTaskId)
                .containsExactly(METHOD + "#cron0");
    }

    @Test
    void shouldKeepTheIntervalsOfTheWrappedTasks() {
        ScheduledTask fixedDelayTask = scheduleFor(
                new ScheduledAnnotationContext("fixedDelay0", null, false),
                () -> registrar.scheduleFixedDelayTask(
                        new FixedDelayTask(new MethodRunnable(), INTERVAL, INITIAL_DELAY)));
        ScheduledTask fixedRateTask = scheduleFor(
                new ScheduledAnnotationContext("fixedRate0", null, false),
                () -> registrar.scheduleFixedRateTask(
                        new FixedRateTask(new MethodRunnable(), INTERVAL, INITIAL_DELAY)));

        registrar.afterPropertiesSet();

        assertThat(List.of(fixedDelayTask.getTask(), fixedRateTask.getTask()))
                .allSatisfy(task -> assertThat(task).isInstanceOfSatisfying(IntervalTask.class, intervalTask -> {
                    assertThat(intervalTask.getIntervalDuration()).isEqualTo(INTERVAL);
                    assertThat(intervalTask.getInitialDelayDuration()).isEqualTo(INITIAL_DELAY);
                }))
                .extracting(task -> task.getRunnable().toString())
                .containsExactly(METHOD + "#fixedDelay0", METHOD + "#fixedRate0");
        assertThat(fixedDelayTask.getTask()).isInstanceOf(FixedDelayTask.class);
        assertThat(fixedRateTask.getTask()).isInstanceOf(FixedRateTask.class);
    }

    @Test
    void shouldNotWrapTheTaskScheduledOutsideOfScheduledAnnotation() {
        // given: e.g. the task registered via SchedulingConfigurer
        ScheduledTask scheduledTask = registrar.scheduleCronTask(new CronTask(new MethodRunnable(), "0 0 1 * * *"));

        // when
        registrar.afterPropertiesSet();

        // then
        assertThat(scheduledTask).isNotNull();
        Runnable runnable = scheduledTask.getTask().getRunnable();
        assertThat(runnable).hasToString(METHOD);

        runnable.run();
        assertThat(history.getExecutions()).isEmpty();
    }

    @Test // the tests run on Spring Framework 6.0, which does not support the reactive tasks
    void afterPropertiesSet_shouldNotSetUpTheRecordingRegistryWithoutTheSupportOfTheReactiveTasks() {
        registrar.afterPropertiesSet();

        assertThat(registrar.getObservationRegistry()).isNull();
    }

    private ScheduledTask scheduleFor(ScheduledAnnotationContext annotation, Supplier<ScheduledTask> scheduling) {
        AtomicReference<ScheduledTask> scheduledTask = new AtomicReference<>();
        registrar.process(annotation, () -> scheduledTask.set(scheduling.get()));
        assertThat(scheduledTask.get()).isNotNull();
        return scheduledTask.get();
    }

    private static final class MethodRunnable implements Runnable {

        @Override
        public void run() {
            // nothing to do
        }

        @Override
        public String toString() {
            return METHOD;
        }
    }
}
