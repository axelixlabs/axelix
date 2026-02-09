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
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.request.state.StateExportRequest;
import com.axelixlabs.axelix.master.api.external.swagger.DefaultApiResponse;
import com.axelixlabs.axelix.master.api.external.swagger.InstanceIdParameter;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.convert.request.StateExportRequestConverter;
import com.axelixlabs.axelix.master.service.export.StateArchiveFileNameGenerator;
import com.axelixlabs.axelix.master.service.export.StateExport;
import com.axelixlabs.axelix.master.service.export.ZipArchiveInstanceStateExporter;

/**
 * The API for exporting the state of a given instance.
 *
 * @author Nikita Kirillov
 * @since 27.10.2025
 */
@ExternalApiRestController
@RequestMapping(path = ApiPaths.StateExportApi.MAIN)
public class StateExportApi {

    private final ZipArchiveInstanceStateExporter exportService;
    private final StateArchiveFileNameGenerator stateArchiveFileNameGenerator;
    private final StateExportRequestConverter stateExportRequestConverter;

    public StateExportApi(
            ZipArchiveInstanceStateExporter exportService,
            StateArchiveFileNameGenerator stateArchiveFileNameGenerator,
            StateExportRequestConverter stateExportRequestConverter) {
        this.exportService = exportService;
        this.stateArchiveFileNameGenerator = stateArchiveFileNameGenerator;
        this.stateExportRequestConverter = stateExportRequestConverter;
    }

    @DefaultApiResponse(summary = "Exports the application's state")
    @ApiResponse(description = "OK", responseCode = "200", content = @Content(mediaType = "application/zip"))
    @InstanceIdParameter
    @PostMapping(path = ApiPaths.StateExportApi.INSTANCE_ID)
    public ResponseEntity<Resource> exportInstanceState(
            @io.swagger.v3.oas.annotations.parameters.RequestBody @RequestBody StateExportRequest request,
            @PathVariable String instanceId) {

        InstanceId id = InstanceId.of(instanceId);

        StateExport convertedRequest = stateExportRequestConverter.convert(request);

        byte[] binaryData = exportService.exportInstanceState(Objects.requireNonNull(convertedRequest), id);

        String filename = stateArchiveFileNameGenerator.generate(id);

        ByteArrayResource resource = new ByteArrayResource(binaryData);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/zip")
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(filename)
                                .build()
                                .toString())
                .body(resource);
    }
}
