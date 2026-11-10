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
package com.axelixlabs.axelix.sbs.spring.core.configprops;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesFeed;
import com.axelixlabs.axelix.common.contract.env.EnvironmentFeed;
import com.axelixlabs.axelix.common.utils.PropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.core.config.EndpointsConfigurationProperties;

/**
 * Identifies and marks sensitive properties.
 *
 * @author Nikita Kirillov
 */
public class PropertySensitivityMarker {
    private final PropertyNameNormalizer normalizer;
    private final Set<String> names;
    private final boolean all;

    public PropertySensitivityMarker(List<String> names, PropertyNameNormalizer normalizer) {
        this.normalizer = normalizer;
        this.all = names.containsAll(EndpointsConfigurationProperties.SANITIZE_ALL);
        this.names = names.stream().map(normalizer::normalize).collect(Collectors.toSet());
    }

    /**
     * Marks sensitive environment properties.
     *
     * @param feed the environment feed
     * @return the feed with sensitivity flags set
     */
    public EnvironmentFeed mark(EnvironmentFeed feed) {
        feed.getPropertySources()
                .forEach(source -> source.getProperties()
                        .forEach(property -> property.setSensitive(isSensitive(property.getName()))));
        return feed;
    }

    /**
     * Marks sensitive configuration properties and binding inputs.
     *
     * @param feed the configuration properties feed
     * @return the feed with sensitivity flags set
     */
    public ConfigurationPropertiesFeed mark(ConfigurationPropertiesFeed feed) {
        feed.getBeans().forEach(bean -> {
            bean.getProperties()
                    .forEach(
                            property -> property.setSensitive(isSensitive(bean.getPrefix() + "." + property.getKey())));
            bean.getInputs().forEach(property -> {
                String key = property.getKey();
                property.setSensitive(key.endsWith(".value")
                        && isSensitive(bean.getPrefix() + "." + key.substring(0, key.length() - ".value".length())));
            });
        });
        return feed;
    }

    private boolean isSensitive(String name) {
        return all || names.contains(normalizer.normalize(name));
    }
}
