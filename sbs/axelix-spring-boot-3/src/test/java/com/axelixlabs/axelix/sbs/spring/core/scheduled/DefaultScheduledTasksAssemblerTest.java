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

import java.lang.reflect.Constructor;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.Task;
import org.springframework.scheduling.config.TriggerTask;

import com.axelixlabs.axelix.common.api.ServiceScheduledTasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultScheduledTasksAssembler}.
 *
 * @author Aleksei Ermakov
 */
@ExtendWith(MockitoExtension.class)
class DefaultScheduledTasksAssemblerTest {

    @Mock
    private ScheduledTasksRegistry registry;

    private DefaultScheduledTasksAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new DefaultScheduledTasksAssembler(registry);
    }

    /**
     * Creates a {@link ScheduledTask} via reflection since its constructor is package-private.
     */
    private static ScheduledTask newScheduledTask(Task task) {
        try {
            Constructor<ScheduledTask> ctor = ScheduledTask.class.getDeclaredConstructor(Task.class);
            ctor.setAccessible(true);
            return ctor.newInstance(task);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate ScheduledTask", e);
        }
    }

    private ManagedScheduledTask buildManagedTask(Task task, Runnable runnable, boolean enabled, long delayMs) {
        ManagedScheduledTask managedTask = mock(ManagedScheduledTask.class);
        ScheduledFuture<?> future = mock(ScheduledFuture.class);
        ScheduledTask scheduledTask = newScheduledTask(task);

        when(managedTask.getScheduledTask()).thenReturn(scheduledTask);
        when(managedTask.getRunnable()).thenReturn(runnable);
        when(managedTask.isEnabled()).thenReturn(enabled);
        if (enabled) {
            doReturn(future).when(managedTask).getFuture();
            when(future.getDelay(TimeUnit.MILLISECONDS)).thenReturn(delayMs);
        }

        return managedTask;
    }

    @Nested
    class WhenTaskHasNeverRun {

        @Test
        void lastExecutionIsNull() {
            // given.
            TrackingRunnable tracking = new TrackingRunnable(() -> {});
            CronTask cronTask = new CronTask(tracking, "*/1 * * * * *");
            ManagedScheduledTask managedTask = buildManagedTask(cronTask, tracking, true, 5000L);
            when(registry.getAll()).thenReturn(List.of(managedTask));

            // when.
            ServiceScheduledTasks result = assembler.assemble();

            // then.
            assertThat(result.getCron()).hasSize(1);
            assertThat(result.getCron().get(0).getLastExecution()).isNull();
        }
    }

    @Nested
    class WhenTaskRanSuccessfully {

        @Test
        void lastExecutionStatusIsSuccess() {
            // given.
            TrackingRunnable tracking = new TrackingRunnable(() -> {});
            tracking.run();
            CronTask cronTask = new CronTask(tracking, "*/1 * * * * *");
            ManagedScheduledTask managedTask = buildManagedTask(cronTask, tracking, true, 5000L);
            when(registry.getAll()).thenReturn(List.of(managedTask));

            // when.
            ServiceScheduledTasks result = assembler.assemble();

            // then.
            assertThat(result.getCron()).hasSize(1);
            ServiceScheduledTasks.LastExecution lastExecution =
                    result.getCron().get(0).getLastExecution();
            assertThat(lastExecution).isNotNull();
            assertThat(lastExecution.getStatus()).isEqualTo("SUCCESS");
            assertThat(lastExecution.getTime()).isNotNull();
            assertThat(lastExecution.getException()).isNull();
        }
    }

    @Nested
    class WhenTaskFailed {

        @Test
        void lastExecutionStatusIsErrorWithExceptionDetails() {
            // given.
            RuntimeException cause = new RuntimeException("task exploded");
            TrackingRunnable tracking = new TrackingRunnable(() -> {
                throw cause;
            });
            try {
                tracking.run();
            } catch (RuntimeException ignored) {
                // expected – we just want to set lastStatus = "ERROR"
            }
            CronTask cronTask = new CronTask(tracking, "*/1 * * * * *");
            ManagedScheduledTask managedTask = buildManagedTask(cronTask, tracking, true, 5000L);
            when(registry.getAll()).thenReturn(List.of(managedTask));

            // when.
            ServiceScheduledTasks result = assembler.assemble();

            // then.
            assertThat(result.getCron()).hasSize(1);
            ServiceScheduledTasks.LastExecution lastExecution =
                    result.getCron().get(0).getLastExecution();
            assertThat(lastExecution).isNotNull();
            assertThat(lastExecution.getStatus()).isEqualTo("ERROR");
            assertThat(lastExecution.getTime()).isNotNull();
            assertThat(lastExecution.getException()).isNotNull();
            assertThat(lastExecution.getException().getType()).isEqualTo(RuntimeException.class.getName());
            assertThat(lastExecution.getException().getMessage()).isEqualTo("task exploded");
        }
    }

    @Nested
    class NextExecution {

        @Test
        void isPopulatedForEnabledTask() {
            // given.
            TrackingRunnable tracking = new TrackingRunnable(() -> {});
            CronTask cronTask = new CronTask(tracking, "*/1 * * * * *");
            ManagedScheduledTask managedTask = buildManagedTask(cronTask, tracking, true, 5000L);
            when(registry.getAll()).thenReturn(List.of(managedTask));

            // when.
            ServiceScheduledTasks result = assembler.assemble();

            // then.
            assertThat(result.getCron()).hasSize(1);
            assertThat(result.getCron().get(0).getNextExecution()).isNotNull();
        }

        @Test
        void isNullForDisabledTask() {
            // given.
            TrackingRunnable tracking = new TrackingRunnable(() -> {});
            CronTask cronTask = new CronTask(tracking, "*/1 * * * * *");
            ManagedScheduledTask managedTask = buildManagedTask(cronTask, tracking, false, 0L);
            when(registry.getAll()).thenReturn(List.of(managedTask));

            // when.
            ServiceScheduledTasks result = assembler.assemble();

            // then.
            assertThat(result.getCron()).hasSize(1);
            assertThat(result.getCron().get(0).getNextExecution()).isNull();
        }

        @Test
        void isNullWhenDelayIsNegative() {
            // given.
            TrackingRunnable tracking = new TrackingRunnable(() -> {});
            CronTask cronTask = new CronTask(tracking, "*/1 * * * * *");
            ManagedScheduledTask managedTask = buildManagedTask(cronTask, tracking, true, -1L);
            when(registry.getAll()).thenReturn(List.of(managedTask));

            // when.
            ServiceScheduledTasks result = assembler.assemble();

            // then.
            assertThat(result.getCron()).hasSize(1);
            assertThat(result.getCron().get(0).getNextExecution()).isNull();
        }
    }

    @Nested
    class TaskTypes {

        @Test
        void shouldAssembleAllTaskTypes() {
            // given.
            Runnable cronRunnable = namedRunnable("cron-target");
            Runnable fixedRateRunnable = namedRunnable("fixed-rate-target");
            Runnable fixedDelayRunnable = namedRunnable("fixed-delay-target");
            Runnable customRunnable = namedRunnable("custom-target");

            CronTask cronTask = new CronTask(cronRunnable, "*/1 * * * * *");
            FixedRateTask fixedRateTask =
                    new FixedRateTask(fixedRateRunnable, Duration.ofMillis(2000L), Duration.ofMillis(100L));
            FixedDelayTask fixedDelayTask =
                    new FixedDelayTask(fixedDelayRunnable, Duration.ofMillis(3000L), Duration.ofMillis(200L));
            TriggerTask customTask = new TriggerTask(customRunnable, triggerContext -> null);

            ManagedScheduledTask managedCronTask = buildManagedTask(cronTask, cronRunnable, true, 5000L);
            ManagedScheduledTask managedFixedRateTask = buildManagedTask(fixedRateTask, fixedRateRunnable, true, 5000L);
            ManagedScheduledTask managedFixedDelayTask =
                    buildManagedTask(fixedDelayTask, fixedDelayRunnable, true, 5000L);
            ManagedScheduledTask managedCustomTask = buildManagedTask(customTask, customRunnable, true, 5000L);
            when(registry.getAll())
                    .thenReturn(
                            List.of(managedCronTask, managedFixedRateTask, managedFixedDelayTask, managedCustomTask));

            // when.
            ServiceScheduledTasks result = assembler.assemble();

            // then.
            assertThat(result.getCron())
                    .singleElement()
                    .extracting(it -> it.getRunnable().getTarget())
                    .isEqualTo("cron-target");
            assertThat(result.getFixedRate())
                    .singleElement()
                    .extracting(it -> it.getRunnable().getTarget())
                    .isEqualTo("fixed-rate-target");
            assertThat(result.getFixedDelay())
                    .singleElement()
                    .extracting(it -> it.getRunnable().getTarget())
                    .isEqualTo("fixed-delay-target");
            assertThat(result.getCustom())
                    .singleElement()
                    .extracting(it -> it.getRunnable().getTarget())
                    .isEqualTo("custom-target");
        }
    }

    private Runnable namedRunnable(String name) {
        return new Runnable() {
            @Override
            public void run() {}

            @Override
            public String toString() {
                return name;
            }
        };
    }
}
