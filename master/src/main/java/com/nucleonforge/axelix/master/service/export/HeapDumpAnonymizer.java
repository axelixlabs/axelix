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
package com.nucleonforge.axelix.master.service.export;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.paypal.heapdumptool.sanitizer.HeapDumpSanitizer;
import com.paypal.heapdumptool.sanitizer.SanitizeCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.nucleonforge.axelix.master.exception.StateExportException;

/**
 * Component responsible for anonymizing heap dump.
 *
 * @author Nikita Kirillov
 * @since 21.11.2025
 */
@Component
public class HeapDumpAnonymizer {

    private static final Logger log = LoggerFactory.getLogger(HeapDumpAnonymizer.class);

    public Resource anonymize(Resource originalHeapDump) throws StateExportException {
        try (InputStream inputStream = originalHeapDump.getInputStream();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            HeapDumpSanitizer sanitizer = new HeapDumpSanitizer();
            sanitizer.setInputStream(inputStream);
            sanitizer.setOutputStream(outputStream);
            sanitizer.setProgressMonitor(processMonitor -> {});
            // TODO: Consider making sanitization options configurable in the future.
            // Currently using default SanitizeCommand, but could be extended
            sanitizer.setSanitizeCommand(new SanitizeCommand());
            sanitizer.sanitize();

            return new ByteArrayResource(outputStream.toByteArray()) {
                @Override
                public String getFilename() {
                    return "heapdump-sanitized.hprof";
                }
            };
        } catch (Exception e) {
            log.warn("Error during heap dump sanitization: {}", e.getMessage());
            throw new StateExportException();
        }
    }
}
