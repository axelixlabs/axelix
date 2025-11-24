package com.nucleonforge.axile.master.service.export.collect;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.api.ConditionsApi;
import com.nucleonforge.axile.master.service.export.StateComponent;
import com.nucleonforge.axile.master.service.export.settings.ConditionsStateComponentSettings;

/**
 * Collects Spring Conditions information for application state export.
 *
 * @see ConditionsApi
 * @since 27.10.2025
 * @author Nikita Kirillov
 */
@Component
public class ConditionsContributorJsonInstance
        extends AbstractJsonInstanceStateCollector<ConditionsStateComponentSettings> {

    private final ConditionsApi conditionsApi;

    public ConditionsContributorJsonInstance(final ConditionsApi conditionsApi) {
        this.conditionsApi = conditionsApi;
    }

    @Override
    public StateComponent responsibleFor() {
        return StateComponent.CONDITIONS;
    }

    @Override
    protected Object collectInternal(String instanceId, ConditionsStateComponentSettings settings) {
        return conditionsApi.getConditionsFeed(instanceId);
    }
}
