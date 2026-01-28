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
package com.axelixlabs.axelix.sbs.spring.properties;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import com.axelixlabs.axelix.sbs.spring.env.PropertyNameNormalizer;

/**
 * Default {@link PropertyNameDiscoverer}.
 *
 * @author Sergey Cherkasov
 */
public class DefaultPropertyNameDiscoverer implements PropertyNameDiscoverer {

    private final ConfigurableEnvironment configurableEnvironment;
    private final PropertyNameNormalizer propertyNameNormalizer;

    public DefaultPropertyNameDiscoverer(
            ConfigurableEnvironment configurableEnvironment, PropertyNameNormalizer propertyNameNormalizer) {
        this.configurableEnvironment = configurableEnvironment;
        this.propertyNameNormalizer = propertyNameNormalizer;
    }

    @Override
    @Nullable
    public String discover(String propertyName) {

        for (PropertySource<?> source : configurableEnvironment.getPropertySources()) {
            String foundPropertyName = extractPropertyNames(source, propertyNameNormalizer.normalize(propertyName));
            if (foundPropertyName != null) {
                return foundPropertyName;
            }
        }

        return null;
    }

    // We rely on the priority order of property sources in PropertySource,
    // from highest to lowest priority.
    private @Nullable String extractPropertyNames(PropertySource<?> source, String normalizedPropertyName) {

        if (source instanceof CompositePropertySource composite) {
            for (PropertySource<?> nest : composite.getPropertySources()) {
                String foundPropertyName = extractPropertyNames(nest, normalizedPropertyName);
                if (foundPropertyName != null) {
                    return foundPropertyName;
                }
            }
        } else if (source instanceof EnumerablePropertySource<?> enumerable) {
            for (String name : enumerable.getPropertyNames()) {
                if (normalizedPropertyName.equals(propertyNameNormalizer.normalize(name))) {
                    return name;
                }
            }
        }

        return null;
    }
}
