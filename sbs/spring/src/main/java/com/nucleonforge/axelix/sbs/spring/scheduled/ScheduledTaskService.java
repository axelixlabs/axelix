/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.sbs.spring.scheduled;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing scheduled tasks with enable/disable functionality.
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Chaerkasov
 */
public final class ScheduledTaskService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskService.class);

    private final ScheduledTasksRegistry registry;
    private final List<TaskRescheduler> taskReschedulers;

    public ScheduledTaskService(ScheduledTasksRegistry registry, List<TaskRescheduler> taskReschedulers) {
        this.registry = registry;
        this.taskReschedulers = taskReschedulers;
    }

    /**
     * Enable the task with the given id. The task will resume its
     * execution according to it's configured schedule.
     *
     * @param taskId the id of the task to enable
     */
    public void enableTask(String taskId) {
        try {
            ManagedScheduledTask task = registry.findRequired(taskId);

            for (TaskRescheduler taskRescheduler : taskReschedulers) {
                if (taskRescheduler.supports(task)) {
                    taskRescheduler.reschedule(task);
                }
            }
        } catch (ScheduledTaskNotFoundException e) {
            log.info("Failed to enable task: {}", taskId, e);
            throw e;
        }
    }

    /**
     * Find the task by the provided id.
     *
     * @param taskId the id of the task to find.
     */
    public Optional<ManagedScheduledTask> find(String taskId) {
        return registry.find(taskId);
    }

    /**
     * Disable the task with the given id.
     *
     * @param taskId the id of the task to disable.
     * @param force if {@code true}, then thread that is running the task may be
     *              interrupted (if the thread is currently executing). If {@code false},
     *              then executing thread will be waited for until the end of execution.
     */
    public void disableTask(String taskId, boolean force) {
        try {
            ManagedScheduledTask task = registry.findRequired(taskId);
            cancelTask(task, force);
            log.info("Disabled scheduled task: {}(force: {})", taskId, force);
        } catch (ScheduledTaskNotFoundException e) {
            log.info("Failed to disable task: {}", taskId, e);
            throw e;
        }
    }

    /**
     * Mutate the configuration for the given id.
     *
     * @param taskId         the id of the task to re-schedule.
     * @param newValue       the new value to apply to the task's configuration.
     */
    public void mutate(String taskId, String newValue) {
        try {
            ManagedScheduledTask task = registry.findRequired(taskId);

            for (TaskRescheduler taskRescheduler : taskReschedulers) {
                if (taskRescheduler.supports(task)) {
                    taskRescheduler.mutate(task, newValue);
                }
            }
        } catch (ScheduledTaskNotFoundException e) {
            log.info("Failed to enable task: {}", taskId, e);
            throw e;
        }
    }

    private void cancelTask(ManagedScheduledTask managedTask, boolean force) {
        ScheduledFuture<?> future = managedTask.getFuture();
        if (future != null) {
            boolean cancelled = future.cancel(force);
            log.debug(
                    "Cancelled task future: {} (mayInterrupt: {}, success: {})", managedTask.getId(), force, cancelled);
        } else {
            log.debug("No future to cancel for task: {}", managedTask.getId());
        }
    }
}
