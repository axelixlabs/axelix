package com.nucleonforge.axile.master.service.state.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.master.exception.StateExportException;

/**
 * Default implementation of {@link ApplicationStateExportService}.
 *
 * @author Nikita Kirillov
 * @since 27.10.2025
 */
@Service
public class DefaultApplicationStateExportService implements ApplicationStateExportService {

    private static final Logger log = LoggerFactory.getLogger(DefaultApplicationStateExportService.class);

    private final List<StateDataCollector> collectors;

    private final ObjectMapper objectMapper;

    public DefaultApplicationStateExportService(List<StateDataCollector> collectors) {
        this.collectors = collectors;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public byte[] exportInstanceState(String instanceId) throws StateExportException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (StateDataCollector collector : collectors) {
                addCollectorDataToZip(zos, instanceId, collector);
            }
        } catch (IOException e) {
            log.error(
                    "Failed to create state export archive for instance: {}. Error: {}", instanceId, e.getMessage(), e);
            throw new StateExportException(instanceId, e);
        }

        return baos.toByteArray();
    }

    private void addCollectorDataToZip(ZipOutputStream zos, String instanceId, StateDataCollector collector)
            throws IOException {
        String filename = collector.getName() + ".json";

        Object collectedData = collector.collectData(instanceId);

        zos.putNextEntry(new ZipEntry(filename));
        zos.write(objectMapper.writeValueAsBytes(collectedData));
        zos.closeEntry();

        log.debug("Successfully collected data from {} for instance: {}", collector.getName(), instanceId);
    }
}
