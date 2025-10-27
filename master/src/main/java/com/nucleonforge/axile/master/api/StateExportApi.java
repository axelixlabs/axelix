package com.nucleonforge.axile.master.api;

import java.time.Instant;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.master.service.state.export.DefaultApplicationStateExportService;

/**
 * The API for export state of a given instance.
 *
 * @author Nikita Kirillov
 * @since 27.10.2025
 */
@RestController
@RequestMapping(path = ApiPaths.StateExportApi.MAIN)
public class StateExportApi {

    private final DefaultApplicationStateExportService exportService;

    public StateExportApi(DefaultApplicationStateExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping(path = ApiPaths.StateExportApi.INSTANCE_ID)
    public ResponseEntity<Resource> exportInstanceState(@PathVariable String instanceId) {
        byte[] zipData = exportService.exportInstanceState(instanceId);

        String filename = String.format(
                "instance-state-%s-%s.zip", instanceId, Instant.now().toString().replace(":", "-"));

        ByteArrayResource resource = new ByteArrayResource(zipData);

        return ResponseEntity.ok()
                .header("Content-Type", "application/zip")
                .header("Content-Disposition", "attachment; filename=" + filename)
                .body(resource);
    }
}
