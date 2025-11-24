package com.nucleonforge.axile.master.service.export.collect;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.api.LogFileApi;
import com.nucleonforge.axile.master.exception.StateExportException;
import com.nucleonforge.axile.master.service.export.StateComponent;
import com.nucleonforge.axile.master.service.export.settings.LogFileStateComponentSettings;

/**
 * Collect log-file for application state export.
 *
 * @see LogFileApi
 * @since 20.11.2025
 * @author Nikita Kirillov
 */
@Component
public class LogFileContributorBinaryInstance
        extends AbstractBinaryInstanceStateCollector<LogFileStateComponentSettings> {

    private final LogFileApi logFileApi;

    public LogFileContributorBinaryInstance(LogFileApi logFileApi) {
        this.logFileApi = logFileApi;
    }

    @Override
    public StateComponent responsibleFor() {
        return StateComponent.LOG_FILE;
    }

    @Override
    protected Resource collectBinaryResource(String instanceId, LogFileStateComponentSettings settings)
            throws StateExportException {
        return logFileApi.getLogFile(instanceId, null);
    }
}
