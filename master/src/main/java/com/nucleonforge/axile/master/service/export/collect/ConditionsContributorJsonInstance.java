package com.nucleonforge.axile.master.service.export.collect;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.api.ConditionsApi;

/**
 * Collects Spring Conditions information for application state export.
 *
 * @see ConditionsApi
 * @since 27.10.2025
 * @author Nikita Kirillov
 */
@Component
public class ConditionsContributorJsonInstance extends AbstractJsonInstanceStateCollector {

    private final ConditionsApi conditionsApi;

    public ConditionsContributorJsonInstance(final ConditionsApi conditionsApi) {
        this.conditionsApi = conditionsApi;
    }

    @Override
    public String getName() {
        return "conditions";
    }

    @Override
    protected Object collectInternal(String instanceId) {
        return conditionsApi.getConditionsFeed(instanceId);
    }
}
