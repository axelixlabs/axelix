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
package com.axelixlabs.axelix.master.service.convert.response;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.api.ConfigPropsFeed;
import com.axelixlabs.axelix.master.api.response.ConfigPropsFeedResponse;
import com.axelixlabs.axelix.master.api.response.ConfigPropsFeedResponse.ConfigPropsProfile;

/**
 * The {@link Converter} from {@link ConfigPropsFeed} to {@link ConfigPropsFeedResponse}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class ConfigPropsFeedConverter implements Converter<ConfigPropsFeed, ConfigPropsFeedResponse> {

    @Override
    public @NonNull ConfigPropsFeedResponse convertInternal(@NonNull ConfigPropsFeed source) {
        ConfigPropsFeedResponse response = new ConfigPropsFeedResponse();

        source.getContexts().values().forEach(context -> {
            if (context != null && context.getBeans() != null) {
                context.getBeans()
                        .forEach((beanName, bean) -> response.addBean(new ConfigPropsProfile(
                                beanName, bean.getPrefix(), bean.getProperties(), bean.getInputs())));
            }
        });

        return response;
    }
}
