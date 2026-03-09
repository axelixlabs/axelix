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
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.axelixlabs.axelix.common.api.integration.FeignIntegration;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.swagger.DefaultApiResponse;
import com.axelixlabs.axelix.master.api.external.swagger.InstanceIdParameter;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * The API for managing feign client.
 *
 * @author Sergey Cherkasov
 */
@Tag(
        name = "Feign Client API",
        description =
                "The Feign endpoint provides information about the Spring Cloud OpenFeign components of the application.")
@ExternalApiRestController
@RequestMapping(path = ApiPaths.FeignClientApi.MAIN)
public class FeignClientApi {

    private final EndpointInvoker endpointInvoker;

    public FeignClientApi(EndpointInvoker endpointInvoker) {
        this.endpointInvoker = endpointInvoker;
    }

    @DefaultApiResponse(summary = "Returns feign client feed for the given instance.")
    @ApiResponse(
            description = "OK",
            responseCode = "200",
            content =
                    @Content(mediaType = "application/json", schema = @Schema(implementation = FeignIntegration.class)))
    @InstanceIdParameter
    @GetMapping(path = ApiPaths.DetailsApi.INSTANCE_ID)
    public ResponseEntity<byte[]> getFeignClientFeed(@PathVariable("instanceId") String instanceId) {
        byte[] body = endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.GET_FEIGN_CLIENT, NoHttpPayload.INSTANCE);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }
}
