package com.nucleonforge.axile.master.exception;

import com.nucleonforge.axile.master.service.export.ZipArchiveInstanceStateExporter;

/**
 * Typically thrown by the {@link ZipArchiveInstanceStateExporter} when state export
 * operations fail due to IO errors, data collection issues, or archive creation problems.
 *
 * @since 27.10.2025
 * @author Nikita Kirillov
 */
public class StateExportException extends RuntimeException {

    public StateExportException() {}

    public StateExportException(String instanceId, Throwable cause) {
        super("State export failed for instance: " + instanceId, cause);
    }
}
