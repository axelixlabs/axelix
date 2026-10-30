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

import java.util.concurrent.atomic.AtomicReference;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskExecution;
import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskLaunchType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ScheduledTaskExecutionObservationHandler}.
 *
 * @author Nikita Kirillov
 */
class ScheduledTaskExecutionObservationHandlerTest {

    private static final String TASK_ID = "com.example.Job.run#cron0";
    private static final long SLEEP_MILLIS = 30L;
    private static final String OBSERVATION = "tasks.scheduled.execution";

    private ScheduledTaskExecutionHistory history;
    private ScheduledTaskExecutionObservationHandler handler;

    @BeforeEach
    void setUp() {
        history = new ScheduledTaskExecutionHistory();
        handler = new ScheduledTaskExecutionObservationHandler(history);
    }

    @Test
    void shouldRecordTheExecutionOfReactiveTaskWhenItsPublisherCompletes() throws InterruptedException {
        Observation.Context context = new Observation.Context();
        reactive(() -> handler.onStart(context)).run();
        Thread.sleep(SLEEP_MILLIS);

        context.setError(new IllegalStateException("reactive boom"));
        handler.onStop(context);

        assertThat(history.getExecutions()).hasSize(1);
        ScheduledTaskExecution execution = history.getExecutions().get(0);
        assertThat(execution.getTaskId()).isEqualTo(TASK_ID);
        assertThat(execution.getSuccess()).isFalse();
        assertThat(execution.getErrorType()).isEqualTo("IllegalStateException");
        assertThat(execution.getErrorMessage()).isEqualTo("reactive boom");
        assertThat(execution.getDurationMillis()).isGreaterThanOrEqualTo(SLEEP_MILLIS);
        assertThat(execution.getLaunchType()).isEqualTo(ScheduledTaskLaunchType.SCHEDULED);
    }

    @Test
    void shouldRecordTheManualLaunchOfReactiveTask() {
        Observation.Context context = new Observation.Context();
        reactive(() -> handler.onStart(context)).manual().run();

        handler.onStop(context);

        assertThat(history.getExecutions())
                .extracting(ScheduledTaskExecution::getLaunchType)
                .containsExactly(ScheduledTaskLaunchType.MANUAL);
    }

    @Test
    void shouldRecordTheExecutionOnceWhenTheObservationIsStoppedTwice() {
        Observation.Context context = new Observation.Context();
        reactive(() -> handler.onStart(context)).run();

        handler.onStop(context);
        handler.onStop(context);

        assertThat(history.getExecutions()).hasSize(1);
    }

    @Test
    void shouldNotRecordTheObservationsStartedOutsideOfReactiveTask() {
        Observation.Context context = new Observation.Context();

        handler.onStart(context);
        handler.onStop(context);

        assertThat(history.getExecutions()).isEmpty();
    }

    @Test
    void shouldNotSupportTheObservationsOfOtherKinds() {
        assertThat(handler.supportsContext(new Observation.Context())).isFalse();
    }

    @Test
    void recording_shouldMakeOwnRegistryObserveOnlyTheReactiveTasks() {
        ObservationRegistry registry = ScheduledTaskExecutionObservationHandler.recording(null, history);
        AtomicReference<Observation> reactiveTaskObservation = new AtomicReference<>();

        reactive(() -> reactiveTaskObservation.set(Observation.createNotStarted(OBSERVATION, registry)))
                .run();

        assertThat(reactiveTaskObservation.get().isNoop()).isFalse();
        assertThat(Observation.createNotStarted(OBSERVATION, registry).isNoop()).isTrue();
    }

    @Test
    void recording_shouldReplaceTheNoopRegistryOfTheApplicationWithOwnOne() {
        ObservationRegistry registry =
                ScheduledTaskExecutionObservationHandler.recording(ObservationRegistry.NOOP, history);

        assertThat(registry).isNotSameAs(ObservationRegistry.NOOP);
        assertThat(registry.isNoop()).isFalse();
    }

    @Test
    void recording_shouldKeepTheRegistryOfTheApplicationObservingEverything() {
        ObservationRegistry applicationRegistry = ObservationRegistry.create();

        ObservationRegistry registry = ScheduledTaskExecutionObservationHandler.recording(applicationRegistry, history);

        assertThat(registry).isSameAs(applicationRegistry);
        assertThat(Observation.createNotStarted(OBSERVATION, registry).isNoop()).isFalse();
    }

    @Test
    void observesScheduledTasks_shouldBeTrueForTheRegistryOfTheApplication() {
        ObservationRegistry registry =
                ScheduledTaskExecutionObservationHandler.recording(ObservationRegistry.create(), history);

        assertThat(ScheduledTaskExecutionObservationHandler.observesScheduledTasks(registry))
                .isTrue();
    }

    @Test
    void observesScheduledTasks_shouldBeFalseWhenTheApplicationDisablesTheObservationsOfTheScheduledTasks() {
        ObservationRegistry applicationRegistry = ObservationRegistry.create();
        applicationRegistry.observationConfig().observationPredicate((name, context) -> !name.equals(OBSERVATION));
        ObservationRegistry registry = ScheduledTaskExecutionObservationHandler.recording(applicationRegistry, history);

        assertThat(ScheduledTaskExecutionObservationHandler.observesScheduledTasks(registry))
                .isFalse();
    }

    @Test
    void observesScheduledTasks_shouldBeTrueWhenThePredicateOfTheApplicationFailsOnThePlainContext() {
        ObservationRegistry applicationRegistry = ObservationRegistry.create();
        applicationRegistry.observationConfig().observationPredicate((name, context) -> {
            throw new ClassCastException("not the context of Spring");
        });
        ObservationRegistry registry = ScheduledTaskExecutionObservationHandler.recording(applicationRegistry, history);

        assertThat(ScheduledTaskExecutionObservationHandler.observesScheduledTasks(registry))
                .isTrue();
    }

    private ScheduledAnnotationRunnable reactive(Runnable delegate) {
        return new ScheduledAnnotationRunnable(delegate, TASK_ID, null, true, history);
    }
}
