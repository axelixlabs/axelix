package com.nucleonforge.axile.master.service.export;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.paypal.heapdumptool.sanitizer.HeapDumpSanitizer;
import com.paypal.heapdumptool.sanitizer.SanitizeCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.exception.StateExportException;

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
