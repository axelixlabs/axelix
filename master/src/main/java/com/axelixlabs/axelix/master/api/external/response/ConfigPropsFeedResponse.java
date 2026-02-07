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
package com.axelixlabs.axelix.master.api.external.response;

import java.util.ArrayList;
import java.util.List;

import com.axelixlabs.axelix.common.api.KeyValue;

/**
 * The feed of {@code @ConfigurationProperties} beans used in the application.
 *
 * @param beans  The unified list of beans that contains beans from one or more contexts.
 *
 * @author Sergey Cherkasov
 */
public record ConfigPropsFeedResponse(List<ConfigPropsProfile> beans) {

    public ConfigPropsFeedResponse() {
        this(new ArrayList<>());
    }

    public ConfigPropsFeedResponse addBean(ConfigPropsProfile beanProfile) {
        this.beans.add(beanProfile);
        return this;
    }

    /**
     * The profile of a given {@code @ConfigurationProperties} bean.
     *
     * @param beanName     The name of the bean.
     * @param prefix       The prefix applied to the names of the bean properties.
     * @param properties   The properties of the bean as name-value pairs.
     * @param inputs       The origin and value of each configuration parameter
     *                     — which value was applied and from which source
     *                     — to configure a specific property.
     *
     * @author Sergey Cherkasov
     */
    public record ConfigPropsProfile(
            String beanName, String prefix, List<KeyValue> properties, List<KeyValue> inputs) {}
}
