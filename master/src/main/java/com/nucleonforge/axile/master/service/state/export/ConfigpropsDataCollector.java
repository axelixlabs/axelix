package com.nucleonforge.axile.master.service.state.export;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.api.ConfigpropsApi;

/**
 * Collects Spring Configuration Properties information for application state export.
 *
 * @see ConfigpropsApi
 * @since 27.10.2025
 * @author Nikita Kirillov
 */
@Component
public class ConfigpropsDataCollector implements StateDataCollector {

    private final ConfigpropsApi configpropsApi;

    public ConfigpropsDataCollector(ConfigpropsApi configpropsApi) {
        this.configpropsApi = configpropsApi;
    }

    @Override
    public String getName() {
        return "configprops";
    }

    @Override
    public Object collectData(String instanceId) {
        return configpropsApi.getConfigpropsFeed(instanceId);
    }
}
