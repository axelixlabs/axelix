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
package com.axelixlabs.axelix.sbs.spring.core.env;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.api.KeyValue;
import com.axelixlabs.axelix.common.api.env.EnvironmentFeed.Deprecation;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesCache;

/**
 * Default implementation {@link PropertyMappingBuilder}.
 *
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
public class DefaultPropertyMappingBuilder implements PropertyMappingBuilder {

    @Nullable
    private final ConfigurationPropertiesCache configurationPropertiesCache;

    private final PropertyNameNormalizer propertyNameNormalizer;

    public DefaultPropertyMappingBuilder(
            PropertyNameNormalizer propertyNameNormalizer,
            @Nullable ConfigurationPropertiesCache configurationPropertiesCache) {
        this.propertyNameNormalizer = propertyNameNormalizer;
        this.configurationPropertiesCache = configurationPropertiesCache;
    }

    @Nullable
    public Deprecation buildFromMetadata(@Nullable PropertyMetadata propertyMetadata) {
        if (propertyMetadata == null || propertyMetadata.getDeprecation() == null) {
            return null;
        }

        return new Deprecation(propertyMetadata.getDeprecation().getMessage());
    }

    public Map<String, String> buildConfigPropsMappingMap() {
        if (configurationPropertiesCache == null) {
            return Map.of();
        }

        Map<String, String> configPropsMapping = new HashMap<>();

        configurationPropertiesCache
                .getConfigurationPropertiesFeed()
                .getBeans()
                .forEach((bean) -> applyPrefixAndProperty(
                        bean.getPrefix(), bean.getProperties(), configPropsMapping, bean.getBeanName()));

        return configPropsMapping;
    }

    private void applyPrefixAndProperty(
            String prefix, List<KeyValue> properties, Map<String, String> configPropsMapping, String beanName) {
        for (var property : properties) {
            String fullProperty = propertyNameNormalizer.normalize(prefix + property.getKey());
            configPropsMapping.put(fullProperty, beanName);
        }
    }
}
