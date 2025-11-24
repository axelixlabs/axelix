package com.nucleonforge.axile.master.service.export.collect;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.api.ConfigPropsApi;
import com.nucleonforge.axile.master.service.export.StateComponent;
import com.nucleonforge.axile.master.service.export.settings.ConfigPropsStateComponentSettings;

/**
 * Collects Spring Configuration Properties information for application state export.
 *
 * @see ConfigPropsApi
 * @since 27.10.2025
 * @author Nikita Kirillov
 */
@Component
public class ConfigpropsContributorJsonInstance
        extends AbstractJsonInstanceStateCollector<ConfigPropsStateComponentSettings> {

    private final ConfigPropsApi configpropsApi;

    public ConfigpropsContributorJsonInstance(ConfigPropsApi configpropsApi) {
        this.configpropsApi = configpropsApi;
    }

    @Override
    public StateComponent responsibleFor() {
        return StateComponent.CONFIG_PROPS;
    }

    @Override
    protected Object collectInternal(String instanceId, ConfigPropsStateComponentSettings settings) {
        return configpropsApi.getConfigpropsFeed(instanceId);
    }
}
