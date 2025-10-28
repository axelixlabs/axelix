package com.nucleonforge.axile.master.api;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.export.StateArchiveFileNameGenerator;
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

    public StateExportApi(
            ZipArchiveInstanceStateExporter exportService,
            StateArchiveFileNameGenerator stateArchiveFileNameGenerator) {
        this.exportService = exportService;
        this.stateArchiveFileNameGenerator = stateArchiveFileNameGenerator;
    }

    @GetMapping(path = ApiPaths.StateExportApi.INSTANCE_ID)
    public ResponseEntity<Resource> exportInstanceState(@PathVariable String instanceId) {
        byte[] binaryData = exportService.exportInstanceState(instanceId);
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
}
