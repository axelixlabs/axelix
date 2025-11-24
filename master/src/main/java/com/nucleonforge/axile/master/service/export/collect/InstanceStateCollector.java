package com.nucleonforge.axile.master.service.export.collect;

import com.nucleonforge.axile.master.exception.StateExportException;
import com.nucleonforge.axile.master.service.export.StateComponent;
import com.nucleonforge.axile.master.service.export.StateComponentSettings;

/**
 * Collector for application state data export functionality.
 *
 * @since 27.10.2025
 * @author Nikita Kirillov
 */
public interface InstanceStateCollector<T extends StateComponentSettings> {

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
    byte[] collect(String instanceId, T settings) throws StateExportException;
}
