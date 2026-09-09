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
package com.axelixlabs.axelix.master.api.external.endpoint;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * The API for thread dump.
 *
 * @since 18.11.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@ExternalApiRestController
public class ThreadDumpApi {

    private final EndpointInvoker endpointInvoker;

    public ThreadDumpApi(EndpointInvoker endpointInvoker) {
        this.endpointInvoker = endpointInvoker;
    }

    @GetMapping(ApiPaths.ThreadDumpApi.INSTANCE_ID)
    public ResponseEntity<byte[]> getThreadDump(@PathVariable("instanceId") String instanceId) {
        byte[] body = endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.GET_THREAD_DUMP, NoHttpPayload.INSTANCE);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @PostMapping(ApiPaths.ThreadDumpApi.ENABLE_CONTENTION_MONITORING)
    public void enableContentionMonitoring(@PathVariable("instanceId") String instanceId) {
        endpointInvoker.invokeNoValue(
                InstanceId.of(instanceId),
                ActuatorEndpoints.THREAD_DUMP_ENABLE_CONTENTION_MONITORING,
                NoHttpPayload.INSTANCE);
    }

    @PostMapping(ApiPaths.ThreadDumpApi.DISABLE_CONTENTION_MONITORING)
    public void disableContentionMonitoring(@PathVariable("instanceId") String instanceId) {
        endpointInvoker.invokeNoValue(
                InstanceId.of(instanceId),
                ActuatorEndpoints.THREAD_DUMP_DISABLE_CONTENTION_MONITORING,
                NoHttpPayload.INSTANCE);
    }
}
