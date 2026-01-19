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
package com.nucleonforge.axelix.sbs.spring.scheduled;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.nucleonforge.axelix.common.api.ServiceScheduledTasks;
import com.nucleonforge.axelix.common.api.request.ScheduledTaskCronExpressionModifyRequest;
import com.nucleonforge.axelix.common.api.request.ScheduledTaskIntervalModifyRequest;
import com.nucleonforge.axelix.common.api.request.ScheduledTaskRunNowRequest;
import com.nucleonforge.axelix.common.api.request.ScheduledTaskToggleRequest;

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
    private final ScheduledTasksAssembler scheduledTasksAssembler;

    public AxelixScheduledTasksEndpoint(
            ScheduledTaskService taskService, ScheduledTasksAssembler scheduledTasksAssembler) {
        this.taskService = taskService;
        this.scheduledTasksAssembler = scheduledTasksAssembler;
    }

    @GetMapping
    public ServiceScheduledTasks getScheduledTasks() {
        return scheduledTasksAssembler.assemble();
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

    @PostMapping("/modify/cron-expression")
    public ResponseEntity<Void> modifyCronExpression(@RequestBody ScheduledTaskCronExpressionModifyRequest request) {
        taskService.modifyCronExpression(request.targetScheduledTask(), request.cronExpression());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/modify/interval")
    public ResponseEntity<Void> modifyInterval(@RequestBody ScheduledTaskIntervalModifyRequest request) {
        taskService.modifyInterval(request.targetScheduledTask(), request.interval());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/run-now")
    public ResponseEntity<Void> runNowTusk(@RequestBody ScheduledTaskRunNowRequest request) {
        taskService.runNowTask(request.targetScheduledTask());
        return ResponseEntity.noContent().build();
    }
}
