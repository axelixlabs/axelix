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
package com.axelixlabs.axelix.sbs.spring.core.properties;

import org.jspecify.annotations.Nullable;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import com.axelixlabs.axelix.sbs.spring.core.env.PropertyNameNormalizer;

/**
 * Default {@link PropertyNameDiscoverer}.
 *
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 */
public class DefaultPropertyNameDiscoverer implements PropertyNameDiscoverer {

    private final PropertyNameNormalizer propertyNameNormalizer;

    private final ConfigurableApplicationContext applicationContext;

    public DefaultPropertyNameDiscoverer(
            ConfigurableApplicationContext applicationContext, PropertyNameNormalizer propertyNameNormalizer) {
        this.applicationContext = applicationContext;
        this.propertyNameNormalizer = propertyNameNormalizer;
    }

    @Override
    @Nullable
    public String discover(String propertyName) {
        String normalizedPropertyName = propertyNameNormalizer.normalize(propertyName);

        ConfigurableApplicationContext ctx = applicationContext;
        while (ctx != null) {
            String foundPropertyName = discoverInEnvironment(ctx.getEnvironment(), normalizedPropertyName);
            if (foundPropertyName != null) {
                return foundPropertyName;
            }

            ctx = (ConfigurableApplicationContext) ctx.getParent();
        }

        return null;
    }

    @Nullable
    private String discoverInEnvironment(ConfigurableEnvironment environment, String normalizedPropertyName) {
        for (PropertySource<?> source : environment.getPropertySources()) {
            String foundPropertyName = extractPropertyName(source, normalizedPropertyName);
            if (foundPropertyName != null) {
                return foundPropertyName;
            }
        }
        return null;
    }

    // We rely on the priority order of property sources in PropertySource,
    // from highest to lowest priority.
    private @Nullable String extractPropertyName(PropertySource<?> source, String normalizedPropertyName) {

        if (source instanceof CompositePropertySource composite) {
            for (PropertySource<?> nest : composite.getPropertySources()) {
                String foundPropertyName = extractPropertyName(nest, normalizedPropertyName);
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
