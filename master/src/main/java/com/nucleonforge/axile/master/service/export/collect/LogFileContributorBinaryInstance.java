package com.nucleonforge.axile.master.service.export.collect;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.api.LogFileApi;
import com.nucleonforge.axile.master.exception.StateExportException;

/**
 * Collect log-file for application state export.
 *
 * @see LogFileApi
 * @since 20.11.2025
 * @author Nikita Kirillov
 */
@Component
public class LogFileContributorBinaryInstance extends AbstractBinaryInstanceStateCollector {

    private final LogFileApi logFileApi;

    public LogFileContributorBinaryInstance(LogFileApi logFileApi) {
        this.logFileApi = logFileApi;
    }

    @Override
    protected Resource collectBinaryResource(String instanceId) throws StateExportException {
        return logFileApi.getLogFile(instanceId, null);
    }

    @Override
    public StateComponent responsibleFor() {
        return StateComponent.LOG_FILE;
    }
}
