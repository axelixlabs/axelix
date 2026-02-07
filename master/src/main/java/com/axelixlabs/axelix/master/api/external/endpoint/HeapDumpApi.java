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

import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.api.error.SimpleApiError;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.export.HeapDumpAnonymizer;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * The API for Heap Dump.
 *
 * @since 12.11.2025
 * @author Nikita Kirillov
 */
@ExternalApiRestController
@RequestMapping(path = ApiPaths.HeapDumpApi.MAIN)
public class HeapDumpApi {

    private final EndpointInvoker endpointInvoker;
    private final HeapDumpAnonymizer heapDumpAnonymizer;

    public HeapDumpApi(EndpointInvoker endpointInvoker, HeapDumpAnonymizer heapDumpAnonymizer) {
        this.endpointInvoker = endpointInvoker;
        this.heapDumpAnonymizer = heapDumpAnonymizer;
    }

    @Operation(
            summary = "Download heap dump for the given instance",
            description = "Returns binary heap dump file",
            responses = {
                @ApiResponse(
                        description = "Heap dump file",
                        responseCode = "200",
                        content =
                                @Content(
                                        mediaType = "application/octet-stream",
                                        schema = @Schema(type = "string", format = "binary")),
                        headers = {
                            @Header(
                                    name = "Content-Disposition",
                                    description = "Attachment with filename",
                                    schema = @Schema(type = "string"))
                        }),
                @ApiResponse(
                        description = "Bad Request - instance not found",
                        responseCode = "400",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class))),
                @ApiResponse(
                        description = "Internal Server Error",
                        responseCode = "500",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class)))
            })
    @Parameter(name = "instanceId", description = "Application Instance ID", required = true)
    @GetMapping(path = ApiPaths.HeapDumpApi.INSTANCE_ID)
    public ResponseEntity<Resource> getHeapDump(
            @PathVariable("instanceId") String instanceId,
            @RequestParam(defaultValue = "true", required = false) boolean sanitizeHeapDump) {

        Resource resource = endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.GET_HEAP_DUMP, NoHttpPayload.INSTANCE);

        if (sanitizeHeapDump) {
            resource = heapDumpAnonymizer.anonymize(resource);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(resource.getFilename())
                                .build()
                                .toString())
                .body(resource);
    }
}
