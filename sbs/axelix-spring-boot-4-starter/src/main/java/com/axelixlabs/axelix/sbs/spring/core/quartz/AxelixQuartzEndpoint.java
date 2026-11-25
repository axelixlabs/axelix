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
package com.axelixlabs.axelix.sbs.spring.core.quartz;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.axelixlabs.axelix.common.contract.quartz.QuartzJobExecuteRequest;
import com.axelixlabs.axelix.common.contract.quartz.QuartzTriggerPauseRequest;
import com.axelixlabs.axelix.common.contract.quartz.QuartzTriggerResumeRequest;
import com.axelixlabs.axelix.common.contract.quartz.ServiceQuartzJobs;
import com.axelixlabs.axelix.common.contract.quartz.ServiceQuartzSchedulers;

/**
 * Custom Actuator endpoint that provides Quartz schedulers and jobs.
 *
 * @author Nikita Kirillov
 */
@RestControllerEndpoint(id = "axelix-quartz")
public class AxelixQuartzEndpoint {

    private final AxelixQuartzService quartzService;

    public AxelixQuartzEndpoint(AxelixQuartzService quartzService) {
        this.quartzService = quartzService;
    }

    @GetMapping
    public ServiceQuartzSchedulers getQuartzSchedulers() {
        return quartzService.getQuartzSchedulers();
    }

    @GetMapping("/jobs/{schedulerName}")
    public ResponseEntity<ServiceQuartzJobs> getQuartzJobs(@PathVariable String schedulerName) {
        ServiceQuartzJobs jobs = quartzService.getQuartzJobs(schedulerName);
        return jobs == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(jobs);
    }

    @PostMapping("/execute")
    public ResponseEntity<Void> executeQuartzJob(@RequestBody QuartzJobExecuteRequest request) {
        if (request.getSchedulerName() == null || request.getJobName() == null || request.getJobGroup() == null) {
            return ResponseEntity.badRequest().build();
        }
        boolean updated =
                quartzService.executeQuartzJob(request.getSchedulerName(), request.getJobName(), request.getJobGroup());
        return updated
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/pause")
    public ResponseEntity<Void> pauseQuartzTrigger(@RequestBody QuartzTriggerPauseRequest request) {
        if (request.getSchedulerName() == null
                || request.getTriggerName() == null
                || request.getTriggerGroup() == null) {
            return ResponseEntity.badRequest().build();
        }
        boolean updated = quartzService.pauseQuartzTrigger(
                request.getSchedulerName(), request.getTriggerName(), request.getTriggerGroup());
        return updated
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/resume")
    public ResponseEntity<Void> resumeQuartzTrigger(@RequestBody QuartzTriggerResumeRequest request) {
        if (request.getSchedulerName() == null
                || request.getTriggerName() == null
                || request.getTriggerGroup() == null) {
            return ResponseEntity.badRequest().build();
        }
        boolean updated = quartzService.resumeQuartzTrigger(
                request.getSchedulerName(), request.getTriggerName(), request.getTriggerGroup());
        return updated
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
