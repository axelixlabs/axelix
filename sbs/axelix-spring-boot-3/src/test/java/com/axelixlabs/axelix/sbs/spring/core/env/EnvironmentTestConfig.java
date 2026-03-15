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

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesCache;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesConverter;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesFlattener;
import com.axelixlabs.axelix.sbs.spring.core.configprops.DefaultConfigurationPropertiesCache;
import com.axelixlabs.axelix.sbs.spring.core.configprops.DefaultConfigurationPropertiesConverter;
import com.axelixlabs.axelix.sbs.spring.core.configprops.DefaultConfigurationPropertiesFlattener;
import com.axelixlabs.axelix.sbs.spring.core.configprops.SmartSanitizingFunction;

/**
 * Environment test configuration.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
@TestConfiguration
public class EnvironmentTestConfig {

    @Bean
    public ConfigurationPropertiesFlattener configurationPropertiesFlattener() {
        return new DefaultConfigurationPropertiesFlattener();
    }

    @Bean
    public ConfigurationPropertiesConverter configurationPropertiesConverter(
            ConfigurationPropertiesFlattener configurationPropertiesFlattener) {
        return new DefaultConfigurationPropertiesConverter(configurationPropertiesFlattener);
    }

    @Bean
    public DefaultConfigurationPropertiesCache configurationPropertiesCache(
            SmartSanitizingFunction smartSanitizingFunction,
            ApplicationContext applicationContext,
            ConfigurationPropertiesConverter configurationPropertiesConverter) {
        return new DefaultConfigurationPropertiesCache(
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
            ValueAnnotationInjectionProcessor annotationInjectionProcessor) {
        return new ValueInjectionTrackerBeanPostProcessor(annotationInjectionProcessor);
    }

    @Bean
    public EnvPropertyEnricher envPropertyEnricher(
            Environment environment,
            PropertyNameNormalizer propertyNameNormalizer,
            PropertyMetadataExtractor metadataExtractor,
            ValueInjectionTrackerBeanPostProcessor valueInjectionTracker,
            SmartSanitizingFunction smartSanitizingFunction,
            PropertySourceDescriptionResolver sourceDescriptionResolver,
            PropertyMappingBuilder environmentMappingBuilder,
            ObjectProvider<ConfigurationPropertiesCache> cache) {
        return new DefaultEnvPropertyEnricher(
                environment,
                propertyNameNormalizer,
                metadataExtractor,
                valueInjectionTracker,
                smartSanitizingFunction,
                sourceDescriptionResolver,
                environmentMappingBuilder);
    }

    @Bean
    public PropertyMappingBuilder propertyMappingBuilder(
            PropertyNameNormalizer propertyNameNormalizer, ObjectProvider<ConfigurationPropertiesCache> cache) {
        return new DefaultPropertyMappingBuilder(propertyNameNormalizer, cache.getIfAvailable());
    }

    @Bean
    public ValueAnnotationInjectionProcessor valueAnnotationInjectionProcessor(
            PropertyNameNormalizer propertyNameNormalizer) {
        return new DefaultValueAnnotationInjectionProcessor(propertyNameNormalizer);
    }

    @Bean
    public PropertySourceDescriptionResolver propertySourceDescriptionResolver() {
        return new DefaultPropertySourceDescriptionResolver();
    }
}
