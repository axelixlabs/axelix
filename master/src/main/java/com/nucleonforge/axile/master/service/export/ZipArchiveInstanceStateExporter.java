package com.nucleonforge.axile.master.service.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.master.exception.StateExportException;
import com.nucleonforge.axile.master.service.export.collect.InstanceStateCollector;
import com.nucleonforge.axile.master.service.export.collect.StateComponent;

/**
 * Default implementation of {@link InstanceStateExporter}.
 *
 * @author Nikita Kirillov
 * @since 27.10.2025
 */
@Service
public class ZipArchiveInstanceStateExporter implements InstanceStateExporter {

    private static final Logger log = LoggerFactory.getLogger(ZipArchiveInstanceStateExporter.class);

    private final List<InstanceStateCollector> collectors;

    public ZipArchiveInstanceStateExporter(List<InstanceStateCollector> collectors) {
        this.collectors = collectors;
    }

    @Override
    public byte[] exportInstanceState(StateExportRequest request) throws StateExportException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (InstanceStateCollector collector : collectors) {
                if (shouldExport(request, collector)) {
                    addCollectorDataToZip(zos, request.instanceId().instanceId(), collector);
                }
            }
        } catch (IOException e) {
            log.error(
                    "Failed to assemble state export archive for instance: {}. Error: {}",
                    request.instanceId().instanceId(),
                    e.getMessage(),
                    e);
            throw new StateExportException(request.instanceId().instanceId(), e);
        }

        return baos.toByteArray();
    }

    private static boolean shouldExport(StateExportRequest request, InstanceStateCollector collector) {
        return request.stateComponents().isEmpty() || request.stateComponents().contains(collector.responsibleFor());
    }

    private void addCollectorDataToZip(ZipOutputStream zos, String instanceId, InstanceStateCollector collector)
            throws IOException {
        StateComponent stateComponent = collector.responsibleFor();
        byte[] state = collector.collect(instanceId);

        zos.putNextEntry(new ZipEntry(stateComponent.getFilename()));
        zos.write(state);
        zos.closeEntry();

        log.debug("Collector {} successfully collected state data for instance: {}", stateComponent, instanceId);
    }
}
