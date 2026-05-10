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
package com.axelixlabs.axelix.master.service.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.exception.StateExportException;
import com.axelixlabs.axelix.master.service.export.collect.InstanceStateCollector;

/**
 * Default implementation of {@link InstanceStateExporter}.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @since 27.10.2025
 */
@Service
public class ZipArchiveInstanceStateExporter implements InstanceStateExporter {

    private static final Logger log = LoggerFactory.getLogger(ZipArchiveInstanceStateExporter.class);

    private final Map<StateComponent, InstanceStateCollector> collectors;

    public ZipArchiveInstanceStateExporter(List<InstanceStateCollector> collectors) {
        this.collectors = collectors.stream()
                .collect(Collectors.toMap(InstanceStateCollector::responsibleFor, Function.identity()));
    }

    @Override
    public byte[] exportInstanceState(StateExport stateExportRequest, InstanceId instanceId)
            throws StateExportException {

        var source = new ByteArrayOutputStream();

        try (var zos = new ZipOutputStream(source)) {
            exportStateIntoZipOutputStream(stateExportRequest, instanceId, zos);
        } catch (IOException e) {
            log.error(
                    "Failed to assemble state export archive for instance: {}. Error: {}",
                    instanceId.instanceId(),
                    e.getMessage(),
                    e);
            throw new StateExportException(instanceId.instanceId(), e);
        }

        return source.toByteArray();
    }

    private void exportStateIntoZipOutputStream(
            StateExport stateExportRequest, InstanceId instanceId, ZipOutputStream zos) {

        for (StateComponentSettings component : stateExportRequest.components()) {

            InstanceStateCollector collector = collectors.get(component.component());

            if (collector == null) {
                log.warn(
                        "Unable to find InstanceStateCollector for the requested StateComponent: {}. "
                                + "That is not okay, please, report this to maintainers",
                        component.component());
                continue;
            }

            try {
                addCollectorDataToZip(zos, instanceId.instanceId(), collector);
            } catch (IOException e) {
                log.error(
                        "Exception in state collection for instance : {}. State collector responsible for {} thrown an error. Skipping this collector",
                        instanceId.instanceId(),
                        collector.responsibleFor(),
                        e);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
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
