package com.nucleonforge.axile.master.service.export.collect;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.api.BeansApi;
import com.nucleonforge.axile.master.service.export.StateComponent;
import com.nucleonforge.axile.master.service.export.settings.BeansStateComponentSettings;

/**
 * Collects Spring Beans information for application state export.
 *
 * @see BeansApi
 * @since 27.10.2025
 * @author Nikita Kirillov
 */
@Component
public class BeansContributorJsonInstance extends AbstractJsonInstanceStateCollector<BeansStateComponentSettings> {

    private final BeansApi beansApi;

    public BeansContributorJsonInstance(BeansApi beansApi) {
        this.beansApi = beansApi;
    }

    @Override
    public StateComponent responsibleFor() {
        return StateComponent.BEANS;
    }

    @Override
    protected Object collectInternal(String instanceId, BeansStateComponentSettings settings) {
        return beansApi.getBeansProfile(instanceId);
    }
}
