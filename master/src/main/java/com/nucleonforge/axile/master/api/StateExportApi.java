package com.nucleonforge.axile.master.api;

import java.util.Objects;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.request.state.StateExportRequest;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.convert.request.StateExportRequestConverter;
import com.nucleonforge.axile.master.service.export.StateArchiveFileNameGenerator;
import com.nucleonforge.axile.master.service.export.StateExport;
import com.nucleonforge.axile.master.service.export.ZipArchiveInstanceStateExporter;

/**
 * The API for exporting the state of a given instance.
 *
 * @author Nikita Kirillov
 * @since 27.10.2025
 */
@RestController
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

    @Operation(
            summary = "Exports the application's state",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        content = @Content(mediaType = "application/zip")),
                @ApiResponse(
                        description = "Bad Request",
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
