package com.nucleonforge.axile.master.service.export.collect;

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
public class ConfigpropsContributorJsonInstance extends AbstractJsonInstanceStateCollector {

    private final ConfigpropsApi configpropsApi;

    public ConfigpropsContributorJsonInstance(ConfigpropsApi configpropsApi) {
        this.configpropsApi = configpropsApi;
    }

    @Override
    public String getName() {
        return "configprops";
    }

    @Override
    protected Object collectInternal(String instanceId) {
        return configpropsApi.getConfigpropsFeed(instanceId);
    }
}
