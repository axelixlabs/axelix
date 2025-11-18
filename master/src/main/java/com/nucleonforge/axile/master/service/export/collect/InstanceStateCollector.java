package com.nucleonforge.axile.master.service.export.collect;

import com.nucleonforge.axile.master.exception.StateExportException;

/**
 * Collector for application state data export functionality.
 *
 * @since 27.10.2025
 * @author Nikita Kirillov
 */
public interface InstanceStateCollector {

    /**
     * @return the {@link StateComponent state export component} that this collector is responsible for.
     */
    StateComponent responsibleFor();

    /**
     * Collects data from the specified application instance.
     *
     * @param instanceId the identifier of the application instance to collect data from
     * @return the collected data as the byte array
     */
    byte[] collect(String instanceId) throws StateExportException;
}
