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

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationPropertiesBean;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import com.axelixlabs.axelix.sbs.spring.core.properties.AxelixPropertySource;
import com.axelixlabs.axelix.sbs.spring.core.properties.PropertyNameDiscoverer;

import static com.axelixlabs.axelix.sbs.spring.core.properties.AxelixPropertySource.AXELIX_PROPERTY_SOURCE_NAME;

/**
 * Implementation of {@link ConfigurationPropertiesMutator} that dynamically rebinds
 * configuration properties at runtime.
 *
 * @since 31.03.2026
 * @author Nikita Kirillov
 */
public class RebindingConfigurationPropertiesMutator implements ConfigurationPropertiesMutator {

    private final ConfigurableEnvironment configurableEnvironment;

    private final PropertyNameDiscoverer propertyNameDiscoverer;

    private final ConfigurationPropertiesRuntimeValidator configurationPropertiesRuntimeValidator;

    private final ConfigurationPropertiesBeansCache configurationPropertiesBeansCache;

    private final ConfigurationPropertiesMutabilityChecker configurationPropertiesMutabilityChecker;

    public RebindingConfigurationPropertiesMutator(
            ConfigurableEnvironment configurableEnvironment,
            PropertyNameDiscoverer propertyNameDiscoverer,
            ConfigurationPropertiesRuntimeValidator configurationPropertiesRuntimeValidator,
            ConfigurationPropertiesBeansCache configurationPropertiesBeansCache,
            ConfigurationPropertiesMutabilityChecker configurationPropertiesMutabilityChecker) {
        this.configurableEnvironment = configurableEnvironment;
        this.propertyNameDiscoverer = propertyNameDiscoverer;
        this.configurationPropertiesRuntimeValidator = configurationPropertiesRuntimeValidator;
        this.configurationPropertiesBeansCache = configurationPropertiesBeansCache;
        this.configurationPropertiesMutabilityChecker = configurationPropertiesMutabilityChecker;
    }

    @Override
    public void mutate(String propertyName, String newValue) throws ConfigurationPropertyMutationException {

        ConfigurationPropertiesBean ownerBean = discoverConfigPropsBean(propertyName);
        validate(ownerBean, propertyName, newValue);

        MutablePropertySources propertySources = configurableEnvironment.getPropertySources();
        PropertySource<?> potentiallyAxelixPropertySource = propertySources.get(AXELIX_PROPERTY_SOURCE_NAME);

        String discoveredPropertyName = propertyNameDiscoverer.discover(propertyName);

        if (discoveredPropertyName == null) {
            throw new ConfigurationPropertyBindingException(
                    String.format("Unable to discover actual property name for '%s'.", propertyName));
        }

        if (potentiallyAxelixPropertySource == null) {
            Map<String, Object> source = new HashMap<>();
            source.put(discoveredPropertyName, newValue);
            propertySources.addFirst(new AxelixPropertySource(source));
        } else {
            var target = (AxelixPropertySource) potentiallyAxelixPropertySource;
            target.addProperty(discoveredPropertyName, newValue);
        }

        Binder binder = Binder.get(configurableEnvironment);
        String prefix = ownerBean.getAnnotation().prefix();
        binder.bind(prefix, ownerBean.asBindTarget());
    }

    private ConfigurationPropertiesBean discoverConfigPropsBean(String propertyName)
            throws ConfigurationPropertyBindingException {
        ConfigurationPropertiesBean ownerBean =
                configurationPropertiesBeansCache.findConfigurationPropertiesBeanByPropertyName(propertyName);

        if (ownerBean == null) {
            throw new ConfigurationPropertyBindingException(
                    String.format("No configuration properties bean found for property '%s'.", propertyName));
        }

        return ownerBean;
    }

    private void validate(ConfigurationPropertiesBean configurationPropertiesBean, String propertyName, String newValue)
            throws ConfigurationPropertyMutationException {
        if (configurationPropertiesMutabilityChecker.isNotMutable(configurationPropertiesBean)) {
            throw new ConfigurationPropertyValidationException(String.format(
                    "Cannot mutate property '%s' because it belongs to a Spring internal "
                            + "configuration bean ('%s'). Only application-specific configuration properties are mutable.",
                    propertyName, configurationPropertiesBean.getName()));
        }

        configurationPropertiesRuntimeValidator.validate(configurationPropertiesBean, propertyName, newValue);
    }
}
