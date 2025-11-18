package com.nucleonforge.axile.master.api;

import java.util.List;
import java.util.Optional;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.request.StateExportComponent;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.export.StateArchiveFileNameGenerator;
import com.nucleonforge.axile.master.service.export.StateExportRequest;
import com.nucleonforge.axile.master.service.export.ZipArchiveInstanceStateExporter;
import com.nucleonforge.axile.master.service.export.collect.StateComponent;

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

    public StateExportApi(
            ZipArchiveInstanceStateExporter exportService,
            StateArchiveFileNameGenerator stateArchiveFileNameGenerator) {
        this.exportService = exportService;
        this.stateArchiveFileNameGenerator = stateArchiveFileNameGenerator;
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
    @Parameter(
            name = "components",
            description = "List of state export components",
            schema = @Schema(type = "array", implementation = StateExportComponent.class))
    @GetMapping(path = ApiPaths.StateExportApi.INSTANCE_ID)
    public ResponseEntity<Resource> exportInstanceState(
            @RequestParam(required = false) List<StateExportComponent> components, @PathVariable String instanceId) {

        byte[] binaryData = exportService.exportInstanceState(buildRequest(components, instanceId));

        String filename = stateArchiveFileNameGenerator.generate(InstanceId.of(instanceId));

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

    // TODO:
    //  For now, building request here if fine. Later, if the logic would become more complicated, it would
    //  make sense to introduce dedicated mapper for it
    private static StateExportRequest buildRequest(List<StateExportComponent> components, String instanceId) {
        // spotless:off
        return new StateExportRequest(
                InstanceId.of(instanceId),
                Optional
                    .ofNullable(components)
                    .orElse(List.of())
                    .stream()
                    .map(it -> StateComponent.valueOf(it.name()))
                    .toList()
        );
        // spotless:on
    }
}
