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

import com.axelixlabs.axelix.master.api.external.endpoint.ConfigPropsApi;
import com.axelixlabs.axelix.master.service.export.StateComponent;
import com.axelixlabs.axelix.master.service.export.settings.ConfigPropsStateComponentSettings;

/**
 * Collects Spring Configuration Properties information for application state export.
 *
 * @see ConfigPropsApi
 * @since 27.10.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@Component
public class ConfigpropsContributorByteInstance
        extends AbstractByteInstanceStateCollector<ConfigPropsStateComponentSettings> {

    private final ConfigPropsApi configpropsApi;

    public ConfigpropsContributorByteInstance(ConfigPropsApi configpropsApi) {
        this.configpropsApi = configpropsApi;
    }

    @Override
    public StateComponent responsibleFor() {
        return StateComponent.CONFIG_PROPS;
    }

    @Override
    protected byte[] collectByte(String instanceId, ConfigPropsStateComponentSettings settings) {
        return configpropsApi.getConfigpropsFeed(instanceId);
    }
}
