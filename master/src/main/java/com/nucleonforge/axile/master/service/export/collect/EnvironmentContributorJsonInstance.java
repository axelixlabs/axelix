package com.nucleonforge.axile.master.service.export.collect;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.api.EnvironmentApi;
import com.nucleonforge.axile.master.service.export.StateComponent;
import com.nucleonforge.axile.master.service.export.settings.EnvStateComponentSettings;

/**
 * Collects Spring Environment information for application state export.
 *
 * @see EnvironmentApi
 * @since 27.10.2025
 * @author Nikita Kirillov
 */
@Component
public class EnvironmentContributorJsonInstance extends AbstractJsonInstanceStateCollector<EnvStateComponentSettings> {

    private final EnvironmentApi environmentApi;

    public EnvironmentContributorJsonInstance(EnvironmentApi environmentApi) {
        this.environmentApi = environmentApi;
    }

    @Override
    public StateComponent responsibleFor() {
        return StateComponent.ENV;
    }

    @Override
    protected Object collectInternal(String instanceId, EnvStateComponentSettings settings) {
        return environmentApi.getEnvironment(instanceId);
    }
}
