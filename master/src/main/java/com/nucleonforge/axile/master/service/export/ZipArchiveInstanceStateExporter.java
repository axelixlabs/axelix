package com.nucleonforge.axile.master.service.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.master.exception.StateExportException;
import com.nucleonforge.axile.master.service.export.collect.JsonInstanceStateCollector;

/**
 * Default implementation of {@link InstanceStateExporter}.
 *
 * @author Nikita Kirillov
 * @since 27.10.2025
 */
@Service
public class ZipArchiveInstanceStateExporter implements InstanceStateExporter {

    private static final Logger log = LoggerFactory.getLogger(ZipArchiveInstanceStateExporter.class);

    private final List<JsonInstanceStateCollector> collectors;

    public ZipArchiveInstanceStateExporter(List<JsonInstanceStateCollector> collectors) {
        this.collectors = collectors;
    }

    @Override
    public byte[] exportInstanceState(String instanceId) throws StateExportException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (JsonInstanceStateCollector collector : collectors) {
                addCollectorDataToZip(zos, instanceId, collector);
            }
        } catch (IOException e) {
            log.error(
                    "Failed to assemble state export archive for instance: {}. Error: {}",
                    instanceId,
                    e.getMessage(),
                    e);
            throw new StateExportException(instanceId, e);
        }

        return baos.toByteArray();
    }

    private void addCollectorDataToZip(ZipOutputStream zos, String instanceId, JsonInstanceStateCollector collector)
            throws IOException {
        String collectorName = collector.getName();
        String stateEntry = collector.collect(instanceId);

        zos.putNextEntry(new ZipEntry(collectorName + ".json"));
        zos.write(stateEntry.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();

        log.debug("Collector {} successfully collected state data for instance: {}", collectorName, instanceId);
    }
}
