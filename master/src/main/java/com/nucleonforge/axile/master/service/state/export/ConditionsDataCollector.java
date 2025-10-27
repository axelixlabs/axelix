package com.nucleonforge.axile.master.service.state.export;

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
public class ConditionsDataCollector implements StateDataCollector {

    private final ConditionsApi conditionsApi;

    public ConditionsDataCollector(final ConditionsApi conditionsApi) {
        this.conditionsApi = conditionsApi;
    }

    @Override
    public String getName() {
        return "conditions";
    }

    @Override
    public Object collectData(String instanceId) {
        return conditionsApi.getConditionsFeed(instanceId);
    }
}
