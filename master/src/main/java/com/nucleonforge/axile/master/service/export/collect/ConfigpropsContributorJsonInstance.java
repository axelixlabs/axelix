package com.nucleonforge.axile.master.service.export.collect;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.api.ConfigPropsApi;

/**
 * Collects Spring Configuration Properties information for application state export.
 *
 * @see ConfigPropsApi
 * @since 27.10.2025
 * @author Nikita Kirillov
 */
@Component
public class ConfigpropsContributorJsonInstance extends AbstractJsonInstanceStateCollector {

    private final ConfigPropsApi configpropsApi;

    public ConfigpropsContributorJsonInstance(ConfigPropsApi configpropsApi) {
        this.configpropsApi = configpropsApi;
    }

    @Override
    protected Object collectInternal(String instanceId) {
        return configpropsApi.getConfigpropsFeed(instanceId);
    }

    @Override
    public StateComponent responsibleFor() {
        return StateComponent.CONFIG_PROPS;
    }
}
