package com.nucleonforge.axile.master.service.state.export;

import com.nucleonforge.axile.master.exception.StateExportException;

/**
 * Service for exporting application instance state data.
 * Provides capability to export various application metrics and configuration.
 *
 * @author Nikita Kirillov
 * @since 27.10.2025
 */
public interface ApplicationStateExportService {

    /**
     * Exports state of the specified application instance.
     *
     * @param instanceId the unique identifier of the application instance
     * @return byte array containing the exported state data
     * @throws StateExportException if export process fails
     */
    byte[] exportInstanceState(String instanceId) throws StateExportException;
}
