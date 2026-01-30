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

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import com.axelixlabs.axelix.sbs.spring.context.ContextRestarter;

import static com.axelixlabs.axelix.sbs.spring.properties.AxelixPropertySource.AXELIX_PROPERTY_SOURCE_NAME;

/**
 * {@link PropertyMutator} that reloads the {@link ApplicationContext} after property change so that the change
 * propagates everywhere where required.
 *
 * @since 07.04.2025
 * @author Mikhail Polivakha
 */
public class ContextReloadingPropertyMutator implements PropertyMutator {

    private final ConfigurableEnvironment configurableEnvironment;
    private final ContextRestarter contextRestarter;

    public ContextReloadingPropertyMutator(
            ConfigurableEnvironment configurableEnvironment, ContextRestarter contextRestarter) {
        this.configurableEnvironment = configurableEnvironment;
        this.contextRestarter = contextRestarter;
    }

    @Override
    public void mutate(String propertyName, String newValue) {
        MutablePropertySources propertySources = configurableEnvironment.getPropertySources();

        PropertySource<?> potentiallyAxelixPropertySource = propertySources.get(AXELIX_PROPERTY_SOURCE_NAME);

        if (potentiallyAxelixPropertySource == null) {
            Map<String, Object> source = new HashMap<>();
            source.put(propertyName, newValue);
            propertySources.addFirst(new AxelixPropertySource(source));
        } else {
            var target = (AxelixPropertySource) potentiallyAxelixPropertySource;
            target.addProperty(propertyName, newValue);
        }

        contextRestarter.restartContext();
    }
}
