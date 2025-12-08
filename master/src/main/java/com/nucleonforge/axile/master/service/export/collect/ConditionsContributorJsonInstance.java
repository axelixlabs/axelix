/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
