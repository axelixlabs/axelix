package com.nucleonforge.axile.master.service.export.collect;

import com.nucleonforge.axile.master.exception.StateExportException;

/**
 * Collector for application state data export functionality.
 *
 * @see BeansJsonInstanceStateColletor
 * @since 27.10.2025
 * @author Nikita Kirillov
 */
public interface JsonInstanceStateCollector {

    /**
     * Returns the unique name of this data collector.
     *
     * @return the collector name, should be unique and descriptive (e.g., "beans", "environment")
     */
    String getName();

    /**
     * Collects data from the specified application instance in a form of a JSON {@link String}.
     *
     * @param instanceId the identifier of the application instance to collect data from
     * @return the collected data, will be serialized to JSON
     */
    String collect(String instanceId) throws StateExportException;
}
