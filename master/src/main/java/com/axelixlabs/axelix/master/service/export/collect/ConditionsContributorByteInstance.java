/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.master.service.export.collect;

import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.master.api.external.endpoint.ConditionsApi;
import com.axelixlabs.axelix.master.service.export.StateComponent;
import com.axelixlabs.axelix.master.service.export.settings.ConditionsStateComponentSettings;

/**
 * Collects Spring Conditions information for application state export.
 *
 * @see ConditionsApi
 * @since 27.10.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@Component
public class ConditionsContributorByteInstance
        extends AbstractByteInstanceStateCollector<ConditionsStateComponentSettings> {

    private final ConditionsApi conditionsApi;

    public ConditionsContributorByteInstance(final ConditionsApi conditionsApi) {
        this.conditionsApi = conditionsApi;
    }

    @Override
    public StateComponent responsibleFor() {
        return StateComponent.CONDITIONS;
    }

    @Override
    protected byte[] collectByte(String instanceId, ConditionsStateComponentSettings settings) {
        return conditionsApi.getConditionsFeed(instanceId);
    }
}
