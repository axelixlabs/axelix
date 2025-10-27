package com.nucleonforge.axile.master.service.state.export;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.api.EnvironmentApi;

/**
 * Collects Spring Environment information for application state export.
 *
 * @see EnvironmentApi
 * @since 27.10.2025
 * @author Nikita Kirillov
 */
@Component
public class EnvironmentDataCollector implements StateDataCollector {

    private final EnvironmentApi environmentApi;

    public EnvironmentDataCollector(EnvironmentApi environmentApi) {
        this.environmentApi = environmentApi;
    }

    @Override
    public String getName() {
        return "environment";
    }

    @Override
    public Object collectData(String instanceId) {
        return environmentApi.getEnvironment(instanceId);
    }
}
