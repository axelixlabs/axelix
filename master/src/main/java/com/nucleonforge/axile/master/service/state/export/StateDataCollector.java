package com.nucleonforge.axile.master.service.state.export;

/**
 * Collector for application state data export functionality.
 * Implement this interface to add new data sources to the state export.
 *
 * @see BeansDataCollector
 * @since 27.10.2025
 * @author Nikita Kirillov
 */
public interface StateDataCollector {

    /**
     * Returns the unique name of this data collector.
     * This name will be used as the filename in the export archive.
     *
     * @return the collector name, should be unique and descriptive (e.g., "beans", "environment")
     */
    String getName();

    /**
     * Collects data from the specified application instance.
     *
     * @param instanceId the identifier of the application instance to collect data from
     * @return the collected data, will be serialized to JSON
     */
    Object collectData(String instanceId);
}
