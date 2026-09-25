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
package com.axelixlabs.axelix.master.service.env;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.domain.Sanitization;
import com.axelixlabs.axelix.common.utils.PropertyNameNormalizer;
import com.axelixlabs.axelix.master.api.external.response.env.DangerousProperty;
import com.axelixlabs.axelix.master.contract.env.EnvironmentFeed;
import com.axelixlabs.axelix.master.contract.env.Property;
import com.axelixlabs.axelix.master.contract.env.PropertySource;

/**
 * Default {@link DangerousPropertyDetector}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class DefaultDangerousPropertyDetector implements DangerousPropertyDetector {

    private final PropertyNameNormalizer propertyNameNormalizer;

    public DefaultDangerousPropertyDetector(PropertyNameNormalizer propertyNameNormalizer) {
        this.propertyNameNormalizer = propertyNameNormalizer;
    }

    @Override
    public Map<Property, DangerousProperty> detect(EnvironmentFeed feed) {
        Map<String, Property> effectiveProperties = new HashMap<>();

        for (PropertySource propertySource : feed.getPropertySources()) {
            for (Property property : propertySource.getProperties()) {
                effectiveProperties.putIfAbsent(propertyNameNormalizer.normalize(property.getName()), property);
            }
        }

        Map<Property, DangerousProperty> dangerousProperties = new IdentityHashMap<>();

        effectiveProperties.forEach((normalizedName, property) -> {
            if (Sanitization.SANITIZED_VALUE.equals(property.getValue())) {
                return;
            }

            DangerousProperty dangerousProperty = DangerousProperty.resolve(normalizedName, property.getValue());

            if (dangerousProperty != null) {
                dangerousProperties.put(property, dangerousProperty);
            }
        });

        return dangerousProperties;
    }
}
