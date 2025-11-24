package com.nucleonforge.axile.master.api;

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
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.export.HeapDumpAnonymizer;
import com.nucleonforge.axile.master.service.transport.HeapDumpEndpointProber;

/**
 * The API for Heap Dump.
 *
 * @since 12.11.2025
 * @author Nikita Kirillov
 */
@RestController
@RequestMapping(path = ApiPaths.HeapDumpApi.MAIN)
public class HeapDumpApi {

    private final HeapDumpEndpointProber heapDumpEndpointProber;

    private final HeapDumpAnonymizer heapDumpAnonymizer;

    public HeapDumpApi(HeapDumpEndpointProber heapDumpEndpointProber, HeapDumpAnonymizer heapDumpAnonymizer) {
        this.heapDumpEndpointProber = heapDumpEndpointProber;
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

        Resource resource = heapDumpEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);

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
