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

import java.time.Instant;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import org.jspecify.annotations.Nullable;

import org.springframework.scheduling.support.ScheduledTaskObservationContext;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskLaunchType;

/**
 * Records the executions of the reactive {@code @Scheduled} tasks out of the observations that Spring Framework makes
 * for them. Only the tasks wrapped into the {@link ScheduledAnnotationRunnable} are recorded.
 *
 * @author Nikita Kirillov
 */
public class ScheduledTaskExecutionObservationHandler implements ObservationHandler<ScheduledTaskObservationContext> {

    private static final String SCHEDULED_TASK_OBSERVATION = "tasks.scheduled.execution";

    private final ScheduledTaskExecutionHistory history;

    public ScheduledTaskExecutionObservationHandler(ScheduledTaskExecutionHistory history) {
        this.history = history;
    }

    /**
     * @return the given registry, or a new one if it is absent or no-op, with this handler added. The new one observes
     *         only the reactive tasks, so that the other tasks stay unobserved, as without it.
     */
    static ObservationRegistry recording(
            @Nullable ObservationRegistry registry, ScheduledTaskExecutionHistory history) {
        ObservationRegistry recording = registry;
        if (recording == null || recording == ObservationRegistry.NOOP) {
            recording = ObservationRegistry.create();
            recording
                    .observationConfig()
                    .observationPredicate((name, context) -> ScheduledAnnotationRunnable.currentReactiveTask() != null);
        }
        recording.observationConfig().observationHandler(new ScheduledTaskExecutionObservationHandler(history));
        return recording;
    }

    static boolean observesScheduledTasks(ObservationRegistry registry) {
        try {
            return !Observation.createNotStarted(SCHEDULED_TASK_OBSERVATION, registry)
                    .isNoop();
        } catch (RuntimeException e) {
            // a predicate of the application may expect the context of Spring rather than the plain one of this probe
            return true;
        }
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof ScheduledTaskObservationContext;
    }

    @Override
    public void onStart(ScheduledTaskObservationContext context) {
        ScheduledAnnotationRunnable task = ScheduledAnnotationRunnable.currentReactiveTask();
        if (task != null) {
            context.put(
                    ReactiveExecution.class,
                    new ReactiveExecution(task.toString(), task.getLaunchType(), Instant.now(), System.nanoTime()));
        }
    }

    @Override
    public void onStop(ScheduledTaskObservationContext context) {
        if (!context.isComplete() && context.getError() == null) {
            return;
        }
        if (context.remove(ReactiveExecution.class) instanceof ReactiveExecution execution) {
            long durationMillis = (System.nanoTime() - execution.startNanos()) / 1_000_000;
            history.record(ScheduledTaskExecutions.of(
                    execution.taskId(),
                    execution.launchType(),
                    execution.startedAt(),
                    durationMillis,
                    context.getError()));
        }
    }

    private record ReactiveExecution(
            String taskId, ScheduledTaskLaunchType launchType, Instant startedAt, long startNanos) {}
}
