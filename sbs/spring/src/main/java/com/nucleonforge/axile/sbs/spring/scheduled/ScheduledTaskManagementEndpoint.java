package com.nucleonforge.axile.sbs.spring.scheduled;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Custom Spring Boot Actuator endpoint
 * that exposes operations for managing scheduled tasks at runtime.
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 */
@RestControllerEndpoint(id = "scheduled-tasks-management")
public class ScheduledTaskManagementEndpoint {

    private final ScheduledTaskService taskService;

    public ScheduledTaskManagementEndpoint(ScheduledTaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/enable")
    public ResponseEntity<Void> enableTask(@RequestBody ScheduledTaskToggleRequest request) {
        taskService.enableTask(request.targetScheduledTask(), request.force());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/disable")
    public ResponseEntity<Void> disableTask(@RequestBody ScheduledTaskToggleRequest request) {
        taskService.disableTask(request.targetScheduledTask(), request.force());
        return ResponseEntity.noContent().build();
    }
}
