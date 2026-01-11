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
import java.util.concurrent.ScheduledFuture;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.boot.actuate.scheduling.ScheduledTasksEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.config.ScheduledTaskHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Custom actuator endpoint that provides information about {@link Scheduled @Scheduled} tasks.
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@RestControllerEndpoint(id = "axelix-scheduled-tasks")
public class AxelixScheduledTasksEndpoint {

    private final ScheduledTaskService taskService;
    private final ScheduledTasksEndpoint delegate;

    public AxelixScheduledTasksEndpoint(List<ScheduledTaskHolder> taskHolders, ScheduledTaskService taskService) {
        this.taskService = taskService;
        this.delegate = new ScheduledTasksEndpoint(taskHolders);
    }

    @GetMapping
    public ExtendedScheduledTasksDescriptor scheduledTasks() {
        ScheduledTasksEndpoint.ScheduledTasksDescriptor scheduledTasksDescriptor = delegate.scheduledTasks();

        List<ExtendedTaskDescriptor> cronTasks = enrich(scheduledTasksDescriptor.getCron());
        List<ExtendedTaskDescriptor> fixedDelayTasks = enrich(scheduledTasksDescriptor.getFixedDelay());
        List<ExtendedTaskDescriptor> fixedRateTasks = enrich(scheduledTasksDescriptor.getFixedRate());
        List<ExtendedTaskDescriptor> customTasks = enrich(scheduledTasksDescriptor.getCustom());

        return new ExtendedScheduledTasksDescriptor(cronTasks, fixedDelayTasks, fixedRateTasks, customTasks);
    }

    @PostMapping("/enable")
    public ResponseEntity<Void> enableTask(@RequestBody ScheduledTaskToggleRequest request) {
        taskService.enableTask(request.targetScheduledTask());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/disable")
    public ResponseEntity<Void> disableTask(
            @RequestBody ScheduledTaskToggleRequest request,
            @RequestParam(value = "force", defaultValue = "false") boolean force) {

        taskService.disableTask(request.targetScheduledTask(), force);
        return ResponseEntity.noContent().build();
    }

    private List<ExtendedTaskDescriptor> enrich(List<? extends ScheduledTasksEndpoint.TaskDescriptor> tasks) {
        return tasks.stream()
                .map(td -> new ExtendedTaskDescriptor(td, resolveTaskEnabledStatus(td)))
                .toList();
    }

    private boolean resolveTaskEnabledStatus(ScheduledTasksEndpoint.TaskDescriptor taskDescriptor) {
        String target = taskDescriptor.getRunnable().getTarget();

        // TODO:
        //  1. how is that possible that future will be null?
        //  2. is that correct that we're returning tru in case the task is not found? I guess no.
        return taskService
                .find(target)
                .map(task -> {
                    ScheduledFuture<?> future = task.getFuture();
                    return future == null || !future.isCancelled();
                })
                .orElse(true);
    }

    public record ExtendedScheduledTasksDescriptor(
            List<ExtendedTaskDescriptor> cron,
            List<ExtendedTaskDescriptor> fixedDelay,
            List<ExtendedTaskDescriptor> fixedRate,
            List<ExtendedTaskDescriptor> custom) {}

    public record ExtendedTaskDescriptor(ScheduledTasksEndpoint.TaskDescriptor delegate, boolean enabled) {}
}
