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

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.swagger.DefaultApiResponse;
import com.axelixlabs.axelix.master.api.external.swagger.InstanceIdParameter;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * The API for Transaction Monitoring.
 *
 * @since 20.01.2026
 * @author Nikita Kirillov
 */
@ExternalApiRestController
@RequestMapping(path = ApiPaths.TransactionMonitoringApi.MAIN)
public class TransactionMonitoringApi {

    private final EndpointInvoker endpointInvoker;

    public TransactionMonitoringApi(EndpointInvoker endpointInvoker) {
        this.endpointInvoker = endpointInvoker;
    }

    @DefaultApiResponse(summary = "Returns transaction statistics for the given instance.")
    @ApiResponse(
            description = "OK",
            responseCode = "200",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "string")))
    @InstanceIdParameter
    @GetMapping(path = ApiPaths.TransactionMonitoringApi.INSTANCE_ID, produces = MediaType.APPLICATION_JSON_VALUE)
    public byte[] getTransactionFeed(@PathVariable("instanceId") String instanceId) {
        return endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.TRANSACTION_STATS_GET, NoHttpPayload.INSTANCE);
    }

    @DefaultApiResponse(summary = "Clears transaction statistics for the given instance.")
    @ApiResponse(description = "OK", responseCode = "200")
    @InstanceIdParameter
    @DeleteMapping(path = ApiPaths.TransactionMonitoringApi.INSTANCE_ID)
    public void clearTransactionStats(@PathVariable("instanceId") String instanceId) {
        endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.TRANSACTION_STATS_CLEAR, NoHttpPayload.INSTANCE);
    }
}
