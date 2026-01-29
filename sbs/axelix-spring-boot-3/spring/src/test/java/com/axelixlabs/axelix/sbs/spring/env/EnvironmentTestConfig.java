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
package com.axelixlabs.axelix.sbs.spring.env;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import com.axelixlabs.axelix.sbs.spring.configprops.ConfigurationPropertiesCache;
import com.axelixlabs.axelix.sbs.spring.configprops.ConfigurationPropertiesConverter;
import com.axelixlabs.axelix.sbs.spring.configprops.FlatteningConfigurationPropertiesConverter;
import com.axelixlabs.axelix.sbs.spring.configprops.SmartSanitizingFunction;

/**
 * Environment test configuration.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
@TestConfiguration
public class EnvironmentTestConfig {

    @Bean
    public ConfigurationPropertiesConverter configurationPropertiesConverter() {
        return new FlatteningConfigurationPropertiesConverter();
    }

    @Bean
    public ConfigurationPropertiesCache configurationPropertiesCache(
            SmartSanitizingFunction smartSanitizingFunction,
            ApplicationContext applicationContext,
            ConfigurationPropertiesConverter configurationPropertiesConverter) {
        return new ConfigurationPropertiesCache(
                smartSanitizingFunction, applicationContext, configurationPropertiesConverter);
    }

    @Bean
    public PropertyNameNormalizer propertyNameNormalizer() {
        return new DefaultPropertyNameNormalizer();
    }

    @Bean
    public PropertyMetadataExtractor propertyMetadataExtractor(
            ConfigurableEnvironment environment, PropertyNameNormalizer propertyNameNormalizer) {
        return new DefaultPropertyMetadataExtractor(environment, propertyNameNormalizer);
    }

    @Bean
    public ValueInjectionTrackerBeanPostProcessor valueInjectionTrackerBeanPostProcessor(
            PropertyNameNormalizer propertyNameNormalizer) {
        return new ValueInjectionTrackerBeanPostProcessor(propertyNameNormalizer);
    }

    @Bean
    public EnvPropertyEnricher envPropertyEnricher(
            Environment environment,
            DefaultPropertyNameNormalizer propertyNameNormalizer,
            ObjectProvider<ConfigurationPropertiesCache> configurationPropertiesCache,
            PropertyMetadataExtractor propertyMetadataExtractor,
            ValueInjectionTrackerBeanPostProcessor valueInjectionTrackerBeanPostProcessor) {
        return new DefaultEnvPropertyEnricher(
                environment,
                propertyNameNormalizer,
                configurationPropertiesCache,
                propertyMetadataExtractor,
                valueInjectionTrackerBeanPostProcessor);
    }
}
