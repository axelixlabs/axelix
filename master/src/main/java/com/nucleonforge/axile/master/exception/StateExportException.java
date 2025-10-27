package com.nucleonforge.axile.master.exception;

import com.nucleonforge.axile.master.service.state.export.DefaultApplicationStateExportService;

/**
 * Typically thrown by the {@link DefaultApplicationStateExportService} when state export
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
