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

import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.axelixlabs.axelix.common.api.ThreadDumpFeed;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.response.ThreadDumpFeedResponse;
import com.axelixlabs.axelix.master.api.external.swagger.DefaultApiResponse;
import com.axelixlabs.axelix.master.api.external.swagger.InstanceIdParameter;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.convert.response.Converter;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * The API for thread dump.
 *
 * @since 18.11.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@Tag(
        name = "Thread Dump API",
        description = "The threaddump endpoint provides access to the thread dump of the application’s JVM.")
@ExternalApiRestController
@RequestMapping(path = ApiPaths.ThreadDumpApi.MAIN)
public class ThreadDumpApi {

    private final EndpointInvoker endpointInvoker;
    private final Converter<ThreadDumpFeed, ThreadDumpFeedResponse> converter;

    public ThreadDumpApi(EndpointInvoker endpointInvoker, Converter<ThreadDumpFeed, ThreadDumpFeedResponse> converter) {
        this.endpointInvoker = endpointInvoker;
        this.converter = converter;
    }

    @DefaultApiResponse(summary = "The threaddump endpoint provides a thread dump from the application’s JVM.")
    @ApiResponse(
            description = "OK",
            responseCode = "200",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ThreadDumpFeedResponse.class)))
    @InstanceIdParameter
    @GetMapping(ApiPaths.ThreadDumpApi.INSTANCE_ID)
    public ThreadDumpFeedResponse getThreadDump(@PathVariable("instanceId") String instanceId) {
        ThreadDumpFeed result = endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.GET_THREAD_DUMP, NoHttpPayload.INSTANCE);

        return Objects.requireNonNull(converter.convert(result));
    }

    @DefaultApiResponse(summary = "Endpoint allows enabling thread contention monitoring.")
    @ApiResponse(description = "OK", responseCode = "200")
    @InstanceIdParameter
    @PostMapping(ApiPaths.ThreadDumpApi.ENABLE_CONTENTION_MONITORING)
    public void enableContentionMonitoring(@PathVariable("instanceId") String instanceId) {
        endpointInvoker.invokeNoValue(
                InstanceId.of(instanceId),
                ActuatorEndpoints.THREAD_DUMP_ENABLE_CONTENTION_MONITORING,
                NoHttpPayload.INSTANCE);
    }

    @DefaultApiResponse(summary = "Endpoint allows disabling thread contention monitoring.")
    @ApiResponse(description = "OK", responseCode = "200")
    @InstanceIdParameter
    @PostMapping(ApiPaths.ThreadDumpApi.DISABLE_CONTENTION_MONITORING)
    public void disableContentionMonitoring(@PathVariable("instanceId") String instanceId) {
        endpointInvoker.invokeNoValue(
                InstanceId.of(instanceId),
                ActuatorEndpoints.THREAD_DUMP_DISABLE_CONTENTION_MONITORING,
                NoHttpPayload.INSTANCE);
    }
}
