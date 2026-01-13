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

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.nucleonforge.axelix.common.api.ServiceScheduledTasks;

/**
 * Custom actuator endpoint that provides information about {@link Scheduled @Scheduled} tasks.
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
@RestControllerEndpoint(id = "axelix-scheduled-tasks")
public class AxelixScheduledTasksEndpoint {

    private final ScheduledTaskService taskService;
    private final ServiceScheduledTasksAssembler serviceScheduledTasksAssembler;

    public AxelixScheduledTasksEndpoint(
            ScheduledTaskService taskService, ServiceScheduledTasksAssembler serviceScheduledTasksAssembler) {
        this.taskService = taskService;
        this.serviceScheduledTasksAssembler = serviceScheduledTasksAssembler;
    }

    @GetMapping
    public ServiceScheduledTasks test() {
        return serviceScheduledTasksAssembler.assemble();
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

    @PostMapping
    public ResponseEntity<Void> mutateCronExpression(@RequestBody ScheduledTaskMutationRequest request) {
        taskService.mutate(request.targetScheduledTask(), request.newValue());
        return ResponseEntity.noContent().build();
    }
}
