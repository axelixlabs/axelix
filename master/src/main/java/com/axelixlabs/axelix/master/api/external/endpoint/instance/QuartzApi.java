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
package com.axelixlabs.axelix.master.api.external.endpoint.instance;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.axelixlabs.axelix.common.contract.quartz.QuartzCronExpressionValidationRequest;
import com.axelixlabs.axelix.common.contract.quartz.QuartzJobExecuteRequest;
import com.axelixlabs.axelix.common.contract.quartz.QuartzJobPauseRequest;
import com.axelixlabs.axelix.common.contract.quartz.QuartzJobResumeRequest;
import com.axelixlabs.axelix.common.contract.quartz.QuartzJobsRequest;
import com.axelixlabs.axelix.common.contract.quartz.QuartzTriggerPauseRequest;
import com.axelixlabs.axelix.common.contract.quartz.QuartzTriggerResumeRequest;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.request.QuartzSimpleTriggerIntervalValidationRequest;
import com.axelixlabs.axelix.master.api.external.response.QuartzSimpleTriggerIntervalValidationResponse;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.domain.instance.InstanceId;
import com.axelixlabs.axelix.master.service.serde.JacksonMessageSerializationStrategy;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * The API for managing Quartz schedulers and jobs.
 *
 * @author Nikita Kirillov
 */
@ExternalApiRestController
public class QuartzApi {

    private final EndpointInvoker endpointInvoker;

    private final JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy;

    public QuartzApi(
            EndpointInvoker endpointInvoker, JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy) {
        this.endpointInvoker = endpointInvoker;
        this.jacksonMessageSerializationStrategy = jacksonMessageSerializationStrategy;
    }

    @GetMapping(path = ApiPaths.QuartzApi.SCHEDULERS)
    public ResponseEntity<byte[]> getQuartzSchedulers(@PathVariable("instanceId") String instanceId) {
        byte[] body = endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.GET_QUARTZ_SCHEDULERS, NoHttpPayload.INSTANCE);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @PostMapping(path = ApiPaths.QuartzApi.JOBS)
    public ResponseEntity<byte[]> getQuartzJobs(
            @PathVariable("instanceId") String instanceId, @RequestBody QuartzJobsRequest request) {
        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        byte[] body = endpointInvoker.invoke(InstanceId.of(instanceId), ActuatorEndpoints.GET_QUARTZ_JOBS, payload);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @PostMapping(path = ApiPaths.QuartzApi.EXECUTE_JOB)
    public ResponseEntity<Void> executeQuartzJob(
            @PathVariable("instanceId") String instanceId, @RequestBody QuartzJobExecuteRequest request) {
        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.EXECUTE_QUARTZ_JOB, payload);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = ApiPaths.QuartzApi.PAUSE_JOB)
    public ResponseEntity<Void> pauseQuartzJob(
            @PathVariable("instanceId") String instanceId, @RequestBody QuartzJobPauseRequest request) {
        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.PAUSE_QUARTZ_JOB, payload);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = ApiPaths.QuartzApi.RESUME_JOB)
    public ResponseEntity<Void> resumeQuartzJob(
            @PathVariable("instanceId") String instanceId, @RequestBody QuartzJobResumeRequest request) {
        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.RESUME_QUARTZ_JOB, payload);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = ApiPaths.QuartzApi.PAUSE_TRIGGER)
    public ResponseEntity<Void> pauseQuartzTrigger(
            @PathVariable("instanceId") String instanceId, @RequestBody QuartzTriggerPauseRequest request) {
        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.PAUSE_QUARTZ_TRIGGER, payload);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = ApiPaths.QuartzApi.RESUME_TRIGGER)
    public ResponseEntity<Void> resumeQuartzTrigger(
            @PathVariable("instanceId") String instanceId, @RequestBody QuartzTriggerResumeRequest request) {
        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.RESUME_QUARTZ_TRIGGER, payload);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = ApiPaths.QuartzApi.VALIDATE_CRON_EXPRESSION)
    public ResponseEntity<byte[]> validateCronExpression(
            @PathVariable("instanceId") String instanceId, @RequestBody QuartzCronExpressionValidationRequest request) {
        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        byte[] body = endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.VALIDATE_QUARTZ_CRON_EXPRESSION, payload);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @PostMapping(path = ApiPaths.QuartzApi.VALIDATE_SIMPLE_TRIGGER_INTERVAL)
    public ResponseEntity<QuartzSimpleTriggerIntervalValidationResponse> validateSimpleTriggerInterval(
            @RequestBody QuartzSimpleTriggerIntervalValidationRequest request) {
        if (request.interval() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(new QuartzSimpleTriggerIntervalValidationResponse(request.interval() > 0));
    }
}
